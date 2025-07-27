package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.Review;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepository {

    private final ReviewJpaRepository reviewJpaRepository;

    @Override
    public Review save(Review review) {
        return reviewJpaRepository.save(review);
    }

    @Override
    public Optional<Review> findById(UUID id) {
        return reviewJpaRepository.findById(id);
    }

    @Override
    public List<Review> findAll() {
        return reviewJpaRepository.findAll();
    }

    @Override
    public List<Review> findByStore(Store store) {
        return reviewJpaRepository.findByStore(store);
    }

    @Override
    public List<Review> findByUser(User user) {
        return reviewJpaRepository.findByUser(user);
    }

    @Override
    public Page<Review> findByStore(Store store, Pageable pageable) {
        return reviewJpaRepository.findByStore(store, pageable);
    }

    @Override
    public List<Review> findByStoreAndIsActiveTrue(Store store) {
        return reviewJpaRepository.findByStoreAndIsActiveTrue(store);
    }

    @Override
    public long countByStore(Store store) {
        return reviewJpaRepository.countByStore(store);
    }

    @Override
    public long countByStoreAndIsActiveTrue(Store store) {
        return reviewJpaRepository.countByStoreAndIsActiveTrue(store);
    }

    @Override
    public Double findAverageRatingByStore(Store store) {
        return reviewJpaRepository.findAverageRatingByStore(store);
    }

    @Override
    public Double findAverageRatingByStoreAndIsActiveTrue(Store store) {
        return reviewJpaRepository.findAverageRatingByStoreAndIsActiveTrue(store);
    }

    @Override
    public void deleteById(UUID id) {
        reviewJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByUserAndStore(User user, Store store) {
        return reviewJpaRepository.existsByUserAndStore(user, store);
    }
}