package org.balanceus.topping.presentation.dto.collaboration;

import java.util.List;

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
public class CollaborationApplyViewModel {
    private UserSummaryView user;
    private boolean businessOwner;
    private StoreOptionView userStore;
    private List<ProductOptionView> userProducts;
    private List<StoreOptionView> allStores;
    private List<ProductOptionView> allProducts;
    private StoreOptionView targetStore;
    private ProductOptionView targetProduct;
    private String storeDataJson;
}
