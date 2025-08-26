package org.balanceus.topping.infrastructure.persistence;

import java.util.UUID;

import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.ProductWishlist;
import org.balanceus.topping.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductWishlistJpaRepository extends JpaRepository<ProductWishlist, UUID> {
    
    boolean existsByUserAndProduct(User user, Product product);
    
    ProductWishlist findByUserAndProduct(User user, Product product);
    
    long countByProduct(Product product);
    
    void deleteByUserUuidAndProductUuid(UUID userUuid, UUID productUuid);
}