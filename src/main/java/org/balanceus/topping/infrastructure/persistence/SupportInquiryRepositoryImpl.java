package org.balanceus.topping.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.balanceus.topping.domain.model.SupportInquiry;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.SupportInquiryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SupportInquiryRepositoryImpl implements SupportInquiryRepository {
    
    private final SupportInquiryJpaRepository supportInquiryJpaRepository;
    
    @Override
    public <S extends SupportInquiry> S save(S entity) {
        return supportInquiryJpaRepository.save(entity);
    }
    
    @Override
    public <S extends SupportInquiry> List<S> saveAll(Iterable<S> entities) {
        return supportInquiryJpaRepository.saveAll(entities);
    }
    
    @Override
    public Optional<SupportInquiry> findById(UUID id) {
        return supportInquiryJpaRepository.findById(id);
    }
    
    @Override
    public List<SupportInquiry> findAll() {
        return supportInquiryJpaRepository.findAll();
    }
    
    @Override
    public Page<SupportInquiry> findAll(Pageable pageable) {
        return supportInquiryJpaRepository.findAll(pageable);
    }
    
    @Override
    public List<SupportInquiry> findByUser(User user) {
        return supportInquiryJpaRepository.findByUser(user);
    }
    
    @Override
    public Page<SupportInquiry> findByUser(User user, Pageable pageable) {
        return supportInquiryJpaRepository.findByUser(user, pageable);
    }
    
    @Override
    public List<SupportInquiry> findByUserOrderByCreatedAtDesc(User user) {
        return supportInquiryJpaRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    @Override
    public Page<SupportInquiry> findByUserOrderByCreatedAtDesc(User user, Pageable pageable) {
        return supportInquiryJpaRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }
    
    @Override
    public List<SupportInquiry> findByStatus(SupportInquiry.InquiryStatus status) {
        return supportInquiryJpaRepository.findByStatus(status);
    }
    
    @Override
    public Page<SupportInquiry> findByStatus(SupportInquiry.InquiryStatus status, Pageable pageable) {
        return supportInquiryJpaRepository.findByStatus(status, pageable);
    }
    
    @Override
    public List<SupportInquiry> findByCategory(SupportInquiry.InquiryCategory category) {
        return supportInquiryJpaRepository.findByCategory(category);
    }
    
    @Override
    public Page<SupportInquiry> findByCategory(SupportInquiry.InquiryCategory category, Pageable pageable) {
        return supportInquiryJpaRepository.findByCategory(category, pageable);
    }
    
    @Override
    public Page<SupportInquiry> findAllByOrderByCreatedAtDesc(Pageable pageable) {
        return supportInquiryJpaRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
    
    @Override
    public Page<SupportInquiry> findByTitleContainingIgnoreCase(String title, Pageable pageable) {
        return supportInquiryJpaRepository.findByTitleContainingIgnoreCase(title, pageable);
    }
    
    @Override
    public Page<SupportInquiry> findByContentContainingIgnoreCase(String content, Pageable pageable) {
        return supportInquiryJpaRepository.findByContentContainingIgnoreCase(content, pageable);
    }
    
    @Override
    public Page<SupportInquiry> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
        String title, String content, Pageable pageable) {
        return supportInquiryJpaRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
            title, content, pageable);
    }
    
    @Override
    public long countByStatus(SupportInquiry.InquiryStatus status) {
        return supportInquiryJpaRepository.countByStatus(status);
    }
    
    @Override
    public long countByCategory(SupportInquiry.InquiryCategory category) {
        return supportInquiryJpaRepository.countByCategory(category);
    }
    
    @Override
    public boolean existsById(UUID id) {
        return supportInquiryJpaRepository.existsById(id);
    }
    
    @Override
    public long count() {
        return supportInquiryJpaRepository.count();
    }
    
    @Override
    public void deleteById(UUID id) {
        supportInquiryJpaRepository.deleteById(id);
    }
    
    @Override
    public void delete(SupportInquiry entity) {
        supportInquiryJpaRepository.delete(entity);
    }
    
    @Override
    public void deleteAll() {
        supportInquiryJpaRepository.deleteAll();
    }
}