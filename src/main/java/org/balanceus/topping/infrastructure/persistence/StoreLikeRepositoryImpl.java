package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.StoreLike;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.StoreLikeRepository;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StoreLikeRepositoryImpl implements StoreLikeRepository {

    private final StoreLikeJpaRepository storeLikeJpaRepository;

    @Override
    public StoreLike save(StoreLike storeLike) {
        return storeLikeJpaRepository.save(storeLike);
    }

    @Override
    public Optional<StoreLike> findByUserAndStore(User user, Store store) {
        return storeLikeJpaRepository.findByUserAndStore(user, store);
    }

    @Override
    public List<StoreLike> findByUser(User user) {
        return storeLikeJpaRepository.findByUser(user);
    }

    @Override
    public long countByStore(Store store) {
        return storeLikeJpaRepository.countByStore(store);
    }

    @Override
    public void delete(StoreLike storeLike) {
        storeLikeJpaRepository.delete(storeLike);
    }

    @Override
    public boolean existsByUserAndStore(User user, Store store) {
        return storeLikeJpaRepository.existsByUserAndStore(user, store);
    }
}