package org.balanceus.topping.infrastructure.persistence;

import java.util.UUID;

import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.UserRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface UserJpaRepository extends UserRepository, JpaRepository<User, UUID> {
}
