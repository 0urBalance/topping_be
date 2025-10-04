package org.balanceus.topping.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "updates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Update {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UpdateType type;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UpdatePriority priority = UpdatePriority.NORMAL;
    
    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;
    
    @Column(nullable = false)
    @Builder.Default
    private boolean isPinned = false;
    
    @Column(nullable = false)
    private LocalDateTime publishDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_uuid")
    private User author;
    
    @Column(nullable = false)
    @Builder.Default
    private int viewCount = 0;
    
    private String imageUrl;
    
    @ElementCollection
    @CollectionTable(name = "update_tags", joinColumns = @JoinColumn(name = "update_id"))
    @Column(name = "tag")
    private List<String> tags;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    public enum UpdateType {
        ANNOUNCEMENT("공지사항"),
        FEATURE_UPDATE("기능 업데이트"),
        MAINTENANCE("시스템 점검"),
        POLICY_CHANGE("정책 변경"),
        SUCCESS_STORY("성공 사례"),
        EDUCATION("교육 컨텐츠");
        
        private final String displayName;
        
        UpdateType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum UpdatePriority {
        HIGH("높음"),
        NORMAL("보통"),
        LOW("낮음");
        
        private final String displayName;
        
        UpdatePriority(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public void incrementViewCount() {
        this.viewCount++;
    }
}