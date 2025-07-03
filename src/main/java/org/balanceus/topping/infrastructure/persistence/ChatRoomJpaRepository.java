package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.ChatRoom;
import org.balanceus.topping.domain.model.CollaborationProposal;
import org.balanceus.topping.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomJpaRepository extends JpaRepository<ChatRoom, UUID> {
	
	Optional<ChatRoom> findByCollaborationProposal(CollaborationProposal proposal);
	
	List<ChatRoom> findByIsActiveTrue();
	
	@Query("SELECT cr FROM ChatRoom cr WHERE cr.collaborationProposal.proposer = :user OR cr.collaborationProposal.targetBusinessOwner = :user")
	List<ChatRoom> findByParticipant(@Param("user") User user);
}