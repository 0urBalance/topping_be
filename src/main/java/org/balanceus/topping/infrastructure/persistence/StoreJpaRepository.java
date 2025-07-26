package org.balanceus.topping.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.User;
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
    
    @Query("SELECT s FROM Store s " +
           "LEFT JOIN FETCH s.products " +
           "LEFT JOIN FETCH s.images " +
           "WHERE s.uuid = :id")
    Optional<Store> findByIdWithProductsAndTags(@Param("id") UUID id);
}