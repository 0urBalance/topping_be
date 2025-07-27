package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.balanceus.topping.domain.model.Review;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewJpaRepository extends JpaRepository<Review, UUID> {
    
    List<Review> findByStore(Store store);
    
    List<Review> findByUser(User user);
    
    Page<Review> findByStore(Store store, Pageable pageable);
    
    List<Review> findByStoreAndIsActiveTrue(Store store);
    
    // Statistics methods
    long countByStore(Store store);
    
    long countByStoreAndIsActiveTrue(Store store);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.store = :store")
    Double findAverageRatingByStore(@Param("store") Store store);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.store = :store AND r.isActive = true")
    Double findAverageRatingByStoreAndIsActiveTrue(@Param("store") Store store);
    
    boolean existsByUserAndStore(User user, Store store);
}