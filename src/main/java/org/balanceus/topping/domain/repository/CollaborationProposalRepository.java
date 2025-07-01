package org.balanceus.topping.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.CollaborationProposal;
import org.balanceus.topping.domain.model.User;

public interface CollaborationProposalRepository {
	
	CollaborationProposal save(CollaborationProposal proposal);
	
	Optional<CollaborationProposal> findById(UUID uuid);
	
	List<CollaborationProposal> findAll();
	
	List<CollaborationProposal> findByProposer(User proposer);
	
	List<CollaborationProposal> findByTargetBusinessOwner(User targetBusinessOwner);
	
	List<CollaborationProposal> findByStatus(CollaborationProposal.ProposalStatus status);
	
	List<CollaborationProposal> findByStatusOrderByTrendScoreDesc(CollaborationProposal.ProposalStatus status);
	
	List<CollaborationProposal> findByCategoryOrderByCreatedAtDesc(String category);
	
	List<CollaborationProposal> findByStatusOrderByCreatedAtDesc(CollaborationProposal.ProposalStatus status);
	
	List<CollaborationProposal> findByProposerAndStatus(User proposer, CollaborationProposal.ProposalStatus status);

	void deleteById(UUID uuid);
}