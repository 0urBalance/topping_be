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
public class UpdateSummaryDto {
    
    private UUID id;
    private String title;
    private String summary; // 내용의 요약 (처음 100자)
    private Update.UpdateType type;
    private String typeDisplayName;
    private Update.UpdatePriority priority;
    private String priorityDisplayName;
    private boolean isPinned;
    private LocalDateTime publishDate;
    private String authorName;
    private int viewCount;
    private String imageUrl;
    private List<String> tags;
    
    public static UpdateSummaryDto from(Update update) {
        String summary = update.getContent();
        if (summary != null && summary.length() > 100) {
            summary = summary.substring(0, 100) + "...";
        }
        
        return UpdateSummaryDto.builder()
            .id(update.getId())
            .title(update.getTitle())
            .summary(summary)
            .type(update.getType())
            .typeDisplayName(update.getType().getDisplayName())
            .priority(update.getPriority())
            .priorityDisplayName(update.getPriority().getDisplayName())
            .isPinned(update.isPinned())
            .publishDate(update.getPublishDate())
            .authorName(update.getAuthor() != null ? update.getAuthor().getUsername() : "Unknown")
            .viewCount(update.getViewCount())
            .imageUrl(update.getImageUrl())
            .tags(update.getTags())
            .build();
    }
}