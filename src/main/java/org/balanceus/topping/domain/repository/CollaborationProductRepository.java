package org.balanceus.topping.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.CollaborationProduct;
import org.balanceus.topping.domain.model.CollaborationProposal;
import org.balanceus.topping.domain.model.User;

public interface CollaborationProductRepository {
	
	CollaborationProduct save(CollaborationProduct product);
	
	Optional<CollaborationProduct> findById(UUID uuid);
	
	List<CollaborationProduct> findAll();
	
	List<CollaborationProduct> findByCollaborationProposal(CollaborationProposal proposal);
	
	List<CollaborationProduct> findByStatus(CollaborationProduct.ProductStatus status);
	
	List<CollaborationProduct> findByStatusOrderByCreatedAtDesc(CollaborationProduct.ProductStatus status);
	
	List<CollaborationProduct> findByUser(User user);
	
	List<CollaborationProduct> findByCollaborationProposal_Proposer(User proposer);

	void deleteById(UUID uuid);
}