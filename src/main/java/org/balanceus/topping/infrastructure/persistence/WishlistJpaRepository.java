package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistJpaRepository extends JpaRepository<Wishlist, UUID> {
    Optional<Wishlist> findByUserAndStore(User user, Store store);
    List<Wishlist> findByUser(User user);
    long countByStore(Store store);
    boolean existsByUserAndStore(User user, Store store);
}