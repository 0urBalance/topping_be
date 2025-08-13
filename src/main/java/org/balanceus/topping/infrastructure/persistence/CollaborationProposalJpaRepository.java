package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.balanceus.topping.domain.model.CollaborationProposal;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.ProposalSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CollaborationProposalJpaRepository extends JpaRepository<CollaborationProposal, UUID> {
	
	List<CollaborationProposal> findByStatus(CollaborationProposal.CollaborationStatus status);
	
	List<CollaborationProposal> findByStatusOrderByCreatedAtDesc(CollaborationProposal.CollaborationStatus status);
	
	List<CollaborationProposal> findByProposerUser(User proposerUser);
	
	List<CollaborationProposal> findByProposerStore(Store proposerStore);
	
	@Query("SELECT cp FROM CollaborationProposal cp WHERE cp.proposerUser = :proposerUser OR cp.proposerStore = :proposerStore")
	List<CollaborationProposal> findByProposerUserOrProposerStore(@Param("proposerUser") User proposerUser, @Param("proposerStore") Store proposerStore);
	
	List<CollaborationProposal> findByTargetStore(Store targetStore);
	
	List<CollaborationProposal> findByTargetStoreIsNull();
	
	List<CollaborationProposal> findByTargetStoreAndStatus(Store targetStore, CollaborationProposal.CollaborationStatus status);
	
	List<CollaborationProposal> findBySource(ProposalSource source);
	
	List<CollaborationProposal> findBySourceAndStatus(ProposalSource source, CollaborationProposal.CollaborationStatus status);
	
	List<CollaborationProposal> findByProposerProduct(Product proposerProduct);
	
	List<CollaborationProposal> findByTargetProduct(Product targetProduct);
	
	@Query("SELECT cp FROM CollaborationProposal cp WHERE cp.targetStore = :targetStore AND cp.status = :status")
	List<CollaborationProposal> findReceivedProposals(@Param("targetStore") Store targetStore, @Param("status") CollaborationProposal.CollaborationStatus status);
	
	@Query("SELECT cp FROM CollaborationProposal cp WHERE (cp.proposerUser = :proposerUser OR cp.proposerStore = :proposerStore) AND cp.status = :status")
	List<CollaborationProposal> findSentProposals(@Param("proposerUser") User proposerUser, @Param("proposerStore") Store proposerStore, @Param("status") CollaborationProposal.CollaborationStatus status);
}