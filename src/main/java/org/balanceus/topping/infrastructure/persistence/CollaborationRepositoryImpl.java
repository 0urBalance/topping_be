package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.Collaboration;
import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.Store;
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
	public List<Collaboration> findByStatus(CollaborationStatus status) {
		return collaborationJpaRepository.findByStatus(status);
	}

	@Override
	public List<Collaboration> findByInitiatorStore(Store initiatorStore) {
		return collaborationJpaRepository.findByInitiatorStore(initiatorStore);
	}

	@Override
	public List<Collaboration> findByPartnerStore(Store partnerStore) {
		return collaborationJpaRepository.findByPartnerStore(partnerStore);
	}

	@Override
	public List<Collaboration> findByInitiatorStoreOrPartnerStore(Store store1, Store store2) {
		return collaborationJpaRepository.findByInitiatorStoreOrPartnerStore(store1, store2);
	}

	@Override
	public List<Collaboration> findByInitiatorProduct(Product initiatorProduct) {
		return collaborationJpaRepository.findByInitiatorProduct(initiatorProduct);
	}

	@Override
	public List<Collaboration> findByPartnerProduct(Product partnerProduct) {
		return collaborationJpaRepository.findByPartnerProduct(partnerProduct);
	}

	@Override
	public List<Collaboration> findByInitiatorProductOrPartnerProduct(Product product1, Product product2) {
		return collaborationJpaRepository.findByInitiatorProductOrPartnerProduct(product1, product2);
	}

	@Override
	public List<Collaboration> findByStoresAndProducts(Store initiatorStore, Store partnerStore,
			Product initiatorProduct, Product partnerProduct) {
		return collaborationJpaRepository.findByStoresAndProducts(initiatorStore, partnerStore, 
				initiatorProduct, partnerProduct);
	}

	@Override
	public List<Collaboration> findByStoreAndStatus(Store store, CollaborationStatus status) {
		return collaborationJpaRepository.findByStoreAndStatus(store, status);
	}

	@Override
	public List<Collaboration> findByStoreParticipation(Store store) {
		return collaborationJpaRepository.findByStoreParticipation(store);
	}

	@Override
	public Optional<Collaboration> findActiveCollaborationBetweenStoresAndProducts(Store store1, Store store2,
			Product product1, Product product2) {
		return collaborationJpaRepository.findActiveCollaborationBetweenStoresAndProducts(store1, store2, 
				product1, product2);
	}

	@Override
	public void deleteById(UUID id) {
		collaborationJpaRepository.deleteById(id);
	}
}