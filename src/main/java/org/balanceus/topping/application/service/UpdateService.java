package org.balanceus.topping.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.balanceus.topping.application.dto.UpdateCreateRequest;
import org.balanceus.topping.application.dto.UpdateResponse;
import org.balanceus.topping.application.dto.UpdateSummaryDto;
import org.balanceus.topping.application.dto.UpdateUpdateRequest;
import org.balanceus.topping.application.exception.ApplicationErrorCode;
import org.balanceus.topping.application.exception.ApplicationException;
import org.balanceus.topping.domain.model.Update;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.UpdateRepository;
import org.balanceus.topping.domain.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UpdateService {

    private final UpdateRepository updateRepository;
    private final UserRepository userRepository;

    /**
     * 발행된 업데이트 목록 조회 (사용자용)
     */
    public Page<UpdateSummaryDto> getPublishedUpdates(Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        return updateRepository
            .findByIsActiveTrueAndPublishDateBeforeOrderByIsPinnedDescPublishDateDesc(now, pageable)
            .map(UpdateSummaryDto::from);
    }

    /**
     * 업데이트 타입별 조회
     */
    public Page<UpdateSummaryDto> getUpdatesByType(Update.UpdateType type, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        return updateRepository.findByTypeAndIsActiveTrueAndPublishDateBefore(type, now, pageable)
            .map(UpdateSummaryDto::from);
    }

    /**
     * 우선순위별 업데이트 조회
     */
    public Page<UpdateSummaryDto> getUpdatesByPriority(Update.UpdatePriority priority, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        return updateRepository.findByPriorityAndIsActiveTrueAndPublishDateBefore(priority, now, pageable)
            .map(UpdateSummaryDto::from);
    }

    /**
     * 고정된 업데이트 조회
     */
    public List<UpdateSummaryDto> getPinnedUpdates() {
        LocalDateTime now = LocalDateTime.now();
        return updateRepository.findByIsPinnedTrueAndIsActiveTrueAndPublishDateBeforeOrderByPublishDateDesc(now)
            .stream()
            .map(UpdateSummaryDto::from)
            .toList();
    }

    /**
     * 인기 업데이트 조회 (조회수 기준)
     */
    public List<UpdateSummaryDto> getPopularUpdates() {
        LocalDateTime now = LocalDateTime.now();
        return updateRepository.findTop10ByIsActiveTrueAndPublishDateBeforeOrderByViewCountDesc(now)
            .stream()
            .map(UpdateSummaryDto::from)
            .toList();
    }

    /**
     * 최신 업데이트 조회
     */
    public List<UpdateSummaryDto> getRecentUpdates() {
        LocalDateTime now = LocalDateTime.now();
        return updateRepository.findTop5ByIsActiveTrueAndPublishDateBeforeOrderByPublishDateDesc(now)
            .stream()
            .map(UpdateSummaryDto::from)
            .toList();
    }

    /**
     * 업데이트 상세 조회 및 조회수 증가 (사용자용)
     */
    @Transactional
    public UpdateResponse viewPublishedUpdate(UUID id) {
        Update update = findUpdateById(id);
        LocalDateTime now = LocalDateTime.now();
        if (!update.isActive() || update.getPublishDate().isAfter(now)) {
            throw new ApplicationException(ApplicationErrorCode.NOT_FOUND, "Update not available");
        }

        update.incrementViewCount();
        Update saved = updateRepository.save(update);
        return UpdateResponse.from(saved);
    }

    /**
     * 업데이트 검색
     */
    public Page<UpdateSummaryDto> searchUpdates(String keyword, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        return updateRepository
            .findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseAndIsActiveTrueAndPublishDateBefore(
                keyword, keyword, now, pageable)
            .map(UpdateSummaryDto::from);
    }

    /**
     * 태그별 업데이트 조회
     */
    public Page<UpdateSummaryDto> getUpdatesByTag(String tag, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        return updateRepository.findByTagsContainingAndIsActiveTrueAndPublishDateBefore(tag, now, pageable)
            .map(UpdateSummaryDto::from);
    }

    // ====== 관리자용 메서드들 ======

    /**
     * 모든 업데이트 조회 (관리자용)
     */
    public Page<UpdateResponse> getAllUpdates(Pageable pageable) {
        return updateRepository.findAll(pageable).map(UpdateResponse::from);
    }

    /**
     * 관리자용 업데이트 상세 조회
     */
    public UpdateResponse getUpdateForAdmin(UUID id) {
        return UpdateResponse.from(findUpdateById(id));
    }

    /**
     * 업데이트 생성
     */
    @Transactional
    public UpdateResponse createUpdate(UpdateCreateRequest request, UUID authorUuid) {
        User author = userRepository.findById(authorUuid)
            .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.NOT_FOUND, "User not found"));

        LocalDateTime publishDate = request.getPublishDate() != null ? request.getPublishDate() : LocalDateTime.now();

        Update update = Update.builder()
            .title(request.getTitle())
            .content(request.getContent())
            .type(request.getType())
            .priority(request.getPriority())
            .isActive(request.isActive())
            .isPinned(request.isPinned())
            .publishDate(publishDate)
            .imageUrl(request.getImageUrl())
            .tags(request.getTags())
            .author(author)
            .build();

        Update saved = updateRepository.save(update);
        return UpdateResponse.from(saved);
    }

    /**
     * 업데이트 수정
     */
    @Transactional
    public UpdateResponse updateUpdate(UUID id, UpdateUpdateRequest request) {
        Update existingUpdate = findUpdateById(id);

        existingUpdate.setTitle(request.getTitle());
        existingUpdate.setContent(request.getContent());
        existingUpdate.setType(request.getType());
        existingUpdate.setPriority(request.getPriority());
        existingUpdate.setActive(request.isActive());
        existingUpdate.setPinned(request.isPinned());
        existingUpdate.setPublishDate(request.getPublishDate());
        existingUpdate.setImageUrl(request.getImageUrl());
        existingUpdate.setTags(request.getTags());

        Update saved = updateRepository.save(existingUpdate);
        return UpdateResponse.from(saved);
    }

    /**
     * 업데이트 삭제
     */
    @Transactional
    public void deleteUpdate(UUID id) {
        if (!updateRepository.existsById(id)) {
            throw new ApplicationException(ApplicationErrorCode.NOT_FOUND, "Update not found");
        }
        updateRepository.deleteById(id);
    }

    /**
     * 업데이트 활성화/비활성화
     */
    @Transactional
    public UpdateResponse toggleUpdateStatus(UUID id) {
        Update update = findUpdateById(id);
        update.setActive(!update.isActive());

        Update saved = updateRepository.save(update);
        return UpdateResponse.from(saved);
    }

    /**
     * 업데이트 고정/고정해제
     */
    @Transactional
    public UpdateResponse toggleUpdatePin(UUID id) {
        Update update = findUpdateById(id);
        update.setPinned(!update.isPinned());

        Update saved = updateRepository.save(update);
        return UpdateResponse.from(saved);
    }

    /**
     * 통계 메서드들
     */
    public long getTotalUpdatesCount() {
        return updateRepository.count();
    }

    public long getActiveUpdatesCount() {
        return updateRepository.countByIsActiveTrue();
    }

    public long getInactiveUpdatesCount() {
        return updateRepository.countByIsActiveFalse();
    }

    public long getPinnedUpdatesCount() {
        return updateRepository.countByIsPinnedTrue();
    }

    public long getUpdatesCountByType(Update.UpdateType type) {
        return updateRepository.countByType(type);
    }

    public long getUpdatesCountByPriority(Update.UpdatePriority priority) {
        return updateRepository.countByPriority(priority);
    }

    private Update findUpdateById(UUID id) {
        return updateRepository.findById(id)
            .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.NOT_FOUND, "Update not found"));
    }
}
