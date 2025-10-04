package org.balanceus.topping.infrastructure.persistence;

import org.balanceus.topping.domain.model.Update;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface UpdateJpaRepository extends JpaRepository<Update, UUID> {
    
    // Active updates
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
    
    // Count operations
    long countByType(Update.UpdateType type);
    long countByPriority(Update.UpdatePriority priority);
    long countByIsActiveTrue();
    long countByIsActiveFalse();
    long countByIsPinnedTrue();
    long countByPublishDateAfter(LocalDateTime date);
    
    // Author-based queries
    List<Update> findByAuthor_Uuid(UUID authorUuid);
    Page<Update> findByAuthor_Uuid(UUID authorUuid, Pageable pageable);
}