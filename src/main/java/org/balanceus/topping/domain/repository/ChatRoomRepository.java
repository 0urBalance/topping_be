package org.balanceus.topping.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.ChatRoom;
import org.balanceus.topping.domain.model.Collaboration;
import org.balanceus.topping.domain.model.CollaborationProposal;
import org.balanceus.topping.domain.model.User;

public interface ChatRoomRepository {
	
	ChatRoom save(ChatRoom chatRoom);
	
	Optional<ChatRoom> findById(UUID uuid);
	
	Optional<ChatRoom> findByCollaborationProposal(CollaborationProposal proposal);
	
	Optional<ChatRoom> findByCollaboration(Collaboration collaboration);
	
	List<ChatRoom> findByIsActiveTrue();
	
	List<ChatRoom> findByParticipant(User user);
	
	List<ChatRoom> findByCollaborationProposalParticipant(User user);
	
	List<ChatRoom> findByCollaborationParticipant(User user);
	
	List<ChatRoom> findAll();

	void deleteById(UUID uuid);
}