package org.balanceus.topping.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.model.Wishlist;

public interface WishlistRepository {
    Wishlist save(Wishlist wishlist);
    Optional<Wishlist> findByUserAndStore(User user, Store store);
    List<Wishlist> findByUser(User user);
    long countByStore(Store store);
    void delete(Wishlist wishlist);
    boolean existsByUserAndStore(User user, Store store);
}