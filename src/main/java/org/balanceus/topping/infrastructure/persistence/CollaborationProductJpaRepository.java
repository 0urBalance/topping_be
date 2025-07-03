package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.balanceus.topping.domain.model.CollaborationProduct;
import org.balanceus.topping.domain.model.CollaborationProposal;
import org.balanceus.topping.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CollaborationProductJpaRepository extends JpaRepository<CollaborationProduct, UUID> {
	
	List<CollaborationProduct> findByCollaborationProposal(CollaborationProposal proposal);
	
	List<CollaborationProduct> findByStatus(CollaborationProduct.ProductStatus status);
	
	List<CollaborationProduct> findByStatusOrderByCreatedAtDesc(CollaborationProduct.ProductStatus status);
	
	@Query("SELECT cp FROM CollaborationProduct cp WHERE cp.collaborationProposal.proposer = :user OR cp.collaborationProposal.targetBusinessOwner = :user")
	List<CollaborationProduct> findByUser(@Param("user") User user);
	
	List<CollaborationProduct> findByCollaborationProposal_Proposer(User proposer);
}