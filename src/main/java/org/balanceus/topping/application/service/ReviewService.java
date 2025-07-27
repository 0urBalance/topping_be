package org.balanceus.topping.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.Review;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public Review createReview(Store store, User user, Integer rating, String content) {
        // Check if user already reviewed this store
        if (user != null && reviewRepository.existsByUserAndStore(user, store)) {
            throw new IllegalStateException("User has already reviewed this store");
        }

        Review review = new Review();
        review.setStore(store);
        review.setUser(user);
        review.setRating(rating);
        review.setContent(content);
        review.setIsActive(true);

        Review savedReview = reviewRepository.save(review);
        log.info("Review created for store {} by user {}", store.getName(), 
                user != null ? user.getUsername() : "anonymous");
        
        return savedReview;
    }

    @Transactional(readOnly = true)
    public List<Review> getActiveReviewsByStore(Store store) {
        return reviewRepository.findByStoreAndIsActiveTrue(store);
    }

    @Transactional(readOnly = true)
    public Page<Review> getReviewsByStore(Store store, Pageable pageable) {
        return reviewRepository.findByStore(store, pageable);
    }

    @Transactional(readOnly = true)
    public long getReviewCount(Store store) {
        return reviewRepository.countByStoreAndIsActiveTrue(store);
    }

    @Transactional(readOnly = true)
    public double getAverageRating(Store store) {
        Double average = reviewRepository.findAverageRatingByStoreAndIsActiveTrue(store);
        return average != null ? Math.round(average * 10.0) / 10.0 : 0.0;
    }

    public Review updateReview(UUID reviewId, Integer rating, String content) {
        Optional<Review> reviewOptional = reviewRepository.findById(reviewId);
        if (reviewOptional.isEmpty()) {
            throw new IllegalArgumentException("Review not found");
        }

        Review review = reviewOptional.get();
        review.setRating(rating);
        review.setContent(content);
        
        return reviewRepository.save(review);
    }

    public void deleteReview(UUID reviewId) {
        Optional<Review> reviewOptional = reviewRepository.findById(reviewId);
        if (reviewOptional.isEmpty()) {
            throw new IllegalArgumentException("Review not found");
        }

        Review review = reviewOptional.get();
        review.setIsActive(false); // Soft delete
        reviewRepository.save(review);
        
        log.info("Review {} soft deleted", reviewId);
    }

    @Transactional(readOnly = true)
    public boolean hasUserReviewed(User user, Store store) {
        return user != null && reviewRepository.existsByUserAndStore(user, store);
    }
}