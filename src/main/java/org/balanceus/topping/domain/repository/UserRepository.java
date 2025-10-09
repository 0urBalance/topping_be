package org.balanceus.topping.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.model.Role;

public interface UserRepository {
	User findByUsername(String username);

	Optional<User> findByEmail(String email);

	Optional<User> findByKakaoId(Long kakaoId);
	
	Optional<User> findByPhoneNumber(String phoneNumber);
	
	boolean existsByPhoneNumber(String phoneNumber);

	boolean existsByKakaoId(Long kakaoId);

	List<User> findByRole(Role role);

	<S extends User> S save(S entity);

	<S extends User> List<S> saveAll(Iterable<S> entities);

	Optional<User> findById(UUID id);

	boolean existsById(UUID id);

	List<User> findAll();

	List<User> findAllById(Iterable<UUID> ids);

	long count();

	void deleteById(UUID id);

	void delete(User entity);

	void deleteAllById(Iterable<? extends UUID> ids);

	void deleteAll(Iterable<? extends User> entities);

	void deleteAll();
}
