package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.Collaboration;
import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.model.Collaboration.CollaborationStatus;
import org.balanceus.topping.domain.repository.CollaborationRepository;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CollaborationRepositoryImpl implements CollaborationRepository {

	private final CollaborationJpaRepository collaborationJpaRepository;

	@Override
	public Collaboration save(Collaboration collaboration) {
		return collaborationJpaRepository.save(collaboration);
	}

	@Override
	public Optional<Collaboration> findById(UUID id) {
		return collaborationJpaRepository.findById(id);
	}

	@Override
	public List<Collaboration> findAll() {
		return collaborationJpaRepository.findAll();
	}

	@Override
	public List<Collaboration> findByProduct(Product product) {
		return collaborationJpaRepository.findByProduct(product);
	}

	@Override
	public List<Collaboration> findByApplicant(User applicant) {
		return collaborationJpaRepository.findByApplicant(applicant);
	}

	@Override
	public List<Collaboration> findByStatus(CollaborationStatus status) {
		return collaborationJpaRepository.findByStatus(status);
	}

	@Override
	public List<Collaboration> findByProductCreator(User creator) {
		return collaborationJpaRepository.findByProductCreator(creator);
	}

	@Override
	public void deleteById(UUID id) {
		collaborationJpaRepository.deleteById(id);
	}
}