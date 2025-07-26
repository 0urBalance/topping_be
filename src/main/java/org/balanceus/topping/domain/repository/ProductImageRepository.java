package org.balanceus.topping.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.ProductImage;

public interface ProductImageRepository {
    Optional<ProductImage> findByUuid(UUID uuid);
    
    List<ProductImage> findByProduct(Product product);
    
    long countByProduct(Product product);
    
    <S extends ProductImage> S save(S entity);
    
    <S extends ProductImage> List<S> saveAll(Iterable<S> entities);
    
    Optional<ProductImage> findById(UUID id);
    
    boolean existsById(UUID id);
    
    List<ProductImage> findAll();
    
    List<ProductImage> findAllById(Iterable<UUID> ids);
    
    long count();
    
    void deleteById(UUID id);
    
    void deleteByUuid(UUID uuid);
    
    void delete(ProductImage entity);
    
    void deleteAllById(Iterable<? extends UUID> ids);
    
    void deleteAll(Iterable<? extends ProductImage> entities);
    
    void deleteAll();
}