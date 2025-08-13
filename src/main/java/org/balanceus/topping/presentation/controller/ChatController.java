package org.balanceus.topping.presentation.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.balanceus.topping.application.service.ChatService;
import org.balanceus.topping.domain.model.ChatMessage;
import org.balanceus.topping.domain.model.ChatRoom;
import org.balanceus.topping.domain.model.Collaboration;
import org.balanceus.topping.domain.model.CollaborationProposal;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.ChatMessageRepository;
import org.balanceus.topping.domain.repository.ChatRoomRepository;
import org.balanceus.topping.domain.repository.CollaborationProposalRepository;
import org.balanceus.topping.domain.repository.UserRepository;
import org.balanceus.topping.infrastructure.response.ApiResponseData;
import org.balanceus.topping.infrastructure.response.Code;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

	private final ChatRoomRepository chatRoomRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final CollaborationProposalRepository proposalRepository;
	private final UserRepository userRepository;
	private final ChatService chatService;
	private final SimpMessagingTemplate messagingTemplate;

	@PostMapping("/room/create/{proposalId}")
	@ResponseBody
	public ApiResponseData<ChatRoom> createChatRoom(
			@PathVariable UUID proposalId,
			Principal principal) {

		CollaborationProposal proposal = proposalRepository.findById(proposalId)
				.orElseThrow(() -> new RuntimeException("Proposal not found"));

		if (proposal.getStatus() != CollaborationProposal.CollaborationStatus.ACCEPTED) {
			throw new RuntimeException("Only accepted proposals can have chat rooms");
		}

		ChatRoom existingRoom = chatRoomRepository.findByCollaborationProposal(proposal).orElse(null);
		if (existingRoom != null) {
			return ApiResponseData.success(existingRoom);
		}

		ChatRoom chatRoom = new ChatRoom();
		chatRoom.setCollaborationProposal(proposal);
		chatRoom.setRoomName(proposal.getTitle() + " 협업 채팅방");
		chatRoom.setIsActive(true);

		ChatRoom saved = chatRoomRepository.save(chatRoom);
		return ApiResponseData.success(saved);
	}


	@GetMapping("/room/{roomId}/data")
	@ResponseBody
	public ApiResponseData<ChatRoomData> getChatRoomData(@PathVariable UUID roomId, Principal principal) {
		ChatRoom chatRoom = chatRoomRepository.findById(roomId)
				.orElseThrow(() -> new RuntimeException("Chat room not found"));

		User currentUser = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));

		// Mark messages as read when user views the chat room
		chatService.markMessagesAsRead(chatRoom, currentUser);

		List<ChatMessage> messages = chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(chatRoom);

		// Find the other user in the chat (not the current user)
		CollaborationProposal proposal = chatRoom.getCollaborationProposal();
		User otherUser = null;
		
		User proposerUser = proposal.getProposerUser() != null ? proposal.getProposerUser() : 
			(proposal.getProposerStore() != null ? proposal.getProposerStore().getUser() : null);
		User targetUser = proposal.getTargetStore() != null ? proposal.getTargetStore().getUser() : null;
		
		if (proposerUser != null && proposerUser.equals(currentUser)) {
			otherUser = targetUser;
		} else {
			otherUser = proposerUser;
		}
		
		// If otherUser is still null, try to find from collaboration
		if (otherUser == null && chatRoom.getCollaboration() != null) {
			Collaboration collaboration = chatRoom.getCollaboration();
			// Get the partner store owner and initiator store owner
			User initiatorOwner = collaboration.getInitiatorStore() != null ? collaboration.getInitiatorStore().getUser() : null;
			User partnerOwner = collaboration.getPartnerStore() != null ? collaboration.getPartnerStore().getUser() : null;
			
			if (initiatorOwner != null && partnerOwner != null) {
				otherUser = initiatorOwner.equals(currentUser) ? partnerOwner : initiatorOwner;
			}
		}
		
		// Fallback: create a placeholder user if still null
		if (otherUser == null) {
			otherUser = new User();
			otherUser.setUuid(UUID.randomUUID());
			otherUser.setUsername("Unknown User");
		}

		ChatRoomData data = new ChatRoomData();
		data.setRoomId(chatRoom.getUuid());
		data.setRoomName(chatRoom.getRoomName());
		data.setCollaborationId(proposal.getUuid());
		data.setOtherUser(new UserInfo(otherUser.getUuid(), otherUser.getUsername()));
		data.setMessages(messages.stream()
			.map(msg -> new MessageInfo(
				msg.getUuid(),
				msg.getSender().getUuid(),
				msg.getSender().getUsername(),
				msg.getMessage(),
				msg.getCreatedAt()
			))
			.toList());

		return ApiResponseData.success(data);
	}

	@PostMapping("/message/send")
	@ResponseBody
	public ApiResponseData<MessageInfo> sendMessage(
			@org.springframework.web.bind.annotation.RequestBody SendMessageRequest request,
			Principal principal) {

		ChatRoom chatRoom = chatRoomRepository.findById(request.getRoomId())
				.orElseThrow(() -> new RuntimeException("Chat room not found"));

		User sender = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));

		ChatMessage chatMessage = new ChatMessage();
		chatMessage.setChatRoom(chatRoom);
		chatMessage.setSender(sender);
		chatMessage.setMessage(request.getMessage());

		ChatMessage saved = chatMessageRepository.save(chatMessage);
		
		// Create DTO for response and WebSocket broadcast
		MessageInfo messageDto = new MessageInfo(
			saved.getUuid(),
			saved.getSender().getUuid(),
			saved.getSender().getUsername(),
			saved.getMessage(),
			saved.getCreatedAt()
		);
		
		// Broadcast message to WebSocket subscribers in the room
		WebSocketMessageData wsMessage = new WebSocketMessageData();
		wsMessage.setMessageId(saved.getUuid());
		wsMessage.setSender(new WebSocketUserInfo(saved.getSender().getUuid(), saved.getSender().getUsername()));
		wsMessage.setMessage(saved.getMessage());
		wsMessage.setCreatedAt(saved.getCreatedAt());
		
		messagingTemplate.convertAndSend("/topic/chat/" + request.getRoomId(), wsMessage);
		
		log.info("Message sent and broadcasted - Room: {}, Sender: {}, Message: {}", 
			request.getRoomId(), sender.getUsername(), saved.getMessage().substring(0, Math.min(50, saved.getMessage().length())));
		
		return ApiResponseData.success(messageDto);
	}

	@MessageMapping("/chat/{roomId}")
	@SendTo("/topic/chat/{roomId}")
	public MessageInfo handleMessage(@PathVariable UUID roomId, ChatMessage message) {
		ChatMessage saved = chatMessageRepository.save(message);
		
		// Return DTO to prevent circular references in WebSocket
		return new MessageInfo(
			saved.getUuid(),
			saved.getSender().getUuid(),
			saved.getSender().getUsername(),
			saved.getMessage(),
			saved.getCreatedAt()
		);
	}

	@GetMapping("/rooms")
	public String listChatRooms(Model model, Principal principal) {
		User user = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));

		List<ChatRoom> activeChatRooms = chatRoomRepository.findByIsActiveTrue();
		
		List<ChatRoom> userChatRooms = activeChatRooms.stream()
				.filter(room -> {
					CollaborationProposal proposal = room.getCollaborationProposal();
					User proposerUser = proposal.getProposerUser() != null ? proposal.getProposerUser() : 
						(proposal.getProposerStore() != null ? proposal.getProposerStore().getUser() : null);
					User targetUser = proposal.getTargetStore() != null ? proposal.getTargetStore().getUser() : null;
					return (proposerUser != null && proposerUser.equals(user)) || 
						   (targetUser != null && targetUser.equals(user));
				})
				.toList();

		// Get unread message counts for each room
		Map<UUID, Long> unreadCounts = chatService.getUnreadCountsByRoomsForUser(userChatRooms, user);
		
		// Create enhanced chat room data with unread counts
		List<ChatRoomWithUnreadCount> chatRoomsWithCounts = userChatRooms.stream()
				.map(room -> new ChatRoomWithUnreadCount(room, unreadCounts.get(room.getUuid())))
				.toList();

		model.addAttribute("chatRooms", chatRoomsWithCounts);
		return "chat/rooms";
	}

	// DTO classes for JSON requests and responses
	public static class SendMessageRequest {
		private UUID roomId;
		private String message;

		public UUID getRoomId() { return roomId; }
		public void setRoomId(UUID roomId) { this.roomId = roomId; }
		
		public String getMessage() { return message; }
		public void setMessage(String message) { this.message = message; }
	}

	public static class ChatRoomData {
		private UUID roomId;
		private String roomName;
		private UUID collaborationId;
		private UserInfo otherUser;
		private List<MessageInfo> messages;

		// Getters and setters
		public UUID getRoomId() { return roomId; }
		public void setRoomId(UUID roomId) { this.roomId = roomId; }
		
		public String getRoomName() { return roomName; }
		public void setRoomName(String roomName) { this.roomName = roomName; }
		
		public UUID getCollaborationId() { return collaborationId; }
		public void setCollaborationId(UUID collaborationId) { this.collaborationId = collaborationId; }
		
		public UserInfo getOtherUser() { return otherUser; }
		public void setOtherUser(UserInfo otherUser) { this.otherUser = otherUser; }
		
		public List<MessageInfo> getMessages() { return messages; }
		public void setMessages(List<MessageInfo> messages) { this.messages = messages; }
	}

	public static class UserInfo {
		private UUID userId;
		private String username;

		public UserInfo(UUID userId, String username) {
			this.userId = userId;
			this.username = username;
		}

		public UUID getUserId() { return userId; }
		public void setUserId(UUID userId) { this.userId = userId; }
		
		public String getUsername() { return username; }
		public void setUsername(String username) { this.username = username; }
	}

	public static class MessageInfo {
		private UUID messageId;
		private UUID senderId;
		private String senderName;
		private String message;
		private java.time.LocalDateTime createdAt;

		public MessageInfo(UUID messageId, UUID senderId, String senderName, String message, java.time.LocalDateTime createdAt) {
			this.messageId = messageId;
			this.senderId = senderId;
			this.senderName = senderName;
			this.message = message;
			this.createdAt = createdAt;
		}

		public UUID getMessageId() { return messageId; }
		public void setMessageId(UUID messageId) { this.messageId = messageId; }
		
		public UUID getSenderId() { return senderId; }
		public void setSenderId(UUID senderId) { this.senderId = senderId; }
		
		public String getSenderName() { return senderName; }
		public void setSenderName(String senderName) { this.senderName = senderName; }
		
		public String getMessage() { return message; }
		public void setMessage(String message) { this.message = message; }
		
		public java.time.LocalDateTime getCreatedAt() { return createdAt; }
		public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
	}

	// Wrapper class to add unread count to ChatRoom for template usage
	public static class ChatRoomWithUnreadCount {
		private final ChatRoom chatRoom;
		private final Long unreadCount;

		public ChatRoomWithUnreadCount(ChatRoom chatRoom, Long unreadCount) {
			this.chatRoom = chatRoom;
			this.unreadCount = unreadCount != null ? unreadCount : 0L;
		}

		// Delegate ChatRoom methods
		public UUID getUuid() { return chatRoom.getUuid(); }
		public String getRoomName() { return chatRoom.getRoomName(); }
		public Boolean getIsActive() { return chatRoom.getIsActive(); }
		public java.time.LocalDateTime getCreatedAt() { return chatRoom.getCreatedAt(); }
		public CollaborationProposal getCollaborationProposal() { return chatRoom.getCollaborationProposal(); }
		public Collaboration getCollaboration() { return chatRoom.getCollaboration(); }

		// Unread count
		public Long getUnreadCount() { return unreadCount; }
	}

	// WebSocket message data class for broadcasting
	public static class WebSocketMessageData {
		private UUID messageId;
		private WebSocketUserInfo sender;
		private String message;
		private java.time.LocalDateTime createdAt;
		
		// Getters and setters
		public UUID getMessageId() { return messageId; }
		public void setMessageId(UUID messageId) { this.messageId = messageId; }
		
		public WebSocketUserInfo getSender() { return sender; }
		public void setSender(WebSocketUserInfo sender) { this.sender = sender; }
		
		public String getMessage() { return message; }
		public void setMessage(String message) { this.message = message; }
		
		public java.time.LocalDateTime getCreatedAt() { return createdAt; }
		public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
	}

	// WebSocket user info with uuid field (to match frontend expectations)
	public static class WebSocketUserInfo {
		private UUID uuid;
		private String username;

		public WebSocketUserInfo(UUID uuid, String username) {
			this.uuid = uuid;
			this.username = username;
		}

		public UUID getUuid() { return uuid; }
		public void setUuid(UUID uuid) { this.uuid = uuid; }
		
		public String getUsername() { return username; }
		public void setUsername(String username) { this.username = username; }
	}
}