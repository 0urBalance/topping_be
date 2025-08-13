package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.Collaboration;
import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.model.Collaboration.CollaborationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CollaborationJpaRepository extends JpaRepository<Collaboration, UUID> {
	
	List<Collaboration> findByStatus(CollaborationStatus status);
	
	List<Collaboration> findByInitiatorStore(Store initiatorStore);
	
	List<Collaboration> findByPartnerStore(Store partnerStore);
	
	@Query("SELECT c FROM Collaboration c WHERE c.initiatorStore = :store1 OR c.partnerStore = :store2")
	List<Collaboration> findByInitiatorStoreOrPartnerStore(@Param("store1") Store store1, @Param("store2") Store store2);
	
	List<Collaboration> findByInitiatorProduct(Product initiatorProduct);
	
	List<Collaboration> findByPartnerProduct(Product partnerProduct);
	
	@Query("SELECT c FROM Collaboration c WHERE c.initiatorProduct = :product1 OR c.partnerProduct = :product2")
	List<Collaboration> findByInitiatorProductOrPartnerProduct(@Param("product1") Product product1, @Param("product2") Product product2);
	
	@Query("SELECT c FROM Collaboration c WHERE c.initiatorStore = :initiatorStore AND c.partnerStore = :partnerStore AND c.initiatorProduct = :initiatorProduct AND c.partnerProduct = :partnerProduct")
	List<Collaboration> findByStoresAndProducts(@Param("initiatorStore") Store initiatorStore, 
												@Param("partnerStore") Store partnerStore,
												@Param("initiatorProduct") Product initiatorProduct, 
												@Param("partnerProduct") Product partnerProduct);
	
	@Query("SELECT c FROM Collaboration c WHERE (c.initiatorStore = :store OR c.partnerStore = :store) AND c.status = :status")
	List<Collaboration> findByStoreAndStatus(@Param("store") Store store, @Param("status") CollaborationStatus status);
	
	@Query("SELECT c FROM Collaboration c WHERE c.initiatorStore = :store OR c.partnerStore = :store")
	List<Collaboration> findByStoreParticipation(@Param("store") Store store);
	
	@Query("SELECT c FROM Collaboration c WHERE ((c.initiatorStore = :store1 AND c.partnerStore = :store2) OR (c.initiatorStore = :store2 AND c.partnerStore = :store1)) AND ((c.initiatorProduct = :product1 AND c.partnerProduct = :product2) OR (c.initiatorProduct = :product2 AND c.partnerProduct = :product1)) AND c.status IN ('PENDING', 'ACCEPTED')")
	Optional<Collaboration> findActiveCollaborationBetweenStoresAndProducts(@Param("store1") Store store1, 
																			@Param("store2") Store store2,
																			@Param("product1") Product product1, 
																			@Param("product2") Product product2);
}