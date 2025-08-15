package org.balanceus.topping.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.balanceus.topping.domain.model.ChatMessage;
import org.balanceus.topping.domain.model.ChatRoom;
import org.balanceus.topping.domain.model.Collaboration;
import org.balanceus.topping.domain.model.CollaborationProposal;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.ChatMessageRepository;
import org.balanceus.topping.domain.repository.ChatRoomRepository;
import org.balanceus.topping.domain.repository.CollaborationProposalRepository;
import org.balanceus.topping.domain.repository.CollaborationRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final CollaborationRepository collaborationRepository;
    private final CollaborationProposalRepository collaborationProposalRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public ChatRoom createChatRoomForCollaborationProposal(UUID proposalId) {
        Optional<CollaborationProposal> proposalOpt = collaborationProposalRepository.findById(proposalId);
        if (proposalOpt.isEmpty()) {
            throw new RuntimeException("CollaborationProposal not found with id: " + proposalId);
        }

        CollaborationProposal proposal = proposalOpt.get();
        
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByCollaborationProposal(proposal);
        if (existingRoom.isPresent()) {
            log.info("Chat room already exists for proposal: {}", proposalId);
            return existingRoom.get();
        }

        ChatRoom chatRoom = ChatRoom.builder()
                .collaborationProposal(proposal)
                .roomName(generateRoomName(proposal))
                .isActive(true)
                .build();

        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);
        log.info("Created chat room {} for collaboration proposal: {}", savedRoom.getUuid(), proposalId);
        return savedRoom;
    }

    public ChatRoom createChatRoomForCollaboration(UUID collaborationId) {
        Optional<Collaboration> collaborationOpt = collaborationRepository.findById(collaborationId);
        if (collaborationOpt.isEmpty()) {
            throw new RuntimeException("Collaboration not found with id: " + collaborationId);
        }

        Collaboration collaboration = collaborationOpt.get();
        
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByCollaboration(collaboration);
        if (existingRoom.isPresent()) {
            log.info("Chat room already exists for collaboration: {}", collaborationId);
            return existingRoom.get();
        }

        ChatRoom chatRoom = ChatRoom.builder()
                .collaboration(collaboration)
                .roomName(generateRoomName(collaboration))
                .isActive(true)
                .build();

        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);
        log.info("Created chat room {} for collaboration: {}", savedRoom.getUuid(), collaborationId);
        return savedRoom;
    }

    private String generateRoomName(CollaborationProposal proposal) {
        String proposerName = "";
        if (proposal.getProposerUser() != null) {
            proposerName = proposal.getProposerUser().getUsername();
        } else if (proposal.getProposerStore() != null) {
            proposerName = proposal.getProposerStore().getName();
        }
        
        String targetName = proposal.getTargetStore() != null ? 
            proposal.getTargetStore().getName() : "Target Store";
            
        return String.format("%s - %s", proposerName, targetName);
    }

    private String generateRoomName(Collaboration collaboration) {
        String initiatorName = collaboration.getInitiatorStore() != null ? 
            collaboration.getInitiatorStore().getName() : "Initiator";
        String partnerName = collaboration.getPartnerStore() != null ? 
            collaboration.getPartnerStore().getName() : "Partner";
            
        return String.format("%s - %s", initiatorName, partnerName);
    }

    // Unread message management
    public Map<UUID, Long> getUnreadCountsByRoomsForUser(List<ChatRoom> chatRooms, User user) {
        return chatMessageRepository.getUnreadCountsByRoomsForUser(chatRooms, user);
    }

    public long getUnreadMessageCount(ChatRoom chatRoom, User user) {
        return chatMessageRepository.countUnreadMessagesInRoom(chatRoom, user);
    }

    @Transactional
    public void markMessagesAsRead(ChatRoom chatRoom, User user) {
        chatMessageRepository.markMessagesAsRead(chatRoom, user);
        log.info("Marked messages as read for user {} in room {}", user.getUuid(), chatRoom.getUuid());
    }

    // Helper method to safely get a compatible message type
    private ChatMessage.MessageType getSafeMessageType(ChatMessage.MessageType requestedType) {
        // Known problematic types that violate database constraints
        // These are newer enum values that may not be in the database check constraint
        if (requestedType == ChatMessage.MessageType.PROPOSAL_ACCEPTED ||
            requestedType == ChatMessage.MessageType.PROPOSAL_REJECTED ||
            requestedType == ChatMessage.MessageType.PROPOSAL_MODIFIED ||
            requestedType == ChatMessage.MessageType.PROPOSAL_STATUS_CHANGE) {
            
            log.info("Converting potentially problematic message type {} to TEXT to avoid constraint violation", requestedType);
            return ChatMessage.MessageType.TEXT;
        }
        
        // For other types, return as-is (TEXT, PROPOSAL_SHARE, PROPOSAL_UPDATE should be safe)
        return requestedType;
    }

    // Proposal event messaging
    @Transactional
    public void sendProposalStatusMessage(ChatRoom chatRoom, ChatMessage.MessageType messageType, 
                                        String statusMessage, CollaborationProposal proposal, User actionUser) {
        
        // Validate required parameters
        if (chatRoom == null) {
            log.error("Cannot send proposal status message: chatRoom is null");
            return;
        }
        
        if (messageType == null) {
            log.warn("Message type is null, defaulting to TEXT");
            messageType = ChatMessage.MessageType.TEXT;
        } else {
            // Use safe message type to handle potential database constraint issues
            ChatMessage.MessageType originalType = messageType;
            messageType = getSafeMessageType(messageType);
            
            // If we converted the message type, modify the message to indicate the original intent
            if (originalType != messageType) {
                switch (originalType) {
                    case PROPOSAL_ACCEPTED:
                        statusMessage = "✅ " + statusMessage;
                        break;
                    case PROPOSAL_REJECTED:
                        statusMessage = "❌ " + statusMessage;
                        break;
                    case PROPOSAL_MODIFIED:
                        statusMessage = "✏️ " + statusMessage;
                        break;
                    case PROPOSAL_STATUS_CHANGE:
                        statusMessage = "🔄 " + statusMessage;
                        break;
                    default:
                        statusMessage = "📝 " + statusMessage;
                }
            }
        }
        
        if (statusMessage == null || statusMessage.trim().isEmpty()) {
            log.warn("Status message is null or empty, using default message");
            statusMessage = "Proposal status updated";
        }
        
        if (actionUser == null) {
            log.error("Cannot send proposal status message: actionUser is null");
            return;
        }
        
        try {
            ChatMessage systemMessage = new ChatMessage();
            systemMessage.setChatRoom(chatRoom);
            systemMessage.setSender(actionUser);
            systemMessage.setMessage(statusMessage);
            
            // Handle message type with fallback for database constraint issues
            try {
                systemMessage.setMessageType(messageType);
            } catch (Exception e) {
                log.warn("Failed to set message type {}, falling back to TEXT: {}", messageType, e.getMessage());
                systemMessage.setMessageType(ChatMessage.MessageType.TEXT);
            }
            
            systemMessage.setCollaborationProposal(proposal);
            
            // Store proposal data as JSON for potential future use
            if (proposal != null) {
                try {
                    Map<String, Object> proposalData = new HashMap<>();
                    proposalData.put("proposalId", proposal.getUuid() != null ? proposal.getUuid() : "");
                    proposalData.put("title", proposal.getTitle() != null ? proposal.getTitle() : "");
                    proposalData.put("status", proposal.getStatus() != null ? proposal.getStatus().name() : "UNKNOWN");
                    proposalData.put("actionUserId", actionUser.getUuid() != null ? actionUser.getUuid() : "");
                    proposalData.put("timestamp", LocalDateTime.now().toString());
                    
                    systemMessage.setProposalData(objectMapper.writeValueAsString(proposalData));
                } catch (JsonProcessingException e) {
                    log.warn("Failed to serialize proposal data for message: {}", e.getMessage());
                    // Continue without proposal data rather than failing
                }
            }
            
            ChatMessage savedMessage;
            try {
                savedMessage = chatMessageRepository.save(systemMessage);
                log.info("Created proposal system message: {} in room {}", messageType, chatRoom.getUuid());
            } catch (Exception saveException) {
                String errorMessage = saveException.getMessage() != null ? saveException.getMessage() : "";
                
                // Check for specific database constraint violation
                if (errorMessage.contains("chat_messages_message_type_check") || 
                    errorMessage.contains("violates check constraint") ||
                    errorMessage.contains("PROPOSAL_ACCEPTED")) {
                    
                    log.warn("Database constraint violation for message type {}, using TEXT fallback", messageType);
                    
                    // Fallback: try saving with TEXT type and modified message
                    systemMessage.setMessageType(ChatMessage.MessageType.TEXT);
                    systemMessage.setMessage("📝 " + statusMessage + " (시스템 알림)");
                    
                    try {
                        savedMessage = chatMessageRepository.save(systemMessage);
                        log.info("Successfully saved fallback message as TEXT type in room {}", chatRoom.getUuid());
                    } catch (Exception fallbackException) {
                        log.error("Failed to save even TEXT fallback message: {}", fallbackException.getMessage());
                        return; // Give up on chat notification but don't break the calling flow
                    }
                } else {
                    // Different type of database error
                    log.error("Database error saving chat message (not constraint violation): {}", errorMessage);
                    return; // Give up but don't break the calling flow
                }
            }
            
            // Broadcast via WebSocket
            broadcastProposalMessage(chatRoom.getUuid(), savedMessage);
            
        } catch (Exception e) {
            log.error("Failed to send proposal status message for room {}: {}", 
                     chatRoom.getUuid(), e.getMessage(), e);
            // Don't throw exception to avoid breaking the calling flow
        }
    }
    
    private void broadcastProposalMessage(UUID roomId, ChatMessage message) {
        try {
            // Validate required fields before broadcasting
            if (message == null) {
                log.warn("Cannot broadcast null message for room {}", roomId);
                return;
            }
            
            if (message.getUuid() == null) {
                log.warn("Cannot broadcast message with null UUID for room {}", roomId);
                return;
            }
            
            // Create message data for WebSocket broadcast with null-safe operations
            Map<String, Object> wsMessage = new HashMap<>();
            wsMessage.put("messageId", message.getUuid());
            wsMessage.put("messageType", message.getMessageType() != null ? message.getMessageType().name() : "TEXT");
            wsMessage.put("message", message.getMessage() != null ? message.getMessage() : "");
            wsMessage.put("createdAt", message.getCreatedAt() != null ? message.getCreatedAt() : LocalDateTime.now());
            wsMessage.put("proposalData", message.getProposalData() != null ? message.getProposalData() : "");
            
            // Handle sender information with null checks
            if (message.getSender() != null) {
                Map<String, Object> senderData = new HashMap<>();
                senderData.put("uuid", message.getSender().getUuid() != null ? message.getSender().getUuid() : "");
                senderData.put("username", message.getSender().getUsername() != null ? message.getSender().getUsername() : "Anonymous");
                wsMessage.put("sender", senderData);
            } else {
                // Fallback sender data
                Map<String, Object> senderData = new HashMap<>();
                senderData.put("uuid", "");
                senderData.put("username", "System");
                wsMessage.put("sender", senderData);
                log.warn("Message has null sender, using system fallback for room {}", roomId);
            }
            
            messagingTemplate.convertAndSend("/topic/chat/" + roomId, wsMessage);
            log.info("Broadcasted proposal message to room {}", roomId);
        } catch (Exception e) {
            log.error("Failed to broadcast proposal message for room {}: {}", roomId, e.getMessage(), e);
        }
    }
    
    public Optional<ChatRoom> findChatRoomByProposal(CollaborationProposal proposal) {
        return chatRoomRepository.findByCollaborationProposal(proposal);
    }
}