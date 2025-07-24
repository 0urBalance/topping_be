package org.balanceus.topping.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.StoreLike;
import org.balanceus.topping.domain.model.User;

public interface StoreLikeRepository {
    StoreLike save(StoreLike storeLike);
    Optional<StoreLike> findByUserAndStore(User user, Store store);
    List<StoreLike> findByUser(User user);
    long countByStore(Store store);
    void delete(StoreLike storeLike);
    boolean existsByUserAndStore(User user, Store store);
}