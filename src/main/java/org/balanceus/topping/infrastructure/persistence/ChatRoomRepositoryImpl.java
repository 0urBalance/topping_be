package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.ChatRoom;
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
	public List<ChatRoom> findByIsActiveTrue() {
		return jpaRepository.findByIsActiveTrue();
	}

	@Override
	public List<ChatRoom> findByParticipant(User user) {
		return jpaRepository.findByParticipant(user);
	}

	@Override
	public void deleteById(UUID uuid) {
		jpaRepository.deleteById(uuid);
	}
}