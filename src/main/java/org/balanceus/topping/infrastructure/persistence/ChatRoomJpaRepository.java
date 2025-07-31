package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.ChatRoom;
import org.balanceus.topping.domain.model.Collaboration;
import org.balanceus.topping.domain.model.CollaborationProposal;
import org.balanceus.topping.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomJpaRepository extends JpaRepository<ChatRoom, UUID> {
	
	Optional<ChatRoom> findByCollaborationProposal(CollaborationProposal proposal);
	
	Optional<ChatRoom> findByCollaboration(Collaboration collaboration);
	
	List<ChatRoom> findByIsActiveTrue();
	
	@Query("SELECT cr FROM ChatRoom cr WHERE " +
	       "cr.collaborationProposal IS NOT NULL AND " +
	       "(cr.collaborationProposal.proposer = :user OR cr.collaborationProposal.targetBusinessOwner = :user)")
	List<ChatRoom> findByCollaborationProposalParticipant(@Param("user") User user);
	
	@Query("SELECT cr FROM ChatRoom cr JOIN cr.collaboration c JOIN c.product p WHERE " +
	       "cr.collaboration IS NOT NULL AND " +
	       "(c.applicant = :user OR p.creator = :user)")
	List<ChatRoom> findByCollaborationParticipant(@Param("user") User user);
}