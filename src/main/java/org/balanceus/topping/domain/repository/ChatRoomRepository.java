package org.balanceus.topping.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.ChatRoom;
import org.balanceus.topping.domain.model.CollaborationProposal;

public interface ChatRoomRepository {
	
	ChatRoom save(ChatRoom chatRoom);
	
	Optional<ChatRoom> findById(UUID uuid);
	
	Optional<ChatRoom> findByCollaborationProposal(CollaborationProposal proposal);
	
	List<ChatRoom> findByIsActiveTrue();

	void deleteById(UUID uuid);
}