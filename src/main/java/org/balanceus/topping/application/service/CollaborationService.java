package org.balanceus.topping.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.balanceus.topping.domain.model.Collaboration;
import org.balanceus.topping.domain.model.ChatRoom;
import org.balanceus.topping.domain.model.Collaboration.CollaborationStatus;
import org.balanceus.topping.domain.repository.CollaborationRepository;
import org.balanceus.topping.infrastructure.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CollaborationService {

    private final CollaborationRepository collaborationRepository;
    private final ChatService chatService;
    private final NotificationService notificationService;

    public void acceptCollaboration(UUID collaborationId) {
        Optional<Collaboration> collaborationOpt = collaborationRepository.findById(collaborationId);
        if (collaborationOpt.isEmpty()) {
            throw new RuntimeException("Collaboration not found with id: " + collaborationId);
        }

        Collaboration collaboration = collaborationOpt.get();
        collaboration.setStatus(CollaborationStatus.ACCEPTED);
        collaborationRepository.save(collaboration);

        try {
            ChatRoom chatRoom = chatService.createChatRoomForCollaboration(collaborationId);
            log.info("Created chat room {} for accepted collaboration: {}", 
                    chatRoom.getUuid(), collaborationId);
        } catch (Exception e) {
            log.error("Failed to create chat room for collaboration: {}", collaborationId, e);
        }

        // Note: Notification will be added when NotificationService supports Collaboration entities
        log.info("Collaboration accepted: {}", collaborationId);
    }

    public void rejectCollaboration(UUID collaborationId) {
        Optional<Collaboration> collaborationOpt = collaborationRepository.findById(collaborationId);
        if (collaborationOpt.isEmpty()) {
            throw new RuntimeException("Collaboration not found with id: " + collaborationId);
        }

        Collaboration collaboration = collaborationOpt.get();
        collaboration.setStatus(CollaborationStatus.REJECTED);
        collaborationRepository.save(collaboration);

        // Note: Notification will be added when NotificationService supports Collaboration entities
        log.info("Collaboration rejected: {}", collaborationId);
    }
}