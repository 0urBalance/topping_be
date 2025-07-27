package org.balanceus.topping.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.Collaboration;
import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.model.Collaboration.CollaborationStatus;

public interface CollaborationRepository {
	
	Collaboration save(Collaboration collaboration);
	
	Optional<Collaboration> findById(UUID id);
	
	List<Collaboration> findAll();
	
	List<Collaboration> findByProduct(Product product);
	
	List<Collaboration> findByApplicant(User applicant);
	
	List<Collaboration> findByStatus(CollaborationStatus status);
	
	List<Collaboration> findByProductCreator(User creator);
	
	List<Collaboration> findByParticipant(User user);
	
	List<Collaboration> findByProductStoreAndStatus(Store store, CollaborationStatus status);
	
	void deleteById(UUID id);
}