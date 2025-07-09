package org.balanceus.topping.domain.repository;

import org.balanceus.topping.domain.model.FAQ;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FAQRepository {
    
    // Create/Update operations
    <S extends FAQ> S save(S entity);
    <S extends FAQ> List<S> saveAll(Iterable<S> entities);
    
    // Read operations
    Optional<FAQ> findById(UUID id);
    List<FAQ> findAll();
    Page<FAQ> findAll(Pageable pageable);
    
    // Custom business queries
    List<FAQ> findByIsActiveTrue();
    Page<FAQ> findByIsActiveTrue(Pageable pageable);
    List<FAQ> findByIsActiveTrueOrderBySortOrderAsc();
    Page<FAQ> findByIsActiveTrueOrderBySortOrderAsc(Pageable pageable);
    
    List<FAQ> findByCategory(FAQ.FAQCategory category);
    Page<FAQ> findByCategory(FAQ.FAQCategory category, Pageable pageable);
    List<FAQ> findByCategoryAndIsActiveTrue(FAQ.FAQCategory category);
    Page<FAQ> findByCategoryAndIsActiveTrue(FAQ.FAQCategory category, Pageable pageable);
    
    // Popular FAQs
    List<FAQ> findTop10ByIsActiveTrueOrderByViewCountDesc();
    
    // Search functionality
    Page<FAQ> findByQuestionContainingIgnoreCaseAndIsActiveTrue(String question, Pageable pageable);
    Page<FAQ> findByAnswerContainingIgnoreCaseAndIsActiveTrue(String answer, Pageable pageable);
    Page<FAQ> findByQuestionContainingIgnoreCaseOrAnswerContainingIgnoreCaseAndIsActiveTrue(
        String question, String answer, Pageable pageable);
    
    // Admin queries
    long countByCategory(FAQ.FAQCategory category);
    long countByIsActiveTrue();
    long countByIsActiveFalse();
    
    // Existence checks
    boolean existsById(UUID id);
    
    // Count operations
    long count();
    
    // Delete operations
    void deleteById(UUID id);
    void delete(FAQ entity);
    void deleteAll();
}