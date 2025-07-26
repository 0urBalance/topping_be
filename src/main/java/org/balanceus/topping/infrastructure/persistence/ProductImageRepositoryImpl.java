package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.ProductImage;
import org.balanceus.topping.domain.repository.ProductImageRepository;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductImageRepositoryImpl implements ProductImageRepository {

    private final ProductImageJpaRepository productImageJpaRepository;

    @Override
    public Optional<ProductImage> findByUuid(UUID uuid) {
        return productImageJpaRepository.findByUuid(uuid);
    }

    @Override
    public List<ProductImage> findByProduct(Product product) {
        return productImageJpaRepository.findByProduct(product);
    }

    @Override
    public long countByProduct(Product product) {
        return productImageJpaRepository.countByProduct(product);
    }

    @Override
    public <S extends ProductImage> S save(S entity) {
        return productImageJpaRepository.save(entity);
    }

    @Override
    public <S extends ProductImage> List<S> saveAll(Iterable<S> entities) {
        return productImageJpaRepository.saveAll(entities);
    }

    @Override
    public Optional<ProductImage> findById(UUID id) {
        return productImageJpaRepository.findById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return productImageJpaRepository.existsById(id);
    }

    @Override
    public List<ProductImage> findAll() {
        return productImageJpaRepository.findAll();
    }

    @Override
    public List<ProductImage> findAllById(Iterable<UUID> ids) {
        return productImageJpaRepository.findAllById(ids);
    }

    @Override
    public long count() {
        return productImageJpaRepository.count();
    }

    @Override
    public void deleteById(UUID id) {
        productImageJpaRepository.deleteById(id);
    }

    @Override
    public void deleteByUuid(UUID uuid) {
        productImageJpaRepository.deleteByUuid(uuid);
    }

    @Override
    public void delete(ProductImage entity) {
        productImageJpaRepository.delete(entity);
    }

    @Override
    public void deleteAllById(Iterable<? extends UUID> ids) {
        productImageJpaRepository.deleteAllById(ids);
    }

    @Override
    public void deleteAll(Iterable<? extends ProductImage> entities) {
        productImageJpaRepository.deleteAll(entities);
    }

    @Override
    public void deleteAll() {
        productImageJpaRepository.deleteAll();
    }
}