package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductJpaRepository extends JpaRepository<Product, UUID> {
	
	List<Product> findByCreator(User creator);
	
	List<Product> findByCategory(String category);
	
	List<Product> findByIsActiveTrue();
	
	// Menu-related methods
	List<Product> findByStore(Store store);
	List<Product> findByStoreAndProductType(Store store, Product.ProductType productType);
	List<Product> findByStoreAndIsAvailable(Store store, Boolean isAvailable);
	Page<Product> findByProductTypeOrderByReviewCountDesc(Product.ProductType productType, Pageable pageable);
	Page<Product> findAllByOrderByReviewCountDesc(Pageable pageable);
	
	// Collaboration-related methods
	long countByStoreAndCollaborationIsNotNull(Store store);
}