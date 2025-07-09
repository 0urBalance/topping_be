package org.balanceus.topping.infrastructure.persistence;

import org.balanceus.topping.domain.model.SupportInquiry;
import org.balanceus.topping.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SupportInquiryJpaRepository extends JpaRepository<SupportInquiry, UUID> {
    
    List<SupportInquiry> findByUser(User user);
    Page<SupportInquiry> findByUser(User user, Pageable pageable);
    List<SupportInquiry> findByUserOrderByCreatedAtDesc(User user);
    Page<SupportInquiry> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    List<SupportInquiry> findByStatus(SupportInquiry.InquiryStatus status);
    Page<SupportInquiry> findByStatus(SupportInquiry.InquiryStatus status, Pageable pageable);
    
    List<SupportInquiry> findByCategory(SupportInquiry.InquiryCategory category);
    Page<SupportInquiry> findByCategory(SupportInquiry.InquiryCategory category, Pageable pageable);
    
    Page<SupportInquiry> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    Page<SupportInquiry> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<SupportInquiry> findByContentContainingIgnoreCase(String content, Pageable pageable);
    Page<SupportInquiry> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
        String title, String content, Pageable pageable);
    
    long countByStatus(SupportInquiry.InquiryStatus status);
    long countByCategory(SupportInquiry.InquiryCategory category);
}