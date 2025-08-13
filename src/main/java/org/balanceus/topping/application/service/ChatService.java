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
}