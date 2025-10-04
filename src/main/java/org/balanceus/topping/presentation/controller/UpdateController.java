package org.balanceus.topping.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.balanceus.topping.application.dto.UpdateSummaryDto;
import org.balanceus.topping.application.service.UpdateService;
import org.balanceus.topping.domain.model.Update;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/updates")
public class UpdateController {
    
    private final UpdateService updateService;
    
    /**
     * 업데이트 목록 페이지
     */
    @GetMapping
    public String updatesList(Model model,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "12") int size,
                             @RequestParam(required = false) String type,
                             @RequestParam(required = false) String priority,
                             @RequestParam(required = false) String search,
                             @RequestParam(required = false) String tag) {
        
        // 페이징 설정
        Pageable pageable = PageRequest.of(page, size, Sort.by("isPinned").descending()
                .and(Sort.by("publishDate").descending()));
        
        Page<Update> updatePage;
        
        // 검색 조건에 따른 조회
        if (search != null && !search.trim().isEmpty()) {
            updatePage = updateService.searchUpdates(search.trim(), pageable);
            model.addAttribute("searchKeyword", search.trim());
        } else if (tag != null && !tag.trim().isEmpty()) {
            updatePage = updateService.getUpdatesByTag(tag.trim(), pageable);
            model.addAttribute("selectedTag", tag.trim());
        } else if (type != null && !type.trim().isEmpty()) {
            try {
                Update.UpdateType updateType = Update.UpdateType.valueOf(type.toUpperCase());
                updatePage = updateService.getUpdatesByType(updateType, pageable);
                model.addAttribute("selectedType", updateType);
            } catch (IllegalArgumentException e) {
                updatePage = updateService.getPublishedUpdates(pageable);
            }
        } else if (priority != null && !priority.trim().isEmpty()) {
            try {
                Update.UpdatePriority updatePriority = Update.UpdatePriority.valueOf(priority.toUpperCase());
                updatePage = updateService.getUpdatesByPriority(updatePriority, pageable);
                model.addAttribute("selectedPriority", updatePriority);
            } catch (IllegalArgumentException e) {
                updatePage = updateService.getPublishedUpdates(pageable);
            }
        } else {
            updatePage = updateService.getPublishedUpdates(pageable);
        }
        
        // Update를 UpdateSummaryDto로 변환
        Page<UpdateSummaryDto> updateSummaryPage = updatePage.map(UpdateSummaryDto::from);
        
        // 고정된 업데이트 조회 (첫 페이지에서만)
        List<UpdateSummaryDto> pinnedUpdates = null;
        if (page == 0 && (search == null || search.trim().isEmpty()) && 
            (tag == null || tag.trim().isEmpty()) && 
            (type == null || type.trim().isEmpty()) && 
            (priority == null || priority.trim().isEmpty())) {
            pinnedUpdates = updateService.getPinnedUpdates()
                .stream()
                .map(UpdateSummaryDto::from)
                .collect(Collectors.toList());
        }
        
        // 최신 업데이트 5개 (사이드바용)
        List<UpdateSummaryDto> recentUpdates = updateService.getRecentUpdates()
            .stream()
            .map(UpdateSummaryDto::from)
            .collect(Collectors.toList());
        
        // 인기 업데이트 10개 (사이드바용)
        List<UpdateSummaryDto> popularUpdates = updateService.getPopularUpdates()
            .stream()
            .map(UpdateSummaryDto::from)
            .collect(Collectors.toList());
        
        // 모델에 데이터 추가
        model.addAttribute("updatePage", updateSummaryPage);
        model.addAttribute("pinnedUpdates", pinnedUpdates);
        model.addAttribute("recentUpdates", recentUpdates);
        model.addAttribute("popularUpdates", popularUpdates);
        model.addAttribute("updateTypes", Update.UpdateType.values());
        model.addAttribute("updatePriorities", Update.UpdatePriority.values());
        
        // 페이징 정보
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", updateSummaryPage.getTotalPages());
        model.addAttribute("totalElements", updateSummaryPage.getTotalElements());
        
        return "updates/list";
    }
    
    /**
     * 업데이트 상세 페이지
     */
    @GetMapping("/{id}")
    public String updateDetail(@PathVariable UUID id, Model model) {
        
        Update update = updateService.getUpdateById(id);
        
        // 관련 업데이트들 (같은 타입)
        List<UpdateSummaryDto> relatedUpdates = updateService.getUpdatesByType(
            update.getType(), PageRequest.of(0, 5))
            .getContent()
            .stream()
            .filter(u -> !u.getId().equals(id)) // 현재 업데이트 제외
            .map(UpdateSummaryDto::from)
            .collect(Collectors.toList());
        
        // 최신 업데이트 5개 (사이드바용)
        List<UpdateSummaryDto> recentUpdates = updateService.getRecentUpdates()
            .stream()
            .map(UpdateSummaryDto::from)
            .collect(Collectors.toList());
        
        model.addAttribute("update", update);
        model.addAttribute("relatedUpdates", relatedUpdates);
        model.addAttribute("recentUpdates", recentUpdates);
        
        return "updates/detail";
    }
}