package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.balanceus.topping.domain.model.CollaborationProposal;
import org.balanceus.topping.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollaborationProposalJpaRepository extends JpaRepository<CollaborationProposal, UUID> {
	
	List<CollaborationProposal> findByProposer(User proposer);
	
	List<CollaborationProposal> findByTargetBusinessOwner(User targetBusinessOwner);
	
	List<CollaborationProposal> findByStatus(CollaborationProposal.ProposalStatus status);
	
	List<CollaborationProposal> findByStatusOrderByTrendScoreDesc(CollaborationProposal.ProposalStatus status);
	
	List<CollaborationProposal> findByCategoryOrderByCreatedAtDesc(String category);
	
	List<CollaborationProposal> findByStatusOrderByCreatedAtDesc(CollaborationProposal.ProposalStatus status);
	
	List<CollaborationProposal> findByProposerAndStatus(User proposer, CollaborationProposal.ProposalStatus status);
}