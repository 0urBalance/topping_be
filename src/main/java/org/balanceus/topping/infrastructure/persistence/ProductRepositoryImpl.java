package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
	public List<Product> findByCategoryAndIsActiveTrue(Product.ProductCategory category) {
		return productJpaRepository.findByCategoryAndIsActiveTrue(category);
	}

	@Override
	public void deleteById(UUID id) {
		productJpaRepository.deleteById(id);
	}

	// Menu-related methods
	@Override
	public List<Product> findByStore(Store store) {
		return productJpaRepository.findByStore(store);
	}

	@Override
	public List<Product> findByStoreAndProductType(Store store, Product.ProductType productType) {
		return productJpaRepository.findByStoreAndProductType(store, productType);
	}

	@Override
	public List<Product> findByStoreAndIsAvailable(Store store, Boolean isAvailable) {
		return productJpaRepository.findByStoreAndIsAvailable(store, isAvailable);
	}

	@Override
	public Page<Product> findByProductTypeOrderByReviewCountDesc(Product.ProductType productType, Pageable pageable) {
		return productJpaRepository.findByProductTypeOrderByReviewCountDesc(productType, pageable);
	}

	@Override
	public Page<Product> findAllByOrderByReviewCountDesc(Pageable pageable) {
		return productJpaRepository.findAllByOrderByReviewCountDesc(pageable);
	}

	@Override
	public long countByStoreAndCollaborationIsNotNull(Store store) {
		return productJpaRepository.countByStoreAndCollaborationIsNotNull(store);
	}
}