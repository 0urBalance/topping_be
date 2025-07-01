package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.CollaborationProposal;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.CollaborationProposalRepository;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CollaborationProposalRepositoryImpl implements CollaborationProposalRepository {

	private final CollaborationProposalJpaRepository jpaRepository;

	@Override
	public CollaborationProposal save(CollaborationProposal proposal) {
		return jpaRepository.save(proposal);
	}

	@Override
	public Optional<CollaborationProposal> findById(UUID uuid) {
		return jpaRepository.findById(uuid);
	}

	@Override
	public List<CollaborationProposal> findAll() {
		return jpaRepository.findAll();
	}

	@Override
	public List<CollaborationProposal> findByProposer(User proposer) {
		return jpaRepository.findByProposer(proposer);
	}

	@Override
	public List<CollaborationProposal> findByTargetBusinessOwner(User targetBusinessOwner) {
		return jpaRepository.findByTargetBusinessOwner(targetBusinessOwner);
	}

	@Override
	public List<CollaborationProposal> findByStatus(CollaborationProposal.ProposalStatus status) {
		return jpaRepository.findByStatus(status);
	}

	@Override
	public List<CollaborationProposal> findByStatusOrderByTrendScoreDesc(CollaborationProposal.ProposalStatus status) {
		return jpaRepository.findByStatusOrderByTrendScoreDesc(status);
	}

	@Override
	public List<CollaborationProposal> findByCategoryOrderByCreatedAtDesc(String category) {
		return jpaRepository.findByCategoryOrderByCreatedAtDesc(category);
	}

	@Override
	public List<CollaborationProposal> findByStatusOrderByCreatedAtDesc(CollaborationProposal.ProposalStatus status) {
		return jpaRepository.findByStatusOrderByCreatedAtDesc(status);
	}

	@Override
	public List<CollaborationProposal> findByProposerAndStatus(User proposer, CollaborationProposal.ProposalStatus status) {
		return jpaRepository.findByProposerAndStatus(proposer, status);
	}

	@Override
	public void deleteById(UUID uuid) {
		jpaRepository.deleteById(uuid);
	}
}