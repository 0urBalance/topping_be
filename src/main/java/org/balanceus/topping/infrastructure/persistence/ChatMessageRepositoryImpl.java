package org.balanceus.topping.infrastructure.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.ChatMessage;
import org.balanceus.topping.domain.model.ChatRoom;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.ChatMessageRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

	@Override
	public long countUnreadMessagesInRoom(ChatRoom chatRoom, User user) {
		return jpaRepository.countUnreadMessagesInRoom(chatRoom, user);
	}

	@Override
	public Map<UUID, Long> getUnreadCountsByRoomsForUser(List<ChatRoom> chatRooms, User user) {
		Map<UUID, Long> unreadCounts = new HashMap<>();
		for (ChatRoom chatRoom : chatRooms) {
			long count = countUnreadMessagesInRoom(chatRoom, user);
			unreadCounts.put(chatRoom.getUuid(), count);
		}
		return unreadCounts;
	}

	@Override
	@Transactional
	public void markMessagesAsRead(ChatRoom chatRoom, User user) {
		jpaRepository.markMessagesAsRead(chatRoom, user);
	}
}