package org.balanceus.topping.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.balanceus.topping.domain.model.FAQ;
import org.balanceus.topping.domain.repository.FAQRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FAQRepositoryImpl implements FAQRepository {
    
    private final FAQJpaRepository faqJpaRepository;
    
    @Override
    public <S extends FAQ> S save(S entity) {
        return faqJpaRepository.save(entity);
    }
    
    @Override
    public <S extends FAQ> List<S> saveAll(Iterable<S> entities) {
        return faqJpaRepository.saveAll(entities);
    }
    
    @Override
    public Optional<FAQ> findById(UUID id) {
        return faqJpaRepository.findById(id);
    }
    
    @Override
    public List<FAQ> findAll() {
        return faqJpaRepository.findAll();
    }
    
    @Override
    public Page<FAQ> findAll(Pageable pageable) {
        return faqJpaRepository.findAll(pageable);
    }
    
    @Override
    public List<FAQ> findByIsActiveTrue() {
        return faqJpaRepository.findByIsActiveTrue();
    }
    
    @Override
    public Page<FAQ> findByIsActiveTrue(Pageable pageable) {
        return faqJpaRepository.findByIsActiveTrue(pageable);
    }
    
    @Override
    public List<FAQ> findByIsActiveTrueOrderBySortOrderAsc() {
        return faqJpaRepository.findByIsActiveTrueOrderBySortOrderAsc();
    }
    
    @Override
    public Page<FAQ> findByIsActiveTrueOrderBySortOrderAsc(Pageable pageable) {
        return faqJpaRepository.findByIsActiveTrueOrderBySortOrderAsc(pageable);
    }
    
    @Override
    public List<FAQ> findByCategory(FAQ.FAQCategory category) {
        return faqJpaRepository.findByCategory(category);
    }
    
    @Override
    public Page<FAQ> findByCategory(FAQ.FAQCategory category, Pageable pageable) {
        return faqJpaRepository.findByCategory(category, pageable);
    }
    
    @Override
    public List<FAQ> findByCategoryAndIsActiveTrue(FAQ.FAQCategory category) {
        return faqJpaRepository.findByCategoryAndIsActiveTrue(category);
    }
    
    @Override
    public Page<FAQ> findByCategoryAndIsActiveTrue(FAQ.FAQCategory category, Pageable pageable) {
        return faqJpaRepository.findByCategoryAndIsActiveTrue(category, pageable);
    }
    
    @Override
    public List<FAQ> findTop10ByIsActiveTrueOrderByViewCountDesc() {
        return faqJpaRepository.findTop10ByIsActiveTrueOrderByViewCountDesc();
    }
    
    @Override
    public Page<FAQ> findByQuestionContainingIgnoreCaseAndIsActiveTrue(String question, Pageable pageable) {
        return faqJpaRepository.findByQuestionContainingIgnoreCaseAndIsActiveTrue(question, pageable);
    }
    
    @Override
    public Page<FAQ> findByAnswerContainingIgnoreCaseAndIsActiveTrue(String answer, Pageable pageable) {
        return faqJpaRepository.findByAnswerContainingIgnoreCaseAndIsActiveTrue(answer, pageable);
    }
    
    @Override
    public Page<FAQ> findByQuestionContainingIgnoreCaseOrAnswerContainingIgnoreCaseAndIsActiveTrue(
        String question, String answer, Pageable pageable) {
        return faqJpaRepository.findByQuestionContainingIgnoreCaseOrAnswerContainingIgnoreCaseAndIsActiveTrue(
            question, answer, pageable);
    }
    
    @Override
    public long countByCategory(FAQ.FAQCategory category) {
        return faqJpaRepository.countByCategory(category);
    }
    
    @Override
    public long countByIsActiveTrue() {
        return faqJpaRepository.countByIsActiveTrue();
    }
    
    @Override
    public long countByIsActiveFalse() {
        return faqJpaRepository.countByIsActiveFalse();
    }
    
    @Override
    public boolean existsById(UUID id) {
        return faqJpaRepository.existsById(id);
    }
    
    @Override
    public long count() {
        return faqJpaRepository.count();
    }
    
    @Override
    public void deleteById(UUID id) {
        faqJpaRepository.deleteById(id);
    }
    
    @Override
    public void delete(FAQ entity) {
        faqJpaRepository.delete(entity);
    }
    
    @Override
    public void deleteAll() {
        faqJpaRepository.deleteAll();
    }
}