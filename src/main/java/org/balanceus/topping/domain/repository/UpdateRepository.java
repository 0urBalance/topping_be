package org.balanceus.topping.domain.repository;

import org.balanceus.topping.domain.model.Update;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UpdateRepository {
    
    // Create/Update operations
    <S extends Update> S save(S entity);
    <S extends Update> List<S> saveAll(Iterable<S> entities);
    
    // Read operations
    Optional<Update> findById(UUID id);
    List<Update> findAll();
    Page<Update> findAll(Pageable pageable);
    
    // Published and active updates
    List<Update> findByIsActiveTrue();
    Page<Update> findByIsActiveTrue(Pageable pageable);
    List<Update> findByIsActiveTrueAndPublishDateBeforeOrderByIsPinnedDescPublishDateDesc(LocalDateTime now);
    Page<Update> findByIsActiveTrueAndPublishDateBeforeOrderByIsPinnedDescPublishDateDesc(LocalDateTime now, Pageable pageable);
    
    // Type-based queries
    List<Update> findByType(Update.UpdateType type);
    Page<Update> findByType(Update.UpdateType type, Pageable pageable);
    List<Update> findByTypeAndIsActiveTrueAndPublishDateBefore(Update.UpdateType type, LocalDateTime now);
    Page<Update> findByTypeAndIsActiveTrueAndPublishDateBefore(Update.UpdateType type, LocalDateTime now, Pageable pageable);
    
    // Priority-based queries
    List<Update> findByPriorityAndIsActiveTrueAndPublishDateBefore(Update.UpdatePriority priority, LocalDateTime now);
    Page<Update> findByPriorityAndIsActiveTrueAndPublishDateBefore(Update.UpdatePriority priority, LocalDateTime now, Pageable pageable);
    
    // Pinned updates
    List<Update> findByIsPinnedTrueAndIsActiveTrueAndPublishDateBeforeOrderByPublishDateDesc(LocalDateTime now);
    
    // Popular updates
    List<Update> findTop10ByIsActiveTrueAndPublishDateBeforeOrderByViewCountDesc(LocalDateTime now);
    
    // Recent updates
    List<Update> findTop5ByIsActiveTrueAndPublishDateBeforeOrderByPublishDateDesc(LocalDateTime now);
    
    // Search functionality
    Page<Update> findByTitleContainingIgnoreCaseAndIsActiveTrueAndPublishDateBefore(String title, LocalDateTime now, Pageable pageable);
    Page<Update> findByContentContainingIgnoreCaseAndIsActiveTrueAndPublishDateBefore(String content, LocalDateTime now, Pageable pageable);
    Page<Update> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseAndIsActiveTrueAndPublishDateBefore(
        String title, String content, LocalDateTime now, Pageable pageable);
    
    // Tag-based queries
    Page<Update> findByTagsContainingAndIsActiveTrueAndPublishDateBefore(String tag, LocalDateTime now, Pageable pageable);
    
    // Admin queries
    long countByType(Update.UpdateType type);
    long countByPriority(Update.UpdatePriority priority);
    long countByIsActiveTrue();
    long countByIsActiveFalse();
    long countByIsPinnedTrue();
    long countByPublishDateAfter(LocalDateTime date);
    
    // Author-based queries
    List<Update> findByAuthor_Uuid(UUID authorUuid);
    Page<Update> findByAuthor_Uuid(UUID authorUuid, Pageable pageable);
    
    // Existence checks
    boolean existsById(UUID id);
    
    // Count operations
    long count();
    
    // Delete operations
    void deleteById(UUID id);
    void delete(Update entity);
    void deleteAll();
}