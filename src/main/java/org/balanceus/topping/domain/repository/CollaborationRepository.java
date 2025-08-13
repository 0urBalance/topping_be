package org.balanceus.topping.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.Collaboration;
import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.model.Collaboration.CollaborationStatus;

public interface CollaborationRepository {
	
	Collaboration save(Collaboration collaboration);
	
	Optional<Collaboration> findById(UUID id);
	
	List<Collaboration> findAll();
	
	List<Collaboration> findByStatus(CollaborationStatus status);
	
	// New methods for refactored entity structure
	List<Collaboration> findByInitiatorStore(Store initiatorStore);
	
	List<Collaboration> findByPartnerStore(Store partnerStore);
	
	List<Collaboration> findByInitiatorStoreOrPartnerStore(Store store1, Store store2);
	
	List<Collaboration> findByInitiatorProduct(Product initiatorProduct);
	
	List<Collaboration> findByPartnerProduct(Product partnerProduct);
	
	List<Collaboration> findByInitiatorProductOrPartnerProduct(Product product1, Product product2);
	
	List<Collaboration> findByStoresAndProducts(Store initiatorStore, Store partnerStore, 
												Product initiatorProduct, Product partnerProduct);
	
	List<Collaboration> findByStoreAndStatus(Store store, CollaborationStatus status);
	
	List<Collaboration> findByStoreParticipation(Store store);
	
	// Check for existing active collaboration between stores and products
	Optional<Collaboration> findActiveCollaborationBetweenStoresAndProducts(Store store1, Store store2, 
																			Product product1, Product product2);
	
	void deleteById(UUID id);
}