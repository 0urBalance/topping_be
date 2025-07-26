package org.balanceus.topping.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequestDto {
    
    @NotBlank(message = "상품명은 필수입니다")
    @Size(max = 255, message = "상품명은 255자를 초과할 수 없습니다")
    private String name;
    
    @Size(max = 2000, message = "상품 설명은 2000자를 초과할 수 없습니다")
    private String description;
    
    @NotNull(message = "가격은 필수입니다")
    @DecimalMin(value = "0.0", inclusive = false, message = "가격은 0보다 커야 합니다")
    private BigDecimal price;
    
    @NotBlank(message = "카테고리는 필수입니다")
    private String category;
    
    @Size(max = 500, message = "썸네일 경로는 500자를 초과할 수 없습니다")
    private String thumbnailPath;
    
    @NotNull(message = "연결된 스토어가 필요합니다")
    private UUID storeId;
    
    // Helper method to get product type from category
    public String getProductType() {
        if ("signature".equalsIgnoreCase(category)) {
            return "SIGNATURE";
        } else if ("popular".equalsIgnoreCase(category)) {
            return "COLLABORATION";
        } else {
            return "REGULAR";
        }
    }
    
    // Helper method to get product category enum
    public String getProductCategory() {
        if ("signature".equalsIgnoreCase(category)) {
            return "SIGNATURE";
        } else if ("popular".equalsIgnoreCase(category)) {
            return "POPULAR";
        } else {
            return "COLLABORATION";
        }
    }
}