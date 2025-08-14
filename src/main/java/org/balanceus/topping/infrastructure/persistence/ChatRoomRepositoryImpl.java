package org.balanceus.topping.infrastructure.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.ChatRoom;
import org.balanceus.topping.domain.model.Collaboration;
import org.balanceus.topping.domain.model.CollaborationProposal;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.ChatRoomRepository;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ChatRoomRepositoryImpl implements ChatRoomRepository {

	private final ChatRoomJpaRepository jpaRepository;

	@Override
	public ChatRoom save(ChatRoom chatRoom) {
		return jpaRepository.save(chatRoom);
	}

	@Override
	public Optional<ChatRoom> findById(UUID uuid) {
		return jpaRepository.findById(uuid);
	}

	@Override
	public Optional<ChatRoom> findByCollaborationProposal(CollaborationProposal proposal) {
		return jpaRepository.findByCollaborationProposal(proposal);
	}

	@Override
	public Optional<ChatRoom> findByCollaboration(Collaboration collaboration) {
		return jpaRepository.findByCollaboration(collaboration);
	}

	@Override
	public List<ChatRoom> findByIsActiveTrue() {
		return jpaRepository.findByIsActiveTrue();
	}

	@Override
	public List<ChatRoom> findByParticipant(User user) {
		List<ChatRoom> result = new ArrayList<>();
		
		// Get rooms from collaboration proposals
		List<ChatRoom> proposalRooms = jpaRepository.findByCollaborationProposalParticipant(user);
		result.addAll(proposalRooms);
		
		// Get rooms from collaborations
		List<ChatRoom> collaborationRooms = jpaRepository.findByCollaborationParticipant(user);
		result.addAll(collaborationRooms);
		
		return result;
	}

	@Override
	public List<ChatRoom> findByCollaborationProposalParticipant(User user) {
		return jpaRepository.findByCollaborationProposalParticipant(user);
	}

	@Override
	public List<ChatRoom> findByCollaborationParticipant(User user) {
		return jpaRepository.findByCollaborationParticipant(user);
	}

	@Override
	public void deleteById(UUID uuid) {
		jpaRepository.deleteById(uuid);
	}
}