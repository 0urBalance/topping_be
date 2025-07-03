package org.balanceus.topping.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.ChatMessage;
import org.balanceus.topping.domain.model.ChatRoom;

public interface ChatMessageRepository {
	
	ChatMessage save(ChatMessage message);
	
	Optional<ChatMessage> findById(UUID uuid);
	
	List<ChatMessage> findByChatRoomOrderByCreatedAtAsc(ChatRoom chatRoom);

	void deleteById(UUID uuid);
}