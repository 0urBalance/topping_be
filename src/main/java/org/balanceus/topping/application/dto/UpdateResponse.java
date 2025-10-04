package org.balanceus.topping.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.balanceus.topping.domain.model.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateResponse {
    
    private UUID id;
    private String title;
    private String content;
    private Update.UpdateType type;
    private String typeDisplayName;
    private Update.UpdatePriority priority;
    private String priorityDisplayName;
    private boolean isActive;
    private boolean isPinned;
    private LocalDateTime publishDate;
    private String authorName;
    private UUID authorUuid;
    private int viewCount;
    private String imageUrl;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static UpdateResponse from(Update update) {
        return UpdateResponse.builder()
            .id(update.getId())
            .title(update.getTitle())
            .content(update.getContent())
            .type(update.getType())
            .typeDisplayName(update.getType().getDisplayName())
            .priority(update.getPriority())
            .priorityDisplayName(update.getPriority().getDisplayName())
            .isActive(update.isActive())
            .isPinned(update.isPinned())
            .publishDate(update.getPublishDate())
            .authorName(update.getAuthor() != null ? update.getAuthor().getUsername() : "Unknown")
            .authorUuid(update.getAuthor() != null ? update.getAuthor().getUuid() : null)
            .viewCount(update.getViewCount())
            .imageUrl(update.getImageUrl())
            .tags(update.getTags())
            .createdAt(update.getCreatedAt())
            .updatedAt(update.getUpdatedAt())
            .build();
    }
}