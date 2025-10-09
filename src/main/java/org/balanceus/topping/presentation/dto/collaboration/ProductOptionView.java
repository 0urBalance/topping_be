package org.balanceus.topping.presentation.dto.collaboration;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductOptionView {
    private UUID uuid;
    private String name;
    private String category;
    private String productType;
    private Boolean available;
    private BigDecimal price;
    private UUID storeId;
}
