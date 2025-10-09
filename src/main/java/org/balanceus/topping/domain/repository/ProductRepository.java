package org.balanceus.topping.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository {
	
	Product save(Product product);
	
	Optional<Product> findById(UUID id);
	
	List<Product> findAll();
	
	List<Product> findByCreator(User creator);
	
	List<Product> findByCategory(String category);
	
	List<Product> findByIsActiveTrue();
	
	List<Product> findByCategoryAndIsActiveTrue(Product.ProductCategory category);
	
	void deleteById(UUID id);
	
	// Menu-related methods
	List<Product> findByStore(Store store);
	List<Product> findByStoreAndProductType(Store store, Product.ProductType productType);
	List<Product> findByStoreAndIsAvailable(Store store, Boolean isAvailable);
	Page<Product> findByProductTypeOrderByReviewCountDesc(Product.ProductType productType, Pageable pageable);
	Page<Product> findAllByOrderByReviewCountDesc(Pageable pageable);
	
	// Collaboration-related methods
	long countByStoreAndCollaborationIsNotNull(Store store);
	long countCollaborationProductsByStore(Store store);
	
	// Search methods
	List<Product> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);
	List<Product> findByDescriptionContainingIgnoreCaseAndIsActiveTrue(String description);
	List<Product> findByIngredientsContainingIgnoreCaseAndIsActiveTrue(String ingredients);
	List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrIngredientsContainingIgnoreCaseAndIsActiveTrue(
		String name, String description, String ingredients);
	Page<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrIngredientsContainingIgnoreCaseAndIsActiveTrue(
		String name, String description, String ingredients, Pageable pageable);
}