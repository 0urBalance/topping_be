package org.balanceus.topping.presentation.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.balanceus.topping.application.service.CollaborationService;
import org.balanceus.topping.domain.model.CollaborationProposal;
import org.balanceus.topping.domain.model.ProductImage;
import org.balanceus.topping.domain.repository.CollaborationProposalRepository;
import org.balanceus.topping.domain.repository.ProductImageRepository;
import org.balanceus.topping.domain.repository.UserRepository;
import org.balanceus.topping.infrastructure.response.ApiResponseData;
import org.balanceus.topping.infrastructure.response.Code;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/api/proposals")
@RequiredArgsConstructor
@Slf4j
public class ProposalController {

    private final CollaborationProposalRepository collaborationProposalRepository;
    private final CollaborationService collaborationService;
    private final UserRepository userRepository;
    private final ProductImageRepository productImageRepository;

    @PostMapping("")
    @ResponseBody
    public ApiResponseData<Void> submitProposal(
            @org.springframework.web.bind.annotation.RequestBody ProposalSubmissionRequest request,
            Principal principal) {
        
        try {
            // For now, return success - this would need actual implementation
            log.info("Proposal submission request received for room: {}", request.getRoomId());
            return ApiResponseData.success(null);
        } catch (Exception e) {
            log.error("Failed to submit proposal", e);
            return ApiResponseData.failure(Code.INTERNAL_SERVER_ERROR.getCode(), "Failed to submit proposal");
        }
    }

    @PostMapping("/accept")
    @ResponseBody
    public ApiResponseData<Void> acceptProposal(
            @org.springframework.web.bind.annotation.RequestBody ProposalActionRequest request,
            Principal principal) {
        
        try {
            // For now, return success - this would need actual implementation
            log.info("Proposal acceptance request received for room: {}", request.getRoomId());
            return ApiResponseData.success(null);
        } catch (Exception e) {
            log.error("Failed to accept proposal", e);
            return ApiResponseData.failure(Code.INTERNAL_SERVER_ERROR.getCode(), "Failed to accept proposal");
        }
    }

    @GetMapping("/{proposalId}")
    @ResponseBody
    public ApiResponseData<ProposalDetails> getProposalDetails(@PathVariable UUID proposalId) {
        try {
            CollaborationProposal proposal = collaborationProposalRepository.findById(proposalId)
                    .orElseThrow(() -> new RuntimeException("Proposal not found"));
            
            ProposalDetails details = new ProposalDetails();
            details.setTitle(proposal.getTitle());
            details.setDescription(proposal.getDescription());
            details.setIndustry("음식점"); // Default value
            details.setProducts(proposal.getDescription()); // Use description as products for now
            details.setProfitShare("5:5"); // Default value
            details.setDuration("1개월"); // Default value
            details.setLocation("각자 픽업"); // Default value
            
            return ApiResponseData.success(details);
        } catch (Exception e) {
            log.error("Failed to get proposal details for: {}", proposalId, e);
            return ApiResponseData.failure(Code.INTERNAL_SERVER_ERROR.getCode(), "Failed to get proposal details");
        }
    }

    @GetMapping("/{proposalId}/images")
    @ResponseBody
    public ApiResponseData<List<ProposalImage>> getProposalImages(@PathVariable UUID proposalId) {
        try {
            CollaborationProposal proposal = collaborationProposalRepository.findById(proposalId)
                    .orElseThrow(() -> new RuntimeException("Proposal not found"));
            
            // Get images from proposer product and target product
            List<ProposalImage> images = new java.util.ArrayList<>();
            
            if (proposal.getProposerProduct() != null) {
                List<ProductImage> proposerImages = productImageRepository.findByProduct(proposal.getProposerProduct());
                proposerImages.forEach(img -> {
                    ProposalImage pImg = new ProposalImage();
                    pImg.setImagePath(img.getImagePath());
                    pImg.setAltText("제안자 상품 이미지");
                    images.add(pImg);
                });
            }
            
            if (proposal.getTargetProduct() != null) {
                List<ProductImage> targetImages = productImageRepository.findByProduct(proposal.getTargetProduct());
                targetImages.forEach(img -> {
                    ProposalImage pImg = new ProposalImage();
                    pImg.setImagePath(img.getImagePath());
                    pImg.setAltText("대상 상품 이미지");
                    images.add(pImg);
                });
            }
            
            return ApiResponseData.success(images);
        } catch (Exception e) {
            log.error("Failed to get proposal images for: {}", proposalId, e);
            return ApiResponseData.failure(Code.INTERNAL_SERVER_ERROR.getCode(), "Failed to get proposal images");
        }
    }

    // Request/Response DTOs
    public static class ProposalSubmissionRequest {
        private UUID roomId;
        private String industry;
        private String products;
        private String share;
        private String duration;
        private String place;

        public UUID getRoomId() { return roomId; }
        public void setRoomId(UUID roomId) { this.roomId = roomId; }

        public String getIndustry() { return industry; }
        public void setIndustry(String industry) { this.industry = industry; }

        public String getProducts() { return products; }
        public void setProducts(String products) { this.products = products; }

        public String getShare() { return share; }
        public void setShare(String share) { this.share = share; }

        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }

        public String getPlace() { return place; }
        public void setPlace(String place) { this.place = place; }
    }

    public static class ProposalActionRequest {
        private UUID roomId;

        public UUID getRoomId() { return roomId; }
        public void setRoomId(UUID roomId) { this.roomId = roomId; }
    }

    public static class ProposalDetails {
        private String title;
        private String description;
        private String industry;
        private String products;
        private String profitShare;
        private String duration;
        private String location;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getIndustry() { return industry; }
        public void setIndustry(String industry) { this.industry = industry; }

        public String getProducts() { return products; }
        public void setProducts(String products) { this.products = products; }

        public String getProfitShare() { return profitShare; }
        public void setProfitShare(String profitShare) { this.profitShare = profitShare; }

        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }

    public static class ProposalImage {
        private String imagePath;
        private String altText;

        public String getImagePath() { return imagePath; }
        public void setImagePath(String imagePath) { this.imagePath = imagePath; }

        public String getAltText() { return altText; }
        public void setAltText(String altText) { this.altText = altText; }
    }
}