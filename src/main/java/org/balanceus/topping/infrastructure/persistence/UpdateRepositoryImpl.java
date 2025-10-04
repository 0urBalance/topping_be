package org.balanceus.topping.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.balanceus.topping.domain.model.Update;
import org.balanceus.topping.domain.repository.UpdateRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UpdateRepositoryImpl implements UpdateRepository {
    
    private final UpdateJpaRepository updateJpaRepository;
    
    @Override
    public <S extends Update> S save(S entity) {
        return updateJpaRepository.save(entity);
    }
    
    @Override
    public <S extends Update> List<S> saveAll(Iterable<S> entities) {
        return updateJpaRepository.saveAll(entities);
    }
    
    @Override
    public Optional<Update> findById(UUID id) {
        return updateJpaRepository.findById(id);
    }
    
    @Override
    public List<Update> findAll() {
        return updateJpaRepository.findAll();
    }
    
    @Override
    public Page<Update> findAll(Pageable pageable) {
        return updateJpaRepository.findAll(pageable);
    }
    
    @Override
    public List<Update> findByIsActiveTrue() {
        return updateJpaRepository.findByIsActiveTrue();
    }
    
    @Override
    public Page<Update> findByIsActiveTrue(Pageable pageable) {
        return updateJpaRepository.findByIsActiveTrue(pageable);
    }
    
    @Override
    public List<Update> findByIsActiveTrueAndPublishDateBeforeOrderByIsPinnedDescPublishDateDesc(LocalDateTime now) {
        return updateJpaRepository.findByIsActiveTrueAndPublishDateBeforeOrderByIsPinnedDescPublishDateDesc(now);
    }
    
    @Override
    public Page<Update> findByIsActiveTrueAndPublishDateBeforeOrderByIsPinnedDescPublishDateDesc(LocalDateTime now, Pageable pageable) {
        return updateJpaRepository.findByIsActiveTrueAndPublishDateBeforeOrderByIsPinnedDescPublishDateDesc(now, pageable);
    }
    
    @Override
    public List<Update> findByType(Update.UpdateType type) {
        return updateJpaRepository.findByType(type);
    }
    
    @Override
    public Page<Update> findByType(Update.UpdateType type, Pageable pageable) {
        return updateJpaRepository.findByType(type, pageable);
    }
    
    @Override
    public List<Update> findByTypeAndIsActiveTrueAndPublishDateBefore(Update.UpdateType type, LocalDateTime now) {
        return updateJpaRepository.findByTypeAndIsActiveTrueAndPublishDateBefore(type, now);
    }
    
    @Override
    public Page<Update> findByTypeAndIsActiveTrueAndPublishDateBefore(Update.UpdateType type, LocalDateTime now, Pageable pageable) {
        return updateJpaRepository.findByTypeAndIsActiveTrueAndPublishDateBefore(type, now, pageable);
    }
    
    @Override
    public List<Update> findByPriorityAndIsActiveTrueAndPublishDateBefore(Update.UpdatePriority priority, LocalDateTime now) {
        return updateJpaRepository.findByPriorityAndIsActiveTrueAndPublishDateBefore(priority, now);
    }
    
    @Override
    public Page<Update> findByPriorityAndIsActiveTrueAndPublishDateBefore(Update.UpdatePriority priority, LocalDateTime now, Pageable pageable) {
        return updateJpaRepository.findByPriorityAndIsActiveTrueAndPublishDateBefore(priority, now, pageable);
    }
    
    @Override
    public List<Update> findByIsPinnedTrueAndIsActiveTrueAndPublishDateBeforeOrderByPublishDateDesc(LocalDateTime now) {
        return updateJpaRepository.findByIsPinnedTrueAndIsActiveTrueAndPublishDateBeforeOrderByPublishDateDesc(now);
    }
    
    @Override
    public List<Update> findTop10ByIsActiveTrueAndPublishDateBeforeOrderByViewCountDesc(LocalDateTime now) {
        return updateJpaRepository.findTop10ByIsActiveTrueAndPublishDateBeforeOrderByViewCountDesc(now);
    }
    
    @Override
    public List<Update> findTop5ByIsActiveTrueAndPublishDateBeforeOrderByPublishDateDesc(LocalDateTime now) {
        return updateJpaRepository.findTop5ByIsActiveTrueAndPublishDateBeforeOrderByPublishDateDesc(now);
    }
    
    @Override
    public Page<Update> findByTitleContainingIgnoreCaseAndIsActiveTrueAndPublishDateBefore(String title, LocalDateTime now, Pageable pageable) {
        return updateJpaRepository.findByTitleContainingIgnoreCaseAndIsActiveTrueAndPublishDateBefore(title, now, pageable);
    }
    
    @Override
    public Page<Update> findByContentContainingIgnoreCaseAndIsActiveTrueAndPublishDateBefore(String content, LocalDateTime now, Pageable pageable) {
        return updateJpaRepository.findByContentContainingIgnoreCaseAndIsActiveTrueAndPublishDateBefore(content, now, pageable);
    }
    
    @Override
    public Page<Update> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseAndIsActiveTrueAndPublishDateBefore(
        String title, String content, LocalDateTime now, Pageable pageable) {
        return updateJpaRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseAndIsActiveTrueAndPublishDateBefore(
            title, content, now, pageable);
    }
    
    @Override
    public Page<Update> findByTagsContainingAndIsActiveTrueAndPublishDateBefore(String tag, LocalDateTime now, Pageable pageable) {
        return updateJpaRepository.findByTagsContainingAndIsActiveTrueAndPublishDateBefore(tag, now, pageable);
    }
    
    @Override
    public long countByType(Update.UpdateType type) {
        return updateJpaRepository.countByType(type);
    }
    
    @Override
    public long countByPriority(Update.UpdatePriority priority) {
        return updateJpaRepository.countByPriority(priority);
    }
    
    @Override
    public long countByIsActiveTrue() {
        return updateJpaRepository.countByIsActiveTrue();
    }
    
    @Override
    public long countByIsActiveFalse() {
        return updateJpaRepository.countByIsActiveFalse();
    }
    
    @Override
    public long countByIsPinnedTrue() {
        return updateJpaRepository.countByIsPinnedTrue();
    }
    
    @Override
    public long countByPublishDateAfter(LocalDateTime date) {
        return updateJpaRepository.countByPublishDateAfter(date);
    }
    
    @Override
    public List<Update> findByAuthor_Uuid(UUID authorUuid) {
        return updateJpaRepository.findByAuthor_Uuid(authorUuid);
    }
    
    @Override
    public Page<Update> findByAuthor_Uuid(UUID authorUuid, Pageable pageable) {
        return updateJpaRepository.findByAuthor_Uuid(authorUuid, pageable);
    }
    
    @Override
    public boolean existsById(UUID id) {
        return updateJpaRepository.existsById(id);
    }
    
    @Override
    public long count() {
        return updateJpaRepository.count();
    }
    
    @Override
    public void deleteById(UUID id) {
        updateJpaRepository.deleteById(id);
    }
    
    @Override
    public void delete(Update entity) {
        updateJpaRepository.delete(entity);
    }
    
    @Override
    public void deleteAll() {
        updateJpaRepository.deleteAll();
    }
}