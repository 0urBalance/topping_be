package org.balanceus.topping.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

import org.balanceus.topping.domain.model.ChatMessage;
import org.balanceus.topping.domain.model.ChatRoom;
import org.balanceus.topping.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageJpaRepository extends JpaRepository<ChatMessage, UUID> {
	
	List<ChatMessage> findByChatRoomOrderByCreatedAtAsc(ChatRoom chatRoom);
	
	// Count unread messages in a room for a specific user (messages not sent by them and not read)
	@Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.chatRoom = :chatRoom AND m.sender != :user AND m.isRead = false")
	long countUnreadMessagesInRoom(ChatRoom chatRoom, User user);
	
	// Mark messages as read for a user in a specific room
	@Modifying
	@Query("UPDATE ChatMessage m SET m.isRead = true, m.readAt = CURRENT_TIMESTAMP WHERE m.chatRoom = :chatRoom AND m.sender != :user AND m.isRead = false")
	void markMessagesAsRead(ChatRoom chatRoom, User user);
	
	// Find latest message in a specific room
	@Query("SELECT m FROM ChatMessage m WHERE m.chatRoom = :chatRoom ORDER BY m.createdAt DESC")
	List<ChatMessage> findLatestMessageByRoomPaginated(@Param("chatRoom") ChatRoom chatRoom, Pageable pageable);
	
	// Get latest message times for multiple rooms - returns room UUID and latest message time
	@Query("SELECT m.chatRoom.uuid, MAX(m.createdAt) FROM ChatMessage m WHERE m.chatRoom IN :chatRooms GROUP BY m.chatRoom.uuid")
	List<Object[]> findLatestMessageTimesByRooms(@Param("chatRooms") List<ChatRoom> chatRooms);
	
	// Get latest message content previews for multiple rooms using native SQL with window function
	@Query(value = "SELECT chat_room_uuid, message FROM (" +
		   "SELECT chat_room_uuid, message, ROW_NUMBER() OVER (PARTITION BY chat_room_uuid ORDER BY created_at DESC) as rn " +
		   "FROM chat_messages WHERE chat_room_uuid IN :roomIds" +
		   ") ranked WHERE rn = 1", nativeQuery = true)
	List<Object[]> findLatestMessagePreviewsByRooms(@Param("roomIds") List<UUID> roomIds);
}