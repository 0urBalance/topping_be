package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.UserRepository;
import org.balanceus.topping.infrastructure.security.Role;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

	private final UserJpaRepository userJpaRepository;

	@Override
	public User findByUsername(String username) {
		return userJpaRepository.findByUsername(username);
	}

	@Override
	public Optional<User> findByEmail(String email) {
		return userJpaRepository.findByEmail(email);
	}
	
	@Override
	public Optional<User> findByPhoneNumber(String phoneNumber) {
		return userJpaRepository.findByPhoneNumber(phoneNumber);
	}
	
	@Override
	public boolean existsByPhoneNumber(String phoneNumber) {
		return userJpaRepository.existsByPhoneNumber(phoneNumber);
	}

	@Override
	public List<User> findByRole(Role role) {
		return userJpaRepository.findByRole(role);
	}

	@Override
	public <S extends User> S save(S entity) {
		return userJpaRepository.save(entity);
	}

	@Override
	public <S extends User> List<S> saveAll(Iterable<S> entities) {
		return userJpaRepository.saveAll(entities);
	}

	@Override
	public Optional<User> findById(UUID id) {
		return userJpaRepository.findById(id);
	}

	@Override
	public boolean existsById(UUID id) {
		return userJpaRepository.existsById(id);
	}

	@Override
	public List<User> findAll() {
		return userJpaRepository.findAll();
	}

	@Override
	public List<User> findAllById(Iterable<UUID> ids) {
		return userJpaRepository.findAllById(ids);
	}

	@Override
	public long count() {
		return userJpaRepository.count();
	}

	@Override
	public void deleteById(UUID id) {
		userJpaRepository.deleteById(id);
	}

	@Override
	public void delete(User entity) {
		userJpaRepository.delete(entity);
	}

	@Override
	public void deleteAllById(Iterable<? extends UUID> ids) {
		userJpaRepository.deleteAllById(ids);
	}

	@Override
	public void deleteAll(Iterable<? extends User> entities) {
		userJpaRepository.deleteAll(entities);
	}

	@Override
	public void deleteAll() {
		userJpaRepository.deleteAll();
	}
}