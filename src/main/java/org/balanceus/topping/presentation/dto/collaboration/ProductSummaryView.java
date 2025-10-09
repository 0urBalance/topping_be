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
public class ProductSummaryView {
    private UUID uuid;
    private String name;
    private String productType;
    private String productTypeDisplayName;
    private ProductCategoryView category;
    private BigDecimal price;
    private Boolean available;
    private UserSummaryView creator;
    private StoreSummaryView store;
}
