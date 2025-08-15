package org.balanceus.topping.presentation.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
		
		User proposerUser = null;
		User targetUser = null;
		
		if (proposal != null) {
			proposerUser = proposal.getProposerUser() != null ? proposal.getProposerUser() : 
				(proposal.getProposerStore() != null ? proposal.getProposerStore().getUser() : null);
			targetUser = proposal.getTargetStore() != null ? proposal.getTargetStore().getUser() : null;
		}
		
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
		UUID collaborationId = proposal != null ? proposal.getUuid() : 
			(chatRoom.getCollaboration() != null ? chatRoom.getCollaboration().getUuid() : null);
		data.setCollaborationId(collaborationId);
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
		
		// CRITICAL: Add comprehensive proposal details
		if (proposal != null) {
			data.setProposalDetails(buildProposalDetails(proposal));
		} else {
			log.warn("Chat room {} has no linked proposal - proposal panel will be empty", chatRoom.getUuid());
		}

		return ApiResponseData.success(data);
	}
	
	/**
	 * Build comprehensive proposal details for the chat room UI.
	 * This method creates the complete proposal information that will be displayed in the proposal panel.
	 */
	private ProposalDetails buildProposalDetails(CollaborationProposal proposal) {
		try {
			ProposalDetails details = new ProposalDetails();
			
			// Basic proposal information
			details.setProposalId(proposal.getUuid());
			details.setTitle(proposal.getTitle() != null ? proposal.getTitle() : "Untitled Proposal");
			details.setDescription(proposal.getDescription());
			details.setStatus(proposal.getStatus() != null ? proposal.getStatus().name() : "UNKNOWN");
			details.setSource(proposal.getSource() != null ? proposal.getSource().name() : "UNKNOWN");
			
			// Timeline information
			details.setProposedStart(proposal.getProposedStart());
			details.setProposedEnd(proposal.getProposedEnd());
			details.setCreatedAt(proposal.getCreatedAt());
			details.setUpdatedAt(proposal.getUpdatedAt());
			
			// Build proposer information
			ProposalParticipant proposer = new ProposalParticipant();
			if (proposal.getProposerUser() != null) {
				proposer.setUserId(proposal.getProposerUser().getUuid());
				proposer.setUsername(proposal.getProposerUser().getUsername());
			}
			if (proposal.getProposerStore() != null) {
				proposer.setStoreId(proposal.getProposerStore().getUuid());
				proposer.setStoreName(proposal.getProposerStore().getName());
			}
			details.setProposer(proposer);
			
			// Build target information
			ProposalParticipant target = new ProposalParticipant();
			if (proposal.getTargetStore() != null) {
				target.setStoreId(proposal.getTargetStore().getUuid());
				target.setStoreName(proposal.getTargetStore().getName());
				
				// Get target store's user
				if (proposal.getTargetStore().getUser() != null) {
					target.setUserId(proposal.getTargetStore().getUser().getUuid());
					target.setUsername(proposal.getTargetStore().getUser().getUsername());
				}
			}
			details.setTarget(target);
			
			// Build proposer product information
			if (proposal.getProposerProduct() != null) {
				ProductInfo proposerProduct = new ProductInfo();
				proposerProduct.setProductId(proposal.getProposerProduct().getUuid());
				proposerProduct.setName(proposal.getProposerProduct().getName());
				proposerProduct.setDescription(proposal.getProposerProduct().getDescription());
				proposerProduct.setProductType(proposal.getProposerProduct().getProductType() != null ? 
					proposal.getProposerProduct().getProductType().name() : "UNKNOWN");
				details.setProposerProduct(proposerProduct);
			}
			
			// Build target product information
			if (proposal.getTargetProduct() != null) {
				ProductInfo targetProduct = new ProductInfo();
				targetProduct.setProductId(proposal.getTargetProduct().getUuid());
				targetProduct.setName(proposal.getTargetProduct().getName());
				targetProduct.setDescription(proposal.getTargetProduct().getDescription());
				targetProduct.setProductType(proposal.getTargetProduct().getProductType() != null ? 
					proposal.getTargetProduct().getProductType().name() : "UNKNOWN");
				details.setTargetProduct(targetProduct);
			}
			
			return details;
			
		} catch (Exception e) {
			log.error("Error building proposal details for proposal {}: {}", 
					 proposal.getUuid(), e.getMessage(), e);
			
			// Return minimal details on error
			ProposalDetails fallback = new ProposalDetails();
			fallback.setProposalId(proposal.getUuid());
			fallback.setTitle("Error loading proposal details");
			fallback.setStatus("ERROR");
			return fallback;
		}
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

		// Get chat rooms where user participates (from both proposals and collaborations)
		List<ChatRoom> proposalRooms = chatRoomRepository.findByCollaborationProposalParticipant(user);
		List<ChatRoom> collaborationRooms = chatRoomRepository.findByCollaborationParticipant(user);
		
		// Combine and deduplicate
		Set<UUID> seenRoomIds = new HashSet<>();
		List<ChatRoom> userChatRooms = new ArrayList<>();
		
		for (ChatRoom room : proposalRooms) {
			if (room.getIsActive() && seenRoomIds.add(room.getUuid())) {
				userChatRooms.add(room);
			}
		}
		for (ChatRoom room : collaborationRooms) {
			if (room.getIsActive() && seenRoomIds.add(room.getUuid())) {
				userChatRooms.add(room);
			}
		}
		
		log.info("Found {} chat rooms for user {} ({} from proposals, {} from collaborations)", 
			userChatRooms.size(), user.getUsername(), proposalRooms.size(), collaborationRooms.size());

		// Get unread message counts for each room
		Map<UUID, Long> unreadCounts = chatService.getUnreadCountsByRoomsForUser(userChatRooms, user);
		
		// Get latest message times and previews for each room
		Map<UUID, LocalDateTime> latestMessageTimes = chatMessageRepository.getLatestMessageTimesByRooms(userChatRooms);
		Map<UUID, String> latestMessagePreviews = chatMessageRepository.getLatestMessagePreviewsByRooms(userChatRooms);
		
		// Create enhanced chat room data with unread counts and latest message data
		List<ChatRoomWithUnreadCount> chatRoomsWithCounts = userChatRooms.stream()
				.map(room -> new ChatRoomWithUnreadCount(
					room, 
					unreadCounts.get(room.getUuid()),
					latestMessageTimes.get(room.getUuid()),
					latestMessagePreviews.get(room.getUuid())
				))
				// Sort by latest activity (most recent first), then by creation time
				.sorted(Comparator.comparing(ChatRoomWithUnreadCount::getDisplayTime).reversed())
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
		
		// Enhanced proposal information
		private ProposalDetails proposalDetails;

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
		
		public ProposalDetails getProposalDetails() { return proposalDetails; }
		public void setProposalDetails(ProposalDetails proposalDetails) { this.proposalDetails = proposalDetails; }
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

	// Wrapper class to add unread count and latest message data to ChatRoom for template usage
	public static class ChatRoomWithUnreadCount {
		private final ChatRoom chatRoom;
		private final Long unreadCount;
		private final java.time.LocalDateTime latestMessageTime;
		private final String latestMessagePreview;

		public ChatRoomWithUnreadCount(ChatRoom chatRoom, Long unreadCount) {
			this.chatRoom = chatRoom;
			this.unreadCount = unreadCount != null ? unreadCount : 0L;
			this.latestMessageTime = null;
			this.latestMessagePreview = null;
		}

		public ChatRoomWithUnreadCount(ChatRoom chatRoom, Long unreadCount, 
									   java.time.LocalDateTime latestMessageTime, String latestMessagePreview) {
			this.chatRoom = chatRoom;
			this.unreadCount = unreadCount != null ? unreadCount : 0L;
			this.latestMessageTime = latestMessageTime;
			this.latestMessagePreview = latestMessagePreview;
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
		
		// Latest message data
		public java.time.LocalDateTime getLatestMessageTime() { return latestMessageTime; }
		public String getLatestMessagePreview() { return latestMessagePreview; }
		
		// Utility method to get display time or fallback to room creation time
		public java.time.LocalDateTime getDisplayTime() {
			return latestMessageTime != null ? latestMessageTime : chatRoom.getCreatedAt();
		}
		
		// Utility method to get display preview or default message
		public String getDisplayPreview() {
			return latestMessagePreview != null ? latestMessagePreview : "메시지 내용";
		}
		
		// Utility method for Korean relative time formatting
		public String getFormattedTime() {
			LocalDateTime time = getDisplayTime();
			if (time == null) {
				return "메시지 없음";
			}
			
			LocalDate now = LocalDate.now();
			LocalDate timeDate = time.toLocalDate();
			
			// Define Korean formatters
			DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN);
			DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN);
			
			if (timeDate.equals(now)) {
				// Today - show only time
				return time.format(timeFormatter);
			} else if (timeDate.equals(now.minusDays(1))) {
				// Yesterday
				return "어제";
			} else if (ChronoUnit.DAYS.between(timeDate, now) <= 7) {
				// Within a week - show date
				return time.format(dateFormatter);
			} else {
				// Older - show date
				return time.format(dateFormatter);
			}
		}
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
	
	// Proposal update endpoints
	@PostMapping("/room/{roomId}/proposal/update")
	@ResponseBody
	public ApiResponseData<Void> broadcastProposalUpdate(
			@PathVariable UUID roomId,
			@org.springframework.web.bind.annotation.RequestBody ProposalUpdateRequest request,
			Principal principal) {
		
		try {
			ChatRoom chatRoom = chatRoomRepository.findById(roomId)
					.orElseThrow(() -> new RuntimeException("Chat room not found"));
			
			User currentUser = userRepository.findByEmail(principal.getName())
					.orElseThrow(() -> new RuntimeException("User not found"));
			
			// Create proposal update message
			String updateMessage = String.format("📝 %s님이 제안서를 수정했습니다.", currentUser.getUsername());
			chatService.sendProposalStatusMessage(
				chatRoom,
				org.balanceus.topping.domain.model.ChatMessage.MessageType.PROPOSAL_MODIFIED,
				updateMessage,
				chatRoom.getCollaborationProposal(),
				currentUser
			);
			
			// Also broadcast proposal data update via separate channel
			ProposalUpdateData updateData = new ProposalUpdateData();
			updateData.setRoomId(roomId);
			updateData.setUpdatedBy(currentUser.getUsername());
			updateData.setUpdateType("PROPOSAL_MODIFIED");
			updateData.setTimestamp(java.time.LocalDateTime.now());
			
			messagingTemplate.convertAndSend("/topic/proposal/" + roomId, updateData);
			
			return ApiResponseData.success(null);
		} catch (Exception e) {
			log.error("Failed to broadcast proposal update for room: {}", roomId, e);
			return ApiResponseData.failure(Code.INTERNAL_SERVER_ERROR.ordinal(), "Failed to broadcast proposal update");
		}
	}
	
	// Request/Response classes for proposal updates
	public static class ProposalUpdateRequest {
		private String updateType;
		private String message;
		
		public String getUpdateType() { return updateType; }
		public void setUpdateType(String updateType) { this.updateType = updateType; }
		
		public String getMessage() { return message; }
		public void setMessage(String message) { this.message = message; }
	}
	
	public static class ProposalUpdateData {
		private UUID roomId;
		private String updatedBy;
		private String updateType;
		private java.time.LocalDateTime timestamp;
		
		public UUID getRoomId() { return roomId; }
		public void setRoomId(UUID roomId) { this.roomId = roomId; }
		
		public String getUpdatedBy() { return updatedBy; }
		public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
		
		public String getUpdateType() { return updateType; }
		public void setUpdateType(String updateType) { this.updateType = updateType; }
		
		public java.time.LocalDateTime getTimestamp() { return timestamp; }
		public void setTimestamp(java.time.LocalDateTime timestamp) { this.timestamp = timestamp; }
	}
	
	// Get proposal ID for a chat room
	@GetMapping("/room/{roomId}/proposal")
	@ResponseBody
	public ApiResponseData<ProposalInfo> getRoomProposal(@PathVariable UUID roomId) {
		try {
			ChatRoom chatRoom = chatRoomRepository.findById(roomId)
					.orElseThrow(() -> new RuntimeException("Chat room not found"));
			
			CollaborationProposal proposal = chatRoom.getCollaborationProposal();
			if (proposal == null) {
				return ApiResponseData.success(null);
			}
			
			ProposalInfo proposalInfo = new ProposalInfo();
			proposalInfo.setProposalId(proposal.getUuid());
			proposalInfo.setTitle(proposal.getTitle());
			proposalInfo.setStatus(proposal.getStatus().name());
			
			return ApiResponseData.success(proposalInfo);
		} catch (Exception e) {
			log.error("Failed to get proposal for room: {}", roomId, e);
			return ApiResponseData.failure(Code.INTERNAL_SERVER_ERROR.getCode(), "Failed to get proposal information");
		}
	}
	
	// Administrative endpoints for proposal linkage management
	@PostMapping("/admin/backfill-proposal-links")
	@ResponseBody 
	public ApiResponseData<ChatService.BackfillResult> backfillProposalLinks(Principal principal) {
		try {
			// TODO: Add proper admin authorization check
			User currentUser = userRepository.findByEmail(principal.getName())
					.orElseThrow(() -> new RuntimeException("User not found"));
			
			log.info("Starting proposal backfill requested by user: {}", currentUser.getEmail());
			ChatService.BackfillResult result = chatService.backfillMissingProposalLinks();
			
			if (result.isSuccessful()) {
				return ApiResponseData.success(result);
			} else {
				return ApiResponseData.failure(Code.INTERNAL_SERVER_ERROR.getCode(), 
					"Backfill completed with errors: " + result.getOverallError());
			}
		} catch (Exception e) {
			log.error("Failed to execute proposal backfill: {}", e.getMessage(), e);
			return ApiResponseData.failure(Code.INTERNAL_SERVER_ERROR.getCode(), 
				"Backfill operation failed: " + e.getMessage());
		}
	}
	
	@GetMapping("/admin/linkage-statistics")
	@ResponseBody
	public ApiResponseData<ChatService.LinkageStatistics> getLinkageStatistics(Principal principal) {
		try {
			// TODO: Add proper admin authorization check
			User currentUser = userRepository.findByEmail(principal.getName())
					.orElseThrow(() -> new RuntimeException("User not found"));
			
			ChatService.LinkageStatistics stats = chatService.getProposalLinkageStatistics();
			
			if (stats.getError() != null) {
				return ApiResponseData.failure(Code.INTERNAL_SERVER_ERROR.getCode(), 
					"Failed to gather statistics: " + stats.getError());
			}
			
			return ApiResponseData.success(stats);
		} catch (Exception e) {
			log.error("Failed to get linkage statistics: {}", e.getMessage(), e);
			return ApiResponseData.failure(Code.INTERNAL_SERVER_ERROR.getCode(), 
				"Statistics operation failed: " + e.getMessage());
		}
	}
	
	@GetMapping("/admin/validate-linkages")
	@ResponseBody
	public ApiResponseData<ChatService.ValidationReport> validateProposalLinkages(Principal principal) {
		try {
			// TODO: Add proper admin authorization check
			User currentUser = userRepository.findByEmail(principal.getName())
					.orElseThrow(() -> new RuntimeException("User not found"));
			
			log.info("Starting proposal linkage validation requested by user: {}", currentUser.getEmail());
			ChatService.ValidationReport report = chatService.validateProposalLinkageIntegrity();
			
			if (report.getOverallError() != null) {
				return ApiResponseData.failure(Code.INTERNAL_SERVER_ERROR.getCode(), 
					"Validation failed: " + report.getOverallError());
			}
			
			return ApiResponseData.success(report);
		} catch (Exception e) {
			log.error("Failed to execute proposal linkage validation: {}", e.getMessage(), e);
			return ApiResponseData.failure(Code.INTERNAL_SERVER_ERROR.getCode(), 
				"Validation operation failed: " + e.getMessage());
		}
	}
	
	public static class ProposalInfo {
		private UUID proposalId;
		private String title;
		private String status;
		
		public UUID getProposalId() { return proposalId; }
		public void setProposalId(UUID proposalId) { this.proposalId = proposalId; }
		
		public String getTitle() { return title; }
		public void setTitle(String title) { this.title = title; }
		
		public String getStatus() { return status; }
		public void setStatus(String status) { this.status = status; }
	}
	
	public static class ProposalDetails {
		private UUID proposalId;
		private String title;
		private String description;
		private String status;
		private String source; // BUSINESS_OWNER or CUSTOMER
		
		// Proposer information
		private ProposalParticipant proposer;
		
		// Target information  
		private ProposalParticipant target;
		
		// Products involved
		private ProductInfo proposerProduct;
		private ProductInfo targetProduct;
		
		// Timeline
		private java.time.LocalDate proposedStart;
		private java.time.LocalDate proposedEnd;
		private java.time.Instant createdAt;
		private java.time.Instant updatedAt;
		
		// Getters and setters
		public UUID getProposalId() { return proposalId; }
		public void setProposalId(UUID proposalId) { this.proposalId = proposalId; }
		
		public String getTitle() { return title; }
		public void setTitle(String title) { this.title = title; }
		
		public String getDescription() { return description; }
		public void setDescription(String description) { this.description = description; }
		
		public String getStatus() { return status; }
		public void setStatus(String status) { this.status = status; }
		
		public String getSource() { return source; }
		public void setSource(String source) { this.source = source; }
		
		public ProposalParticipant getProposer() { return proposer; }
		public void setProposer(ProposalParticipant proposer) { this.proposer = proposer; }
		
		public ProposalParticipant getTarget() { return target; }
		public void setTarget(ProposalParticipant target) { this.target = target; }
		
		public ProductInfo getProposerProduct() { return proposerProduct; }
		public void setProposerProduct(ProductInfo proposerProduct) { this.proposerProduct = proposerProduct; }
		
		public ProductInfo getTargetProduct() { return targetProduct; }
		public void setTargetProduct(ProductInfo targetProduct) { this.targetProduct = targetProduct; }
		
		public java.time.LocalDate getProposedStart() { return proposedStart; }
		public void setProposedStart(java.time.LocalDate proposedStart) { this.proposedStart = proposedStart; }
		
		public java.time.LocalDate getProposedEnd() { return proposedEnd; }
		public void setProposedEnd(java.time.LocalDate proposedEnd) { this.proposedEnd = proposedEnd; }
		
		public java.time.Instant getCreatedAt() { return createdAt; }
		public void setCreatedAt(java.time.Instant createdAt) { this.createdAt = createdAt; }
		
		public java.time.Instant getUpdatedAt() { return updatedAt; }
		public void setUpdatedAt(java.time.Instant updatedAt) { this.updatedAt = updatedAt; }
	}
	
	public static class ProposalParticipant {
		private UUID userId;
		private String username;
		private UUID storeId;
		private String storeName;
		
		public UUID getUserId() { return userId; }
		public void setUserId(UUID userId) { this.userId = userId; }
		
		public String getUsername() { return username; }
		public void setUsername(String username) { this.username = username; }
		
		public UUID getStoreId() { return storeId; }
		public void setStoreId(UUID storeId) { this.storeId = storeId; }
		
		public String getStoreName() { return storeName; }
		public void setStoreName(String storeName) { this.storeName = storeName; }
	}
	
	public static class ProductInfo {
		private UUID productId;
		private String name;
		private String description;
		private String productType;
		
		public UUID getProductId() { return productId; }
		public void setProductId(UUID productId) { this.productId = productId; }
		
		public String getName() { return name; }
		public void setName(String name) { this.name = name; }
		
		public String getDescription() { return description; }
		public void setDescription(String description) { this.description = description; }
		
		public String getProductType() { return productType; }
		public void setProductType(String productType) { this.productType = productType; }
	}
}