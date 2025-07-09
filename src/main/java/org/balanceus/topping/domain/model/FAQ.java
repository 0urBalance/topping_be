package org.balanceus.topping.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "faqs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FAQ {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String question;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FAQCategory category;
    
    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;
    
    @Column(nullable = false)
    @Builder.Default
    private int sortOrder = 0;
    
    @Column(nullable = false)
    @Builder.Default
    private int viewCount = 0;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    public enum FAQCategory {
        GENERAL("일반"),
        ACCOUNT("계정"),
        COLLABORATION("협업"),
        PAYMENT("결제"),
        TECHNICAL("기술");
        
        private final String displayName;
        
        FAQCategory(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}