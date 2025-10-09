package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserJpaRepository extends JpaRepository<User, UUID> {
	User findByUsername(String username);
	Optional<User> findByEmail(String email);
	Optional<User> findByPhoneNumber(String phoneNumber);
	boolean existsByPhoneNumber(String phoneNumber);
	List<User> findByRole(Role role);
}
