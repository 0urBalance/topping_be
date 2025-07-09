package org.balanceus.topping.domain.repository;

import org.balanceus.topping.domain.model.SupportInquiry;
import org.balanceus.topping.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupportInquiryRepository {
    
    // Create/Update operations
    <S extends SupportInquiry> S save(S entity);
    <S extends SupportInquiry> List<S> saveAll(Iterable<S> entities);
    
    // Read operations
    Optional<SupportInquiry> findById(UUID id);
    List<SupportInquiry> findAll();
    Page<SupportInquiry> findAll(Pageable pageable);
    
    // Custom business queries
    List<SupportInquiry> findByUser(User user);
    Page<SupportInquiry> findByUser(User user, Pageable pageable);
    List<SupportInquiry> findByUserOrderByCreatedAtDesc(User user);
    Page<SupportInquiry> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    List<SupportInquiry> findByStatus(SupportInquiry.InquiryStatus status);
    Page<SupportInquiry> findByStatus(SupportInquiry.InquiryStatus status, Pageable pageable);
    
    List<SupportInquiry> findByCategory(SupportInquiry.InquiryCategory category);
    Page<SupportInquiry> findByCategory(SupportInquiry.InquiryCategory category, Pageable pageable);
    
    Page<SupportInquiry> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // Search functionality
    Page<SupportInquiry> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<SupportInquiry> findByContentContainingIgnoreCase(String content, Pageable pageable);
    Page<SupportInquiry> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
        String title, String content, Pageable pageable);
    
    // Admin queries
    long countByStatus(SupportInquiry.InquiryStatus status);
    long countByCategory(SupportInquiry.InquiryCategory category);
    
    // Existence checks
    boolean existsById(UUID id);
    
    // Count operations
    long count();
    
    // Delete operations
    void deleteById(UUID id);
    void delete(SupportInquiry entity);
    void deleteAll();
}