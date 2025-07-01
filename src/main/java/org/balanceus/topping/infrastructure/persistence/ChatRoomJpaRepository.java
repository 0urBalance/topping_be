package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.ChatRoom;
import org.balanceus.topping.domain.model.CollaborationProposal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomJpaRepository extends JpaRepository<ChatRoom, UUID> {
	
	Optional<ChatRoom> findByCollaborationProposal(CollaborationProposal proposal);
	
	List<ChatRoom> findByIsActiveTrue();
}