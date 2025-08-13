package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.CollaborationProposal;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.ProposalSource;
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
	public List<CollaborationProposal> findByStatus(CollaborationProposal.CollaborationStatus status) {
		return jpaRepository.findByStatus(status);
	}

	@Override
	public List<CollaborationProposal> findByStatusOrderByCreatedAtDesc(CollaborationProposal.CollaborationStatus status) {
		return jpaRepository.findByStatusOrderByCreatedAtDesc(status);
	}

	@Override
	public List<CollaborationProposal> findByProposerUser(User proposerUser) {
		return jpaRepository.findByProposerUser(proposerUser);
	}

	@Override
	public List<CollaborationProposal> findByProposerStore(Store proposerStore) {
		return jpaRepository.findByProposerStore(proposerStore);
	}

	@Override
	public List<CollaborationProposal> findByProposerUserOrProposerStore(User proposerUser, Store proposerStore) {
		return jpaRepository.findByProposerUserOrProposerStore(proposerUser, proposerStore);
	}

	@Override
	public List<CollaborationProposal> findByTargetStore(Store targetStore) {
		return jpaRepository.findByTargetStore(targetStore);
	}

	@Override
	public List<CollaborationProposal> findByTargetStoreIsNull() {
		return jpaRepository.findByTargetStoreIsNull();
	}

	@Override
	public List<CollaborationProposal> findByTargetStoreAndStatus(Store targetStore, CollaborationProposal.CollaborationStatus status) {
		return jpaRepository.findByTargetStoreAndStatus(targetStore, status);
	}

	@Override
	public List<CollaborationProposal> findBySource(ProposalSource source) {
		return jpaRepository.findBySource(source);
	}

	@Override
	public List<CollaborationProposal> findBySourceAndStatus(ProposalSource source, CollaborationProposal.CollaborationStatus status) {
		return jpaRepository.findBySourceAndStatus(source, status);
	}

	@Override
	public List<CollaborationProposal> findByProposerProduct(Product proposerProduct) {
		return jpaRepository.findByProposerProduct(proposerProduct);
	}

	@Override
	public List<CollaborationProposal> findByTargetProduct(Product targetProduct) {
		return jpaRepository.findByTargetProduct(targetProduct);
	}

	@Override
	public List<CollaborationProposal> findReceivedProposals(Store targetStore, CollaborationProposal.CollaborationStatus status) {
		return jpaRepository.findReceivedProposals(targetStore, status);
	}

	@Override
	public List<CollaborationProposal> findSentProposals(User proposerUser, Store proposerStore, CollaborationProposal.CollaborationStatus status) {
		return jpaRepository.findSentProposals(proposerUser, proposerStore, status);
	}

	@Override
	public void deleteById(UUID uuid) {
		jpaRepository.deleteById(uuid);
	}
}