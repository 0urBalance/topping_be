package org.balanceus.topping.application.dto;

import java.util.List;

import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.Store;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreDetailView {
    private final Store store;
    private final boolean owner;
    private final List<Product> popularMenus;
    private final List<Product> signatureMenus;
    private final long likeCount;
    private final long wishlistCount;
    private final boolean liked;
    private final boolean wishlisted;
    private final long reviewCount;
    private final double rating;
    private final long collaborationProductCount;
    private final List<Store> collaboratingStores;
}
