package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.CollaborationProduct;
import org.balanceus.topping.domain.model.CollaborationProposal;
import org.balanceus.topping.domain.repository.CollaborationProductRepository;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CollaborationProductRepositoryImpl implements CollaborationProductRepository {

	private final CollaborationProductJpaRepository jpaRepository;

	@Override
	public CollaborationProduct save(CollaborationProduct product) {
		return jpaRepository.save(product);
	}

	@Override
	public Optional<CollaborationProduct> findById(UUID uuid) {
		return jpaRepository.findById(uuid);
	}

	@Override
	public List<CollaborationProduct> findAll() {
		return jpaRepository.findAll();
	}

	@Override
	public List<CollaborationProduct> findByCollaborationProposal(CollaborationProposal proposal) {
		return jpaRepository.findByCollaborationProposal(proposal);
	}

	@Override
	public List<CollaborationProduct> findByStatus(CollaborationProduct.ProductStatus status) {
		return jpaRepository.findByStatus(status);
	}

	@Override
	public List<CollaborationProduct> findByStatusOrderByCreatedAtDesc(CollaborationProduct.ProductStatus status) {
		return jpaRepository.findByStatusOrderByCreatedAtDesc(status);
	}

	@Override
	public void deleteById(UUID uuid) {
		jpaRepository.deleteById(uuid);
	}
}