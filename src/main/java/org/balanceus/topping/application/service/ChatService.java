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
import java.util.ArrayList;
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

        // Find the originating proposal for this collaboration
        CollaborationProposal originatingProposal = findOriginatingProposal(collaboration);
        if (originatingProposal == null) {
            log.warn("No originating proposal found for collaboration: {}", collaborationId);
        }

        ChatRoom chatRoom = ChatRoom.builder()
                .collaboration(collaboration)
                .collaborationProposal(originatingProposal) // CRITICAL: Link to originating proposal
                .roomName(generateRoomName(collaboration, originatingProposal))
                .isActive(true)
                .build();

        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);
        log.info("Created chat room {} for collaboration: {} with proposal link: {}", 
                savedRoom.getUuid(), collaborationId, 
                originatingProposal != null ? originatingProposal.getUuid() : "none");
        return savedRoom;
    }

    /**
     * Find the originating proposal for a given collaboration.
     * This method implements the canonical lookup logic to ensure proper proposal-chat linkage.
     */
    private CollaborationProposal findOriginatingProposal(Collaboration collaboration) {
        if (collaboration == null) {
            return null;
        }
        
        try {
            // Method 1: Direct lookup by collaboration reference
            // Look for a proposal that has this collaboration linked to it
            List<CollaborationProposal> proposalsWithCollaboration = 
                collaborationProposalRepository.findAll().stream()
                .filter(proposal -> proposal.getCollaboration() != null && 
                               proposal.getCollaboration().getUuid().equals(collaboration.getUuid()))
                .toList();
                
            if (!proposalsWithCollaboration.isEmpty()) {
                if (proposalsWithCollaboration.size() > 1) {
                    log.warn("Multiple proposals found for collaboration {}, using first one", 
                             collaboration.getUuid());
                }
                return proposalsWithCollaboration.get(0);
            }
            
            // Method 2: Heuristic lookup by matching attributes
            // If no direct link exists, try to find proposal by matching store and product combinations
            List<CollaborationProposal> candidateProposals = collaborationProposalRepository
                .findByStatus(CollaborationProposal.CollaborationStatus.ACCEPTED);
                
            for (CollaborationProposal proposal : candidateProposals) {
                if (isProposalMatchForCollaboration(proposal, collaboration)) {
                    log.info("Found matching proposal {} for collaboration {} via heuristic lookup", 
                             proposal.getUuid(), collaboration.getUuid());
                    return proposal;
                }
            }
            
            log.warn("No originating proposal found for collaboration: {}", collaboration.getUuid());
            return null;
            
        } catch (Exception e) {
            log.error("Error finding originating proposal for collaboration {}: {}", 
                     collaboration.getUuid(), e.getMessage());
            return null;
        }
    }
    
    /**
     * Heuristic method to check if a proposal matches a collaboration based on participants and products
     */
    private boolean isProposalMatchForCollaboration(CollaborationProposal proposal, Collaboration collaboration) {
        // Check if stores match
        boolean storesMatch = false;
        if (proposal.getTargetStore() != null && collaboration.getPartnerStore() != null) {
            storesMatch = proposal.getTargetStore().getUuid().equals(collaboration.getPartnerStore().getUuid());
        }
        if (proposal.getProposerStore() != null && collaboration.getInitiatorStore() != null) {
            storesMatch = storesMatch || proposal.getProposerStore().getUuid().equals(collaboration.getInitiatorStore().getUuid());
        }
        
        // Check if products match
        boolean productsMatch = false;
        if (proposal.getTargetProduct() != null && collaboration.getPartnerProduct() != null) {
            productsMatch = proposal.getTargetProduct().getUuid().equals(collaboration.getPartnerProduct().getUuid());
        }
        if (proposal.getProposerProduct() != null && collaboration.getInitiatorProduct() != null) {
            productsMatch = productsMatch || proposal.getProposerProduct().getUuid().equals(collaboration.getInitiatorProduct().getUuid());
        }
        
        // Check if titles match (loose comparison)
        boolean titlesMatch = false;
        if (proposal.getTitle() != null && collaboration.getTitle() != null) {
            titlesMatch = proposal.getTitle().equals(collaboration.getTitle());
        }
        
        // Require at least 2 out of 3 criteria to match
        int matchCount = (storesMatch ? 1 : 0) + (productsMatch ? 1 : 0) + (titlesMatch ? 1 : 0);
        return matchCount >= 2;
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

    private String generateRoomName(Collaboration collaboration, CollaborationProposal originatingProposal) {
        // Prefer proposal information if available (more detailed and original)
        if (originatingProposal != null) {
            return generateRoomName(originatingProposal);
        }
        
        // Fallback to collaboration information
        return generateRoomName(collaboration);
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
    
    /**
     * Backfill missing proposal links for existing chat rooms.
     * This method repairs chat rooms that have collaboration but missing collaborationProposal reference.
     * Safe to run multiple times (idempotent).
     */
    @Transactional
    public BackfillResult backfillMissingProposalLinks() {
        log.info("Starting backfill of missing proposal links for chat rooms");
        
        BackfillResult result = new BackfillResult();
        
        try {
            // Find chat rooms that have collaboration but missing collaborationProposal
            List<ChatRoom> roomsNeedingBackfill = chatRoomRepository.findAll().stream()
                .filter(room -> room.getCollaboration() != null && room.getCollaborationProposal() == null)
                .toList();
                
            log.info("Found {} chat rooms needing proposal backfill", roomsNeedingBackfill.size());
            result.setTotalRoomsFound(roomsNeedingBackfill.size());
            
            for (ChatRoom room : roomsNeedingBackfill) {
                try {
                    CollaborationProposal originatingProposal = findOriginatingProposal(room.getCollaboration());
                    
                    if (originatingProposal != null) {
                        room.setCollaborationProposal(originatingProposal);
                        chatRoomRepository.save(room);
                        result.incrementSuccessfullyLinked();
                        
                        log.info("Backfilled proposal link for chat room {} -> proposal {}", 
                                room.getUuid(), originatingProposal.getUuid());
                    } else {
                        result.incrementNoProposalFound();
                        log.warn("No originating proposal found for chat room {} with collaboration {}", 
                                room.getUuid(), room.getCollaboration().getUuid());
                    }
                    
                } catch (Exception e) {
                    result.incrementErrors();
                    log.error("Error backfilling proposal link for chat room {}: {}", 
                             room.getUuid(), e.getMessage(), e);
                }
            }
            
            log.info("Backfill completed: {} successful, {} no proposal found, {} errors", 
                    result.getSuccessfullyLinked(), result.getNoProposalFound(), result.getErrors());
                    
        } catch (Exception e) {
            log.error("Error during backfill operation: {}", e.getMessage(), e);
            result.setOverallError(e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Get statistics about chat room proposal linkage for monitoring/validation
     */
    public LinkageStatistics getProposalLinkageStatistics() {
        try {
            List<ChatRoom> allRooms = chatRoomRepository.findAll();
            
            LinkageStatistics stats = new LinkageStatistics();
            stats.setTotalChatRooms(allRooms.size());
            
            long roomsWithProposalOnly = allRooms.stream()
                .filter(room -> room.getCollaborationProposal() != null && room.getCollaboration() == null)
                .count();
            stats.setRoomsWithProposalOnly(roomsWithProposalOnly);
            
            long roomsWithCollaborationOnly = allRooms.stream()
                .filter(room -> room.getCollaboration() != null && room.getCollaborationProposal() == null)
                .count();
            stats.setRoomsWithCollaborationOnly(roomsWithCollaborationOnly);
            
            long roomsWithBothLinks = allRooms.stream()
                .filter(room -> room.getCollaboration() != null && room.getCollaborationProposal() != null)
                .count();
            stats.setRoomsWithBothLinks(roomsWithBothLinks);
            
            long roomsWithNeitherLink = allRooms.stream()
                .filter(room -> room.getCollaboration() == null && room.getCollaborationProposal() == null)
                .count();
            stats.setRoomsWithNeitherLink(roomsWithNeitherLink);
            
            return stats;
            
        } catch (Exception e) {
            log.error("Error gathering linkage statistics: {}", e.getMessage(), e);
            LinkageStatistics errorStats = new LinkageStatistics();
            errorStats.setError(e.getMessage());
            return errorStats;
        }
    }
    
    // Result classes for backfill operations
    public static class BackfillResult {
        private int totalRoomsFound = 0;
        private int successfullyLinked = 0;
        private int noProposalFound = 0;
        private int errors = 0;
        private String overallError;
        
        public int getTotalRoomsFound() { return totalRoomsFound; }
        public void setTotalRoomsFound(int totalRoomsFound) { this.totalRoomsFound = totalRoomsFound; }
        
        public int getSuccessfullyLinked() { return successfullyLinked; }
        public void incrementSuccessfullyLinked() { this.successfullyLinked++; }
        
        public int getNoProposalFound() { return noProposalFound; }
        public void incrementNoProposalFound() { this.noProposalFound++; }
        
        public int getErrors() { return errors; }
        public void incrementErrors() { this.errors++; }
        
        public String getOverallError() { return overallError; }
        public void setOverallError(String overallError) { this.overallError = overallError; }
        
        public boolean isSuccessful() { return overallError == null && errors == 0; }
    }
    
    public static class LinkageStatistics {
        private int totalChatRooms;
        private long roomsWithProposalOnly;
        private long roomsWithCollaborationOnly;
        private long roomsWithBothLinks;
        private long roomsWithNeitherLink;
        private String error;
        
        public int getTotalChatRooms() { return totalChatRooms; }
        public void setTotalChatRooms(int totalChatRooms) { this.totalChatRooms = totalChatRooms; }
        
        public long getRoomsWithProposalOnly() { return roomsWithProposalOnly; }
        public void setRoomsWithProposalOnly(long roomsWithProposalOnly) { this.roomsWithProposalOnly = roomsWithProposalOnly; }
        
        public long getRoomsWithCollaborationOnly() { return roomsWithCollaborationOnly; }
        public void setRoomsWithCollaborationOnly(long roomsWithCollaborationOnly) { this.roomsWithCollaborationOnly = roomsWithCollaborationOnly; }
        
        public long getRoomsWithBothLinks() { return roomsWithBothLinks; }
        public void setRoomsWithBothLinks(long roomsWithBothLinks) { this.roomsWithBothLinks = roomsWithBothLinks; }
        
        public long getRoomsWithNeitherLink() { return roomsWithNeitherLink; }
        public void setRoomsWithNeitherLink(long roomsWithNeitherLink) { this.roomsWithNeitherLink = roomsWithNeitherLink; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        
        public double getHealthPercentage() {
            if (totalChatRooms == 0) return 100.0;
            return (double) roomsWithBothLinks / totalChatRooms * 100.0;
        }
    }
    
    /**
     * Validate the integrity of chat room proposal linkages.
     * This method performs comprehensive validation checks and reports any inconsistencies.
     */
    public ValidationReport validateProposalLinkageIntegrity() {
        log.info("Starting comprehensive validation of proposal linkage integrity");
        
        ValidationReport report = new ValidationReport();
        
        try {
            List<ChatRoom> allRooms = chatRoomRepository.findAll();
            report.setTotalRoomsChecked(allRooms.size());
            
            for (ChatRoom room : allRooms) {
                validateSingleRoom(room, report);
            }
            
            // Additional system-wide checks
            validateSystemWideConsistency(report);
            
            log.info("Validation completed: {} rooms checked, {} issues found", 
                    report.getTotalRoomsChecked(), report.getTotalIssues());
                    
        } catch (Exception e) {
            report.setOverallError("Validation failed: " + e.getMessage());
            log.error("Error during validation: {}", e.getMessage(), e);
        }
        
        return report;
    }
    
    private void validateSingleRoom(ChatRoom room, ValidationReport report) {
        try {
            // Check 1: Room has at least one linkage
            if (room.getCollaboration() == null && room.getCollaborationProposal() == null) {
                report.addIssue(ValidationIssue.orphanedRoom(room.getUuid()));
            }
            
            // Check 2: If room has collaboration, verify it exists
            if (room.getCollaboration() != null) {
                Optional<Collaboration> collaboration = collaborationRepository.findById(room.getCollaboration().getUuid());
                if (collaboration.isEmpty()) {
                    report.addIssue(ValidationIssue.brokenCollaborationLink(room.getUuid(), room.getCollaboration().getUuid()));
                }
            }
            
            // Check 3: If room has proposal, verify it exists  
            if (room.getCollaborationProposal() != null) {
                Optional<CollaborationProposal> proposal = collaborationProposalRepository.findById(room.getCollaborationProposal().getUuid());
                if (proposal.isEmpty()) {
                    report.addIssue(ValidationIssue.brokenProposalLink(room.getUuid(), room.getCollaborationProposal().getUuid()));
                }
            }
            
            // Check 4: If room has both links, verify they match
            if (room.getCollaboration() != null && room.getCollaborationProposal() != null) {
                CollaborationProposal proposal = room.getCollaborationProposal();
                if (proposal.getCollaboration() == null || 
                    !proposal.getCollaboration().getUuid().equals(room.getCollaboration().getUuid())) {
                    report.addIssue(ValidationIssue.mismatchedLinks(room.getUuid(), 
                        room.getCollaboration().getUuid(), room.getCollaborationProposal().getUuid()));
                }
            }
            
            // Check 5: Room missing proposal link (primary issue we're fixing)
            if (room.getCollaboration() != null && room.getCollaborationProposal() == null) {
                report.addIssue(ValidationIssue.missingProposalLink(room.getUuid(), room.getCollaboration().getUuid()));
            }
            
        } catch (Exception e) {
            report.addIssue(ValidationIssue.validationError(room.getUuid(), e.getMessage()));
        }
    }
    
    private void validateSystemWideConsistency(ValidationReport report) {
        try {
            // Check for duplicate chat rooms for same collaboration
            Map<UUID, List<ChatRoom>> roomsByCollaboration = chatRoomRepository.findAll().stream()
                .filter(room -> room.getCollaboration() != null)
                .collect(java.util.stream.Collectors.groupingBy(room -> room.getCollaboration().getUuid()));
                
            roomsByCollaboration.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .forEach(entry -> report.addIssue(ValidationIssue.duplicateRoomsForCollaboration(
                    entry.getKey(), entry.getValue().stream().map(ChatRoom::getUuid).toList())));
                    
            // Check for duplicate chat rooms for same proposal
            Map<UUID, List<ChatRoom>> roomsByProposal = chatRoomRepository.findAll().stream()
                .filter(room -> room.getCollaborationProposal() != null)
                .collect(java.util.stream.Collectors.groupingBy(room -> room.getCollaborationProposal().getUuid()));
                
            roomsByProposal.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .forEach(entry -> report.addIssue(ValidationIssue.duplicateRoomsForProposal(
                    entry.getKey(), entry.getValue().stream().map(ChatRoom::getUuid).toList())));
                    
        } catch (Exception e) {
            log.error("Error in system-wide validation: {}", e.getMessage(), e);
        }
    }
    
    // Validation result classes
    public static class ValidationReport {
        private int totalRoomsChecked = 0;
        private final List<ValidationIssue> issues = new ArrayList<>();
        private String overallError;
        
        public int getTotalRoomsChecked() { return totalRoomsChecked; }
        public void setTotalRoomsChecked(int totalRoomsChecked) { this.totalRoomsChecked = totalRoomsChecked; }
        
        public List<ValidationIssue> getIssues() { return issues; }
        public void addIssue(ValidationIssue issue) { this.issues.add(issue); }
        
        public int getTotalIssues() { return issues.size(); }
        
        public String getOverallError() { return overallError; }
        public void setOverallError(String overallError) { this.overallError = overallError; }
        
        public boolean isHealthy() { return overallError == null && issues.isEmpty(); }
        
        public long getIssueCountByType(String issueType) {
            return issues.stream().filter(issue -> issue.getType().equals(issueType)).count();
        }
    }
    
    public static class ValidationIssue {
        private String type;
        private String description;
        private UUID roomId;
        private UUID collaborationId;
        private UUID proposalId;
        private List<UUID> relatedRoomIds;
        
        public static ValidationIssue orphanedRoom(UUID roomId) {
            ValidationIssue issue = new ValidationIssue();
            issue.type = "ORPHANED_ROOM";
            issue.description = "Chat room has no collaboration or proposal linkage";
            issue.roomId = roomId;
            return issue;
        }
        
        public static ValidationIssue brokenCollaborationLink(UUID roomId, UUID collaborationId) {
            ValidationIssue issue = new ValidationIssue();
            issue.type = "BROKEN_COLLABORATION_LINK";
            issue.description = "Chat room references non-existent collaboration";
            issue.roomId = roomId;
            issue.collaborationId = collaborationId;
            return issue;
        }
        
        public static ValidationIssue brokenProposalLink(UUID roomId, UUID proposalId) {
            ValidationIssue issue = new ValidationIssue();
            issue.type = "BROKEN_PROPOSAL_LINK";
            issue.description = "Chat room references non-existent proposal";
            issue.roomId = roomId;
            issue.proposalId = proposalId;
            return issue;
        }
        
        public static ValidationIssue mismatchedLinks(UUID roomId, UUID collaborationId, UUID proposalId) {
            ValidationIssue issue = new ValidationIssue();
            issue.type = "MISMATCHED_LINKS";
            issue.description = "Chat room's collaboration and proposal links don't match";
            issue.roomId = roomId;
            issue.collaborationId = collaborationId;
            issue.proposalId = proposalId;
            return issue;
        }
        
        public static ValidationIssue missingProposalLink(UUID roomId, UUID collaborationId) {
            ValidationIssue issue = new ValidationIssue();
            issue.type = "MISSING_PROPOSAL_LINK";
            issue.description = "Chat room has collaboration but missing proposal link";
            issue.roomId = roomId;
            issue.collaborationId = collaborationId;
            return issue;
        }
        
        public static ValidationIssue duplicateRoomsForCollaboration(UUID collaborationId, List<UUID> roomIds) {
            ValidationIssue issue = new ValidationIssue();
            issue.type = "DUPLICATE_ROOMS_FOR_COLLABORATION";
            issue.description = "Multiple chat rooms exist for same collaboration";
            issue.collaborationId = collaborationId;
            issue.relatedRoomIds = roomIds;
            return issue;
        }
        
        public static ValidationIssue duplicateRoomsForProposal(UUID proposalId, List<UUID> roomIds) {
            ValidationIssue issue = new ValidationIssue();
            issue.type = "DUPLICATE_ROOMS_FOR_PROPOSAL";
            issue.description = "Multiple chat rooms exist for same proposal";
            issue.proposalId = proposalId;
            issue.relatedRoomIds = roomIds;
            return issue;
        }
        
        public static ValidationIssue validationError(UUID roomId, String errorMessage) {
            ValidationIssue issue = new ValidationIssue();
            issue.type = "VALIDATION_ERROR";
            issue.description = "Error during validation: " + errorMessage;
            issue.roomId = roomId;
            return issue;
        }
        
        // Getters
        public String getType() { return type; }
        public String getDescription() { return description; }
        public UUID getRoomId() { return roomId; }
        public UUID getCollaborationId() { return collaborationId; }
        public UUID getProposalId() { return proposalId; }
        public List<UUID> getRelatedRoomIds() { return relatedRoomIds; }
    }
}