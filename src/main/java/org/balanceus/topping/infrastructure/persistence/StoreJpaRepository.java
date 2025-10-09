package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.StoreCategory;
import org.balanceus.topping.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreJpaRepository extends JpaRepository<Store, UUID> {
    Optional<Store> findByUser(User user);
    Optional<Store> findByUserUuid(UUID userUuid);
    Optional<Store> findByName(String name);
    boolean existsByName(String name);
    boolean existsByUser(User user);
    
    List<Store> findByCategory(StoreCategory category);
    List<Store> findAllByOrderByNameAsc();
    List<Store> findByCategoryOrderByNameAsc(StoreCategory category);
    
    @Query("SELECT s FROM Store s " +
           "LEFT JOIN FETCH s.products p " +
           "WHERE s.uuid = :id")
    Optional<Store> findByIdWithProducts(@Param("id") UUID id);
    
    @Query("SELECT s FROM Store s " +
           "LEFT JOIN FETCH s.images " +
           "WHERE s.uuid = :id")
    Optional<Store> findByIdWithImages(@Param("id") UUID id);
    
    // Search methods
    List<Store> findByNameContainingIgnoreCase(String name);
    List<Store> findByAddressContainingIgnoreCase(String address);
    List<Store> findByDescriptionContainingIgnoreCase(String description);
    List<Store> findByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
        String name, String address, String description);
    Page<Store> findByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
        String name, String address, String description, Pageable pageable);
}