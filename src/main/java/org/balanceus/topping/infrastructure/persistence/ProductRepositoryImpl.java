package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.ProductRepository;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

	private final ProductJpaRepository productJpaRepository;

	@Override
	public Product save(Product product) {
		return productJpaRepository.save(product);
	}

	@Override
	public Optional<Product> findById(UUID id) {
		return productJpaRepository.findById(id);
	}

	@Override
	public List<Product> findAll() {
		return productJpaRepository.findAll();
	}

	@Override
	public List<Product> findByCreator(User creator) {
		return productJpaRepository.findByCreator(creator);
	}

	@Override
	public List<Product> findByCategory(String category) {
		return productJpaRepository.findByCategory(category);
	}

	@Override
	public List<Product> findByIsActiveTrue() {
		return productJpaRepository.findByIsActiveTrue();
	}

	@Override
	public void deleteById(UUID id) {
		productJpaRepository.deleteById(id);
	}
}