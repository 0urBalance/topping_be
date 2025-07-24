package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.model.Wishlist;
import org.balanceus.topping.domain.repository.WishlistRepository;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WishlistRepositoryImpl implements WishlistRepository {

    private final WishlistJpaRepository wishlistJpaRepository;

    @Override
    public Wishlist save(Wishlist wishlist) {
        return wishlistJpaRepository.save(wishlist);
    }

    @Override
    public Optional<Wishlist> findByUserAndStore(User user, Store store) {
        return wishlistJpaRepository.findByUserAndStore(user, store);
    }

    @Override
    public List<Wishlist> findByUser(User user) {
        return wishlistJpaRepository.findByUser(user);
    }

    @Override
    public long countByStore(Store store) {
        return wishlistJpaRepository.countByStore(store);
    }

    @Override
    public void delete(Wishlist wishlist) {
        wishlistJpaRepository.delete(wishlist);
    }

    @Override
    public boolean existsByUserAndStore(User user, Store store) {
        return wishlistJpaRepository.existsByUserAndStore(user, store);
    }
}