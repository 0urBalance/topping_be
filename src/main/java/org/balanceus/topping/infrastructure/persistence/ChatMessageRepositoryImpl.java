package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.ChatMessage;
import org.balanceus.topping.domain.model.ChatRoom;
import org.balanceus.topping.domain.repository.ChatMessageRepository;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ChatMessageRepositoryImpl implements ChatMessageRepository {

	private final ChatMessageJpaRepository jpaRepository;

	@Override
	public ChatMessage save(ChatMessage message) {
		return jpaRepository.save(message);
	}

	@Override
	public Optional<ChatMessage> findById(UUID uuid) {
		return jpaRepository.findById(uuid);
	}

	@Override
	public List<ChatMessage> findByChatRoomOrderByCreatedAtAsc(ChatRoom chatRoom) {
		return jpaRepository.findByChatRoomOrderByCreatedAtAsc(chatRoom);
	}

	@Override
	public void deleteById(UUID uuid) {
		jpaRepository.deleteById(uuid);
	}
}