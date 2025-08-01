package org.balanceus.topping.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.balanceus.topping.domain.model.ChatMessage;
import org.balanceus.topping.domain.model.ChatRoom;
import org.balanceus.topping.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ChatMessageJpaRepository extends JpaRepository<ChatMessage, UUID> {
	
	List<ChatMessage> findByChatRoomOrderByCreatedAtAsc(ChatRoom chatRoom);
	
	// Count unread messages in a room for a specific user (messages not sent by them and not read)
	@Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.chatRoom = :chatRoom AND m.sender != :user AND m.isRead = false")
	long countUnreadMessagesInRoom(ChatRoom chatRoom, User user);
	
	// Mark messages as read for a user in a specific room
	@Modifying
	@Query("UPDATE ChatMessage m SET m.isRead = true, m.readAt = CURRENT_TIMESTAMP WHERE m.chatRoom = :chatRoom AND m.sender != :user AND m.isRead = false")
	void markMessagesAsRead(ChatRoom chatRoom, User user);
}