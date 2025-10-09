package org.balanceus.topping.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.balanceus.topping.application.dto.UpdateCreateRequest;
import org.balanceus.topping.application.dto.UpdateResponse;
import org.balanceus.topping.application.dto.UpdateUpdateRequest;
import org.balanceus.topping.application.service.UpdateService;
import org.balanceus.topping.domain.model.Update;
import org.balanceus.topping.infrastructure.response.ApiResponseData;
import org.balanceus.topping.infrastructure.security.UserDetailsImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/updates")
@PreAuthorize("hasRole('ADMIN')")
public class UpdateAdminController {
    
    private final UpdateService updateService;
    
    /**
     * 관리자 업데이트 목록 페이지
     */
    @GetMapping
    public String adminUpdatesList(Model model,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size,
                                  @RequestParam(required = false) String type,
                                  @RequestParam(required = false) String status) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<UpdateResponse> updateResponsePage = updateService.getAllUpdates(pageable);
        
        // 통계 정보
        long totalCount = updateService.getTotalUpdatesCount();
        long activeCount = updateService.getActiveUpdatesCount();
        long inactiveCount = updateService.getInactiveUpdatesCount();
        long pinnedCount = updateService.getPinnedUpdatesCount();
        
        model.addAttribute("updatePage", updateResponsePage);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("inactiveCount", inactiveCount);
        model.addAttribute("pinnedCount", pinnedCount);
        model.addAttribute("updateTypes", Update.UpdateType.values());
        model.addAttribute("updatePriorities", Update.UpdatePriority.values());
        
        return "admin/updates/list";
    }
    
    /**
     * 업데이트 생성 페이지
     */
    @GetMapping("/create")
    public String createUpdateForm(Model model) {
        model.addAttribute("updateRequest", new UpdateCreateRequest());
        model.addAttribute("updateTypes", Update.UpdateType.values());
        model.addAttribute("updatePriorities", Update.UpdatePriority.values());
        return "admin/updates/create";
    }
    
    /**
     * 업데이트 생성 처리
     */
    @PostMapping("/create")
    public String createUpdate(@Valid @ModelAttribute("updateRequest") UpdateCreateRequest request,
                              BindingResult bindingResult,
                              @AuthenticationPrincipal UserDetailsImpl userDetails,
                              Model model) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("updateTypes", Update.UpdateType.values());
            model.addAttribute("updatePriorities", Update.UpdatePriority.values());
            return "admin/updates/create";
        }
        
        try {
            updateService.createUpdate(request, userDetails.getUser().getUuid());
            
            return "redirect:/admin/updates?success=created";
        } catch (Exception e) {
            model.addAttribute("error", "업데이트 생성 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("updateTypes", Update.UpdateType.values());
            model.addAttribute("updatePriorities", Update.UpdatePriority.values());
            return "admin/updates/create";
        }
    }
    
    /**
     * 업데이트 수정 페이지
     */
    @GetMapping("/{id}/edit")
    public String editUpdateForm(@PathVariable UUID id, Model model) {
        try {
            UpdateResponse update = updateService.getUpdateForAdmin(id);
            
            UpdateUpdateRequest request = new UpdateUpdateRequest();
            request.setTitle(update.getTitle());
            request.setContent(update.getContent());
            request.setType(update.getType());
            request.setPriority(update.getPriority());
            request.setActive(update.isActive());
            request.setPinned(update.isPinned());
            request.setPublishDate(update.getPublishDate());
            request.setImageUrl(update.getImageUrl());
            request.setTags(update.getTags());
            
            model.addAttribute("update", update);
            model.addAttribute("updateRequest", request);
            model.addAttribute("updateTypes", Update.UpdateType.values());
            model.addAttribute("updatePriorities", Update.UpdatePriority.values());
            
            return "admin/updates/edit";
        } catch (Exception e) {
            return "redirect:/admin/updates?error=notfound";
        }
    }
    
    /**
     * 업데이트 수정 처리
     */
    @PostMapping("/{id}/edit")
    public String updateUpdate(@PathVariable UUID id,
                              @Valid @ModelAttribute("updateRequest") UpdateUpdateRequest request,
                              BindingResult bindingResult,
                              Model model) {
        
        if (bindingResult.hasErrors()) {
            try {
                UpdateResponse update = updateService.getUpdateForAdmin(id);
                model.addAttribute("update", update);
                model.addAttribute("updateTypes", Update.UpdateType.values());
                model.addAttribute("updatePriorities", Update.UpdatePriority.values());
                return "admin/updates/edit";
            } catch (Exception e) {
                return "redirect:/admin/updates?error=notfound";
            }
        }
        
        try {
            updateService.updateUpdate(id, request);
            
            return "redirect:/admin/updates?success=updated";
        } catch (Exception e) {
            model.addAttribute("error", "업데이트 수정 중 오류가 발생했습니다: " + e.getMessage());
            try {
                UpdateResponse update = updateService.getUpdateForAdmin(id);
                model.addAttribute("update", update);
                model.addAttribute("updateTypes", Update.UpdateType.values());
                model.addAttribute("updatePriorities", Update.UpdatePriority.values());
                return "admin/updates/edit";
            } catch (Exception ex) {
                return "redirect:/admin/updates?error=notfound";
            }
        }
    }
    
    /**
     * 업데이트 삭제
     */
    @PostMapping("/{id}/delete")
    public String deleteUpdate(@PathVariable UUID id) {
        try {
            updateService.deleteUpdate(id);
            return "redirect:/admin/updates?success=deleted";
        } catch (Exception e) {
            return "redirect:/admin/updates?error=deletefailed";
        }
    }
    
    // ====== AJAX API 엔드포인트들 ======
    
    /**
     * 업데이트 활성화/비활성화 토글
     */
    @PostMapping("/{id}/toggle-status")
    @ResponseBody
    public ResponseEntity<ApiResponseData<UpdateResponse>> toggleUpdateStatus(@PathVariable UUID id) {
        try {
            UpdateResponse update = updateService.toggleUpdateStatus(id);
            return ResponseEntity.ok(ApiResponseData.success(update));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponseData.failure(400, "상태 변경에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 업데이트 고정/고정해제 토글
     */
    @PostMapping("/{id}/toggle-pin")
    @ResponseBody
    public ResponseEntity<ApiResponseData<UpdateResponse>> toggleUpdatePin(@PathVariable UUID id) {
        try {
            UpdateResponse update = updateService.toggleUpdatePin(id);
            return ResponseEntity.ok(ApiResponseData.success(update));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponseData.failure(400, "고정 상태 변경에 실패했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 업데이트 상세 조회 (AJAX)
     */
    @GetMapping("/{id}/details")
    @ResponseBody
    public ResponseEntity<ApiResponseData<UpdateResponse>> getUpdateDetails(@PathVariable UUID id) {
        try {
            UpdateResponse update = updateService.getUpdateForAdmin(id);
            return ResponseEntity.ok(ApiResponseData.success(update));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponseData.failure(404, "업데이트를 찾을 수 없습니다: " + e.getMessage()));
        }
    }
}
