package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.StoreLike;
import org.balanceus.topping.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreLikeJpaRepository extends JpaRepository<StoreLike, UUID> {
    Optional<StoreLike> findByUserAndStore(User user, Store store);
    List<StoreLike> findByUser(User user);
    long countByStore(Store store);
    boolean existsByUserAndStore(User user, Store store);
}