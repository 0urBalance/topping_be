package org.balanceus.topping.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

	@Override
	public Optional<ChatMessage> findLatestMessageByRoom(ChatRoom chatRoom) {
		Pageable pageable = PageRequest.of(0, 1);
		List<ChatMessage> messages = jpaRepository.findLatestMessageByRoomPaginated(chatRoom, pageable);
		return messages.isEmpty() ? Optional.empty() : Optional.of(messages.get(0));
	}

	@Override
	public Map<UUID, LocalDateTime> getLatestMessageTimesByRooms(List<ChatRoom> chatRooms) {
		Map<UUID, LocalDateTime> latestTimes = new HashMap<>();
		
		if (chatRooms.isEmpty()) {
			return latestTimes;
		}
		
		List<Object[]> results = jpaRepository.findLatestMessageTimesByRooms(chatRooms);
		for (Object[] result : results) {
			UUID roomId = (UUID) result[0];
			LocalDateTime latestTime = (LocalDateTime) result[1];
			latestTimes.put(roomId, latestTime);
		}
		
		return latestTimes;
	}

	@Override
	public Map<UUID, String> getLatestMessagePreviewsByRooms(List<ChatRoom> chatRooms) {
		Map<UUID, String> latestPreviews = new HashMap<>();
		
		if (chatRooms.isEmpty()) {
			return latestPreviews;
		}
		
		// Extract room UUIDs for the native query
		List<UUID> roomIds = chatRooms.stream()
			.map(ChatRoom::getUuid)
			.collect(Collectors.toList());
		
		List<Object[]> results = jpaRepository.findLatestMessagePreviewsByRooms(roomIds);
		for (Object[] result : results) {
			UUID roomId = (UUID) result[0];
			String content = (String) result[1];
			
			// Truncate content for preview (limit to 30 characters)
			String preview = content != null && content.length() > 30 
				? content.substring(0, 30) + "..." 
				: content;
			
			latestPreviews.put(roomId, preview);
		}
		
		return latestPreviews;
	}
}