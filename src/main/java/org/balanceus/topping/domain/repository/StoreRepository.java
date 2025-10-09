package org.balanceus.topping.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.StoreCategory;
import org.balanceus.topping.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StoreRepository {
    Optional<Store> findByUser(User user);
    
    Optional<Store> findByUserUuid(UUID userUuid);
    
    Optional<Store> findByName(String name);
    
    boolean existsByName(String name);
    
    boolean existsByUser(User user);
    
    <S extends Store> S save(S entity);
    
    <S extends Store> List<S> saveAll(Iterable<S> entities);
    
    Optional<Store> findById(UUID id);
    
    Optional<Store> findByIdWithProductsAndTags(UUID id);
    
    boolean existsById(UUID id);
    
    List<Store> findAll(Pageable pageable);
    
    List<Store> findByCategory(StoreCategory category);
    
    List<Store> findAllByOrderByNameAsc();
    
    List<Store> findByCategoryOrderByNameAsc(StoreCategory category);
    
    List<Store> findAllById(Iterable<UUID> ids);
    
    long count();
    
    void deleteById(UUID id);
    
    void delete(Store entity);
    
    void deleteAllById(Iterable<? extends UUID> ids);
    
    void deleteAll(Iterable<? extends Store> entities);
    
    void deleteAll();
    
    // Search methods
    List<Store> findByNameContainingIgnoreCase(String name);
    List<Store> findByAddressContainingIgnoreCase(String address);
    List<Store> findByDescriptionContainingIgnoreCase(String description);
    List<Store> findByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
        String name, String address, String description);
    Page<Store> findByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
        String name, String address, String description, Pageable pageable);
}