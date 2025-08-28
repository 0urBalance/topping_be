package org.balanceus.topping.domain.repository;

import java.util.List;
import java.util.UUID;

import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.ProductWishlist;
import org.balanceus.topping.domain.model.User;

public interface ProductWishlistRepository {
    
    ProductWishlist save(ProductWishlist productWishlist);
    
    void delete(ProductWishlist productWishlist);
    
    boolean existsByUserAndProduct(User user, Product product);
    
    ProductWishlist findByUserAndProduct(User user, Product product);
    
    List<ProductWishlist> findByUser(User user);
    
    long countByProduct(Product product);
    
    void deleteByUserUuidAndProductUuid(UUID userUuid, UUID productUuid);
}