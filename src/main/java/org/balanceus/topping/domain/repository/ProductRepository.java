package org.balanceus.topping.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.User;

public interface ProductRepository {
	
	Product save(Product product);
	
	Optional<Product> findById(UUID id);
	
	List<Product> findAll();
	
	List<Product> findByCreator(User creator);
	
	List<Product> findByCategory(String category);
	
	List<Product> findByIsActiveTrue();
	
	void deleteById(UUID id);
}