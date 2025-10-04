package org.balanceus.topping.application.service;

import lombok.RequiredArgsConstructor;
import org.balanceus.topping.domain.model.Update;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.UpdateRepository;
import org.balanceus.topping.domain.repository.UserRepository;
import org.balanceus.topping.infrastructure.exception.BaseException;
import org.balanceus.topping.infrastructure.response.Code;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UpdateService {
    
    private final UpdateRepository updateRepository;
    private final UserRepository userRepository;
    
    /**
     * 발행된 업데이트 목록 조회 (사용자용)
     */
    public Page<Update> getPublishedUpdates(Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        return updateRepository.findByIsActiveTrueAndPublishDateBeforeOrderByIsPinnedDescPublishDateDesc(now, pageable);
    }
    
    /**
     * 업데이트 타입별 조회
     */
    public Page<Update> getUpdatesByType(Update.UpdateType type, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        return updateRepository.findByTypeAndIsActiveTrueAndPublishDateBefore(type, now, pageable);
    }
    
    /**
     * 우선순위별 업데이트 조회
     */
    public Page<Update> getUpdatesByPriority(Update.UpdatePriority priority, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        return updateRepository.findByPriorityAndIsActiveTrueAndPublishDateBefore(priority, now, pageable);
    }
    
    /**
     * 고정된 업데이트 조회
     */
    public List<Update> getPinnedUpdates() {
        LocalDateTime now = LocalDateTime.now();
        return updateRepository.findByIsPinnedTrueAndIsActiveTrueAndPublishDateBeforeOrderByPublishDateDesc(now);
    }
    
    /**
     * 인기 업데이트 조회 (조회수 기준)
     */
    public List<Update> getPopularUpdates() {
        LocalDateTime now = LocalDateTime.now();
        return updateRepository.findTop10ByIsActiveTrueAndPublishDateBeforeOrderByViewCountDesc(now);
    }
    
    /**
     * 최신 업데이트 조회
     */
    public List<Update> getRecentUpdates() {
        LocalDateTime now = LocalDateTime.now();
        return updateRepository.findTop5ByIsActiveTrueAndPublishDateBeforeOrderByPublishDateDesc(now);
    }
    
    /**
     * 업데이트 상세 조회 및 조회수 증가
     */
    @Transactional
    public Update getUpdateById(UUID id) {
        Optional<Update> updateOptional = updateRepository.findById(id);
        if (updateOptional.isEmpty()) {
            throw new BaseException(Code.NOT_FOUND, "Update not found");
        }
        
        Update update = updateOptional.get();
        
        // 발행되지 않았거나 비활성화된 업데이트는 조회 불가
        LocalDateTime now = LocalDateTime.now();
        if (!update.isActive() || update.getPublishDate().isAfter(now)) {
            throw new BaseException(Code.NOT_FOUND, "Update not available");
        }
        
        // 조회수 증가
        update.incrementViewCount();
        updateRepository.save(update);
        
        return update;
    }
    
    /**
     * 업데이트 검색
     */
    public Page<Update> searchUpdates(String keyword, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        return updateRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseAndIsActiveTrueAndPublishDateBefore(
            keyword, keyword, now, pageable);
    }
    
    /**
     * 태그별 업데이트 조회
     */
    public Page<Update> getUpdatesByTag(String tag, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        return updateRepository.findByTagsContainingAndIsActiveTrueAndPublishDateBefore(tag, now, pageable);
    }
    
    // ====== 관리자용 메서드들 ======
    
    /**
     * 모든 업데이트 조회 (관리자용)
     */
    public Page<Update> getAllUpdates(Pageable pageable) {
        return updateRepository.findAll(pageable);
    }
    
    /**
     * 업데이트 생성
     */
    @Transactional
    public Update createUpdate(Update update, UUID authorUuid) {
        Optional<User> userOptional = userRepository.findById(authorUuid);
        if (userOptional.isEmpty()) {
            throw new BaseException(Code.NOT_FOUND, "User not found");
        }
        
        update.setAuthor(userOptional.get());
        
        // 발행일이 설정되지 않았으면 현재 시간으로 설정
        if (update.getPublishDate() == null) {
            update.setPublishDate(LocalDateTime.now());
        }
        
        return updateRepository.save(update);
    }
    
    /**
     * 업데이트 수정
     */
    @Transactional
    public Update updateUpdate(UUID id, Update updateData) {
        Optional<Update> existingUpdateOptional = updateRepository.findById(id);
        if (existingUpdateOptional.isEmpty()) {
            throw new BaseException(Code.NOT_FOUND, "Update not found");
        }
        
        Update existingUpdate = existingUpdateOptional.get();
        
        // 업데이트 가능한 필드들만 수정
        existingUpdate.setTitle(updateData.getTitle());
        existingUpdate.setContent(updateData.getContent());
        existingUpdate.setType(updateData.getType());
        existingUpdate.setPriority(updateData.getPriority());
        existingUpdate.setActive(updateData.isActive());
        existingUpdate.setPinned(updateData.isPinned());
        existingUpdate.setPublishDate(updateData.getPublishDate());
        existingUpdate.setImageUrl(updateData.getImageUrl());
        existingUpdate.setTags(updateData.getTags());
        
        return updateRepository.save(existingUpdate);
    }
    
    /**
     * 업데이트 삭제
     */
    @Transactional
    public void deleteUpdate(UUID id) {
        if (!updateRepository.existsById(id)) {
            throw new BaseException(Code.NOT_FOUND, "Update not found");
        }
        updateRepository.deleteById(id);
    }
    
    /**
     * 업데이트 활성화/비활성화
     */
    @Transactional
    public Update toggleUpdateStatus(UUID id) {
        Optional<Update> updateOptional = updateRepository.findById(id);
        if (updateOptional.isEmpty()) {
            throw new BaseException(Code.NOT_FOUND, "Update not found");
        }
        
        Update update = updateOptional.get();
        update.setActive(!update.isActive());
        
        return updateRepository.save(update);
    }
    
    /**
     * 업데이트 고정/고정해제
     */
    @Transactional
    public Update toggleUpdatePin(UUID id) {
        Optional<Update> updateOptional = updateRepository.findById(id);
        if (updateOptional.isEmpty()) {
            throw new BaseException(Code.NOT_FOUND, "Update not found");
        }
        
        Update update = updateOptional.get();
        update.setPinned(!update.isPinned());
        
        return updateRepository.save(update);
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
}