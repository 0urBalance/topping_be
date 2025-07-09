package org.balanceus.topping.infrastructure.persistence;

import org.balanceus.topping.domain.model.FAQ;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FAQJpaRepository extends JpaRepository<FAQ, UUID> {
    
    List<FAQ> findByIsActiveTrue();
    Page<FAQ> findByIsActiveTrue(Pageable pageable);
    List<FAQ> findByIsActiveTrueOrderBySortOrderAsc();
    Page<FAQ> findByIsActiveTrueOrderBySortOrderAsc(Pageable pageable);
    
    List<FAQ> findByCategory(FAQ.FAQCategory category);
    Page<FAQ> findByCategory(FAQ.FAQCategory category, Pageable pageable);
    List<FAQ> findByCategoryAndIsActiveTrue(FAQ.FAQCategory category);
    Page<FAQ> findByCategoryAndIsActiveTrue(FAQ.FAQCategory category, Pageable pageable);
    
    List<FAQ> findTop10ByIsActiveTrueOrderByViewCountDesc();
    
    Page<FAQ> findByQuestionContainingIgnoreCaseAndIsActiveTrue(String question, Pageable pageable);
    Page<FAQ> findByAnswerContainingIgnoreCaseAndIsActiveTrue(String answer, Pageable pageable);
    Page<FAQ> findByQuestionContainingIgnoreCaseOrAnswerContainingIgnoreCaseAndIsActiveTrue(
        String question, String answer, Pageable pageable);
    
    long countByCategory(FAQ.FAQCategory category);
    long countByIsActiveTrue();
    long countByIsActiveFalse();
}