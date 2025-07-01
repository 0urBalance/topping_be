package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.balanceus.topping.domain.model.CollaborationProduct;
import org.balanceus.topping.domain.model.CollaborationProposal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollaborationProductJpaRepository extends JpaRepository<CollaborationProduct, UUID> {
	
	List<CollaborationProduct> findByCollaborationProposal(CollaborationProposal proposal);
	
	List<CollaborationProduct> findByStatus(CollaborationProduct.ProductStatus status);
	
	List<CollaborationProduct> findByStatusOrderByCreatedAtDesc(CollaborationProduct.ProductStatus status);
}