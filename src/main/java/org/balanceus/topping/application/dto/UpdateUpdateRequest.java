package org.balanceus.topping.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.balanceus.topping.domain.model.Update;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUpdateRequest {
    
    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다")
    private String title;
    
    @NotBlank(message = "내용은 필수입니다")
    private String content;
    
    @NotNull(message = "업데이트 타입은 필수입니다")
    private Update.UpdateType type;
    
    @NotNull(message = "우선순위는 필수입니다")
    private Update.UpdatePriority priority;
    
    private boolean isActive;
    
    private boolean isPinned;
    
    private LocalDateTime publishDate;
    
    @Size(max = 500, message = "이미지 URL은 500자를 초과할 수 없습니다")
    private String imageUrl;
    
    @Size(max = 10, message = "태그는 최대 10개까지 가능합니다")
    private List<@Size(max = 50, message = "각 태그는 50자를 초과할 수 없습니다") String> tags;
}