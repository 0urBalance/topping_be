package org.balanceus.topping.domain.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.ChatMessage;
import org.balanceus.topping.domain.model.ChatRoom;
import org.balanceus.topping.domain.model.User;

public interface ChatMessageRepository {
	
	ChatMessage save(ChatMessage message);
	
	Optional<ChatMessage> findById(UUID uuid);
	
	List<ChatMessage> findByChatRoomOrderByCreatedAtAsc(ChatRoom chatRoom);

	void deleteById(UUID uuid);
	
	// Unread message count methods
	long countUnreadMessagesInRoom(ChatRoom chatRoom, User user);
	
	Map<UUID, Long> getUnreadCountsByRoomsForUser(List<ChatRoom> chatRooms, User user);
	
	void markMessagesAsRead(ChatRoom chatRoom, User user);
}