package org.balanceus.topping.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreJpaRepository extends JpaRepository<Store, UUID> {
    Optional<Store> findByUser(User user);
    Optional<Store> findByUserUuid(UUID userUuid);
    Optional<Store> findByName(String name);
    boolean existsByName(String name);
    boolean existsByUser(User user);
}