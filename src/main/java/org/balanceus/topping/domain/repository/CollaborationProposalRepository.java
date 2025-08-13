package org.balanceus.topping.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.CollaborationProposal;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.ProposalSource;

public interface CollaborationProposalRepository {
	
	CollaborationProposal save(CollaborationProposal proposal);
	
	Optional<CollaborationProposal> findById(UUID uuid);
	
	List<CollaborationProposal> findAll();
	
	List<CollaborationProposal> findByStatus(CollaborationProposal.CollaborationStatus status);
	
	List<CollaborationProposal> findByStatusOrderByCreatedAtDesc(CollaborationProposal.CollaborationStatus status);
	
	// New methods for refactored entity structure
	List<CollaborationProposal> findByProposerUser(User proposerUser);
	
	List<CollaborationProposal> findByProposerStore(Store proposerStore);
	
	List<CollaborationProposal> findByProposerUserOrProposerStore(User proposerUser, Store proposerStore);
	
	List<CollaborationProposal> findByTargetStore(Store targetStore);
	
	List<CollaborationProposal> findByTargetStoreIsNull();
	
	List<CollaborationProposal> findByTargetStoreAndStatus(Store targetStore, CollaborationProposal.CollaborationStatus status);
	
	List<CollaborationProposal> findBySource(ProposalSource source);
	
	List<CollaborationProposal> findBySourceAndStatus(ProposalSource source, CollaborationProposal.CollaborationStatus status);
	
	List<CollaborationProposal> findByProposerProduct(Product proposerProduct);
	
	List<CollaborationProposal> findByTargetProduct(Product targetProduct);
	
	// For business owners - find proposals they received
	List<CollaborationProposal> findReceivedProposals(Store targetStore, CollaborationProposal.CollaborationStatus status);
	
	// For customers/proposers - find proposals they sent
	List<CollaborationProposal> findSentProposals(User proposerUser, Store proposerStore, CollaborationProposal.CollaborationStatus status);

	void deleteById(UUID uuid);
}