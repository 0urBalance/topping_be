package org.balanceus.topping.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequestDto {
    
    @NotBlank(message = "상품 제목은 필수입니다")
    @Size(max = 255, message = "상품 제목은 255자를 초과할 수 없습니다")
    private String title;
    
    @NotBlank(message = "상품 설명은 필수입니다")
    @Size(max = 2000, message = "상품 설명은 2000자를 초과할 수 없습니다")
    private String description;
    
    @NotBlank(message = "카테고리는 필수입니다")
    private String category;
    
    @Size(max = 500, message = "이미지 URL은 500자를 초과할 수 없습니다")
    private String imageUrl;
}