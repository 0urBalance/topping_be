package org.balanceus.topping.domain.repository;

import java.util.UUID;

import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.ProductWishlist;
import org.balanceus.topping.domain.model.User;

public interface ProductWishlistRepository {
    
    ProductWishlist save(ProductWishlist productWishlist);
    
    void delete(ProductWishlist productWishlist);
    
    boolean existsByUserAndProduct(User user, Product product);
    
    ProductWishlist findByUserAndProduct(User user, Product product);
    
    long countByProduct(Product product);
    
    void deleteByUserUuidAndProductUuid(UUID userUuid, UUID productUuid);
}