package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductImageJpaRepository extends JpaRepository<ProductImage, UUID> {
    Optional<ProductImage> findByUuid(UUID uuid);
    List<ProductImage> findByProduct(Product product);
    long countByProduct(Product product);
    void deleteByUuid(UUID uuid);
}