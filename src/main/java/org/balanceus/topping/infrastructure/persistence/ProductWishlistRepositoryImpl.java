package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.ProductWishlist;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.ProductWishlistRepository;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductWishlistRepositoryImpl implements ProductWishlistRepository {

    private final ProductWishlistJpaRepository productWishlistJpaRepository;

    @Override
    public ProductWishlist save(ProductWishlist productWishlist) {
        return productWishlistJpaRepository.save(productWishlist);
    }

    @Override
    public void delete(ProductWishlist productWishlist) {
        productWishlistJpaRepository.delete(productWishlist);
    }

    @Override
    public boolean existsByUserAndProduct(User user, Product product) {
        return productWishlistJpaRepository.existsByUserAndProduct(user, product);
    }

    @Override
    public ProductWishlist findByUserAndProduct(User user, Product product) {
        return productWishlistJpaRepository.findByUserAndProduct(user, product);
    }

    @Override
    public List<ProductWishlist> findByUser(User user) {
        return productWishlistJpaRepository.findByUser(user);
    }

    @Override
    public long countByProduct(Product product) {
        return productWishlistJpaRepository.countByProduct(product);
    }

    @Override
    public void deleteByUserUuidAndProductUuid(UUID userUuid, UUID productUuid) {
        productWishlistJpaRepository.deleteByUserUuidAndProductUuid(userUuid, productUuid);
    }
}