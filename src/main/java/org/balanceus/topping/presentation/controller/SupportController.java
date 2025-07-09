package org.balanceus.topping.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.balanceus.topping.domain.model.FAQ;
import org.balanceus.topping.domain.model.SupportInquiry;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.FAQRepository;
import org.balanceus.topping.domain.repository.SupportInquiryRepository;
import org.balanceus.topping.infrastructure.response.ApiResponseData;
import org.balanceus.topping.infrastructure.security.UserDetailsImpl;
import org.balanceus.topping.presentation.dto.InquiryRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/support")
public class SupportController {
    
    private final FAQRepository faqRepository;
    private final SupportInquiryRepository supportInquiryRepository;
    
    @GetMapping("/cs")
    public String customerSupport(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(required = false) String category,
                                @RequestParam(required = false) String search) {
        
        // Get popular FAQs for display
        List<FAQ> popularFAQs = faqRepository.findTop10ByIsActiveTrueOrderByViewCountDesc();
        
        // Get paginated FAQs
        Pageable pageable = PageRequest.of(page, size, Sort.by("sortOrder").ascending());
        Page<FAQ> faqPage;
        
        if (search != null && !search.trim().isEmpty()) {
            faqPage = faqRepository.findByQuestionContainingIgnoreCaseOrAnswerContainingIgnoreCaseAndIsActiveTrue(
                search, search, pageable);
        } else if (category != null && !category.isEmpty()) {
            try {
                FAQ.FAQCategory faqCategory = FAQ.FAQCategory.valueOf(category.toUpperCase());
                faqPage = faqRepository.findByCategoryAndIsActiveTrue(faqCategory, pageable);
            } catch (IllegalArgumentException e) {
                faqPage = faqRepository.findByIsActiveTrueOrderBySortOrderAsc(pageable);
            }
        } else {
            faqPage = faqRepository.findByIsActiveTrueOrderBySortOrderAsc(pageable);
        }
        
        model.addAttribute("popularFAQs", popularFAQs);
        model.addAttribute("faqPage", faqPage);
        model.addAttribute("faqCategories", FAQ.FAQCategory.values());
        model.addAttribute("currentCategory", category);
        model.addAttribute("currentSearch", search);
        
        return "support/cs";
    }
    
    @GetMapping("/inquiry-form")
    public String inquiryForm(Model model, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails != null) {
            User user = userDetails.getUser();
            model.addAttribute("user", user);
        }
        
        model.addAttribute("inquiryCategories", SupportInquiry.InquiryCategory.values());
        model.addAttribute("inquiryRequest", new InquiryRequest());
        
        return "support/inquiry-form";
    }
    
    @PostMapping("/inquiry")
    public String submitInquiry(@ModelAttribute InquiryRequest request,
                               @RequestParam(required = false) MultipartFile attachment,
                               @AuthenticationPrincipal UserDetailsImpl userDetails,
                               Model model) {
        
        if (userDetails == null) {
            model.addAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/auth/login";
        }
        
        try {
            User user = userDetails.getUser();
            
            SupportInquiry inquiry = SupportInquiry.builder()
                .title(request.getTitle())
                .category(request.getCategory())
                .content(request.getContent())
                .user(user)
                .status(SupportInquiry.InquiryStatus.PENDING)
                .build();
            
            // Handle file attachment if present
            if (attachment != null && !attachment.isEmpty()) {
                // TODO: Implement file upload functionality
                inquiry.setAttachmentFileName(attachment.getOriginalFilename());
                // inquiry.setAttachmentPath(uploadedFilePath);
            }
            
            supportInquiryRepository.save(inquiry);
            
            model.addAttribute("successMessage", "문의가 성공적으로 접수되었습니다.");
            return "redirect:/support/cs?submitted=true";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "문의 접수 중 오류가 발생했습니다.");
            model.addAttribute("inquiryCategories", SupportInquiry.InquiryCategory.values());
            model.addAttribute("inquiryRequest", request);
            return "support/inquiry-form";
        }
    }
    
    @GetMapping("/faq/{id}")
    public String viewFAQ(@PathVariable UUID id, Model model) {
        Optional<FAQ> faqOpt = faqRepository.findById(id);
        
        if (faqOpt.isEmpty() || !faqOpt.get().isActive()) {
            return "redirect:/support/cs";
        }
        
        FAQ faq = faqOpt.get();
        
        // Increment view count
        faq.setViewCount(faq.getViewCount() + 1);
        faqRepository.save(faq);
        
        model.addAttribute("faq", faq);
        
        return "support/faq-detail";
    }
    
    @GetMapping("/my-inquiries")
    public String myInquiries(@AuthenticationPrincipal UserDetailsImpl userDetails,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             Model model) {
        
        if (userDetails == null) {
            return "redirect:/auth/login";
        }
        
        User user = userDetails.getUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Page<SupportInquiry> inquiriesPage = supportInquiryRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        
        model.addAttribute("inquiriesPage", inquiriesPage);
        model.addAttribute("inquiryStatuses", SupportInquiry.InquiryStatus.values());
        
        return "support/my-inquiries";
    }
    
    @GetMapping("/inquiry/{id}")
    public String viewInquiry(@PathVariable UUID id,
                             @AuthenticationPrincipal UserDetailsImpl userDetails,
                             Model model) {
        
        if (userDetails == null) {
            return "redirect:/auth/login";
        }
        
        Optional<SupportInquiry> inquiryOpt = supportInquiryRepository.findById(id);
        
        if (inquiryOpt.isEmpty()) {
            return "redirect:/support/my-inquiries";
        }
        
        SupportInquiry inquiry = inquiryOpt.get();
        User user = userDetails.getUser();
        
        // Check if user owns this inquiry or is admin
        if (!inquiry.getUser().getUuid().equals(user.getUuid()) && 
            !user.getRole().name().contains("ADMIN")) {
            return "redirect:/support/my-inquiries";
        }
        
        model.addAttribute("inquiry", inquiry);
        
        return "support/inquiry-detail";
    }
    
    // API endpoints for AJAX functionality
    @GetMapping("/api/faqs")
    @ResponseBody
    public ResponseEntity<ApiResponseData<Page<FAQ>>> getFAQsApi(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("sortOrder").ascending());
            Page<FAQ> faqPage;
            
            if (search != null && !search.trim().isEmpty()) {
                faqPage = faqRepository.findByQuestionContainingIgnoreCaseOrAnswerContainingIgnoreCaseAndIsActiveTrue(
                    search, search, pageable);
            } else if (category != null && !category.isEmpty()) {
                FAQ.FAQCategory faqCategory = FAQ.FAQCategory.valueOf(category.toUpperCase());
                faqPage = faqRepository.findByCategoryAndIsActiveTrue(faqCategory, pageable);
            } else {
                faqPage = faqRepository.findByIsActiveTrueOrderBySortOrderAsc(pageable);
            }
            
            return ResponseEntity.ok(ApiResponseData.success(faqPage));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponseData.failure(500, "FAQ 목록을 불러오는데 실패했습니다."));
        }
    }
}