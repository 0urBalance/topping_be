package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.balanceus.topping.domain.model.Collaboration;
import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.model.Collaboration.CollaborationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CollaborationJpaRepository extends JpaRepository<Collaboration, UUID> {
	
	List<Collaboration> findByProduct(Product product);
	
	List<Collaboration> findByApplicant(User applicant);
	
	List<Collaboration> findByStatus(CollaborationStatus status);
	
	@Query("SELECT c FROM Collaboration c WHERE c.product.creator = :creator")
	List<Collaboration> findByProductCreator(@Param("creator") User creator);
}