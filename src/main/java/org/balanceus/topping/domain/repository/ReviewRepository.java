package org.balanceus.topping.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.Review;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewRepository {
    
    Review save(Review review);
    
    Optional<Review> findById(UUID id);
    
    List<Review> findAll();
    
    List<Review> findByStore(Store store);
    
    List<Review> findByUser(User user);
    
    Page<Review> findByStore(Store store, Pageable pageable);
    
    List<Review> findByStoreAndIsActiveTrue(Store store);
    
    // Statistics methods
    long countByStore(Store store);
    
    long countByStoreAndIsActiveTrue(Store store);
    
    Double findAverageRatingByStore(Store store);
    
    Double findAverageRatingByStoreAndIsActiveTrue(Store store);
    
    void deleteById(UUID id);
    
    boolean existsByUserAndStore(User user, Store store);
}