package org.balanceus.topping.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.balanceus.topping.domain.model.Collaboration;
import org.balanceus.topping.domain.model.CollaborationProposal;
import org.balanceus.topping.domain.model.ChatRoom;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.model.ProposalSource;
import org.balanceus.topping.domain.model.Collaboration.CollaborationStatus;
import org.balanceus.topping.domain.repository.CollaborationRepository;
import org.balanceus.topping.domain.repository.CollaborationProposalRepository;
import org.balanceus.topping.domain.repository.StoreRepository;
import org.balanceus.topping.infrastructure.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CollaborationService {

    private final CollaborationRepository collaborationRepository;
    private final CollaborationProposalRepository collaborationProposalRepository;
    private final StoreRepository storeRepository;
    private final ChatService chatService;
    private final NotificationService notificationService;

    /**
     * Create a new collaboration proposal
     */
    public CollaborationProposal propose(ProposalSource source, User proposerUser, Store proposerStore, 
                                       Store targetStore, Product proposerProduct, Product targetProduct, 
                                       String title, String description, LocalDate startDate, LocalDate endDate) {
        
        // Validate proposal source and corresponding proposer
        if (source == ProposalSource.BUSINESS_OWNER && proposerStore == null) {
            throw new IllegalArgumentException("Business owner proposals require a proposer store");
        }
        if (source == ProposalSource.CUSTOMER && proposerUser == null) {
            throw new IllegalArgumentException("Customer proposals require a proposer user");
        }
        
        // Check for existing active proposals between the same entities
        // TODO: Implement duplicate prevention logic based on requirements
        
        CollaborationProposal proposal = new CollaborationProposal();
        proposal.setSource(source);
        proposal.setProposerUser(proposerUser);
        proposal.setProposerStore(proposerStore);
        proposal.setTargetStore(targetStore);
        proposal.setProposerProduct(proposerProduct);
        proposal.setTargetProduct(targetProduct);
        proposal.setTitle(title);
        proposal.setDescription(description);
        proposal.setProposedStart(startDate);
        proposal.setProposedEnd(endDate);
        proposal.setStatus(org.balanceus.topping.domain.model.CollaborationProposal.CollaborationStatus.PENDING);
        
        CollaborationProposal savedProposal = collaborationProposalRepository.save(proposal);
        log.info("Created collaboration proposal: {}", savedProposal.getUuid());
        
        return savedProposal;
    }

    /**
     * Approve a collaboration proposal and create a Collaboration entity
     */
    public Collaboration approve(UUID proposalId) {
        Optional<CollaborationProposal> proposalOpt = collaborationProposalRepository.findById(proposalId);
        if (proposalOpt.isEmpty()) {
            throw new RuntimeException("Collaboration proposal not found with id: " + proposalId);
        }

        CollaborationProposal proposal = proposalOpt.get();
        
        if (proposal.getStatus() != org.balanceus.topping.domain.model.CollaborationProposal.CollaborationStatus.PENDING) {
            throw new RuntimeException("Proposal is not in pending status");
        }

        // Check for existing active collaborations to prevent duplicates
        Optional<Collaboration> existingCollaboration = collaborationRepository
            .findActiveCollaborationBetweenStoresAndProducts(
                proposal.getProposerStore() != null ? proposal.getProposerStore() : proposal.getTargetStore(),
                proposal.getTargetStore(),
                proposal.getProposerProduct(),
                proposal.getTargetProduct()
            );
            
        if (existingCollaboration.isPresent()) {
            throw new RuntimeException("Active collaboration already exists between these stores and products");
        }

        // Create new Collaboration from proposal
        Collaboration collaboration = new Collaboration();
        
        // Determine initiator and partner based on proposal source
        if (proposal.getSource() == ProposalSource.BUSINESS_OWNER) {
            collaboration.setInitiatorStore(proposal.getProposerStore());
            collaboration.setPartnerStore(proposal.getTargetStore());
        } else {
            // For customer proposals, we need to get the customer's store if they have one
            // or use the target store as partner and proposer user's store (if available) as initiator
            Store customerStore = proposal.getProposerUser() != null ? 
                storeRepository.findByUser(proposal.getProposerUser()).orElse(null) : null;
            
            collaboration.setInitiatorStore(customerStore);
            collaboration.setPartnerStore(proposal.getTargetStore());
        }
        
        collaboration.setInitiatorProduct(proposal.getProposerProduct());
        collaboration.setPartnerProduct(proposal.getTargetProduct());
        collaboration.setStartDate(proposal.getProposedStart());
        collaboration.setEndDate(proposal.getProposedEnd());
        collaboration.setTitle(proposal.getTitle());
        collaboration.setDescription(proposal.getDescription());
        collaboration.setStatus(CollaborationStatus.ACCEPTED);

        Collaboration savedCollaboration = collaborationRepository.save(collaboration);

        // Update proposal status and link to collaboration
        proposal.setStatus(org.balanceus.topping.domain.model.CollaborationProposal.CollaborationStatus.ACCEPTED);
        proposal.setCollaboration(savedCollaboration);
        collaborationProposalRepository.save(proposal);

        try {
            ChatRoom chatRoom = chatService.createChatRoomForCollaboration(savedCollaboration.getUuid());
            log.info("Created chat room {} for approved collaboration: {}", 
                    chatRoom.getUuid(), savedCollaboration.getUuid());
        } catch (Exception e) {
            log.error("Failed to create chat room for collaboration: {}", savedCollaboration.getUuid(), e);
        }

        log.info("Collaboration proposal approved and collaboration created: {}", savedCollaboration.getUuid());
        return savedCollaboration;
    }

    /**
     * Reject a collaboration proposal
     */
    public void reject(UUID proposalId) {
        Optional<CollaborationProposal> proposalOpt = collaborationProposalRepository.findById(proposalId);
        if (proposalOpt.isEmpty()) {
            throw new RuntimeException("Collaboration proposal not found with id: " + proposalId);
        }

        CollaborationProposal proposal = proposalOpt.get();
        proposal.setStatus(org.balanceus.topping.domain.model.CollaborationProposal.CollaborationStatus.REJECTED);
        collaborationProposalRepository.save(proposal);

        log.info("Collaboration proposal rejected: {}", proposalId);
    }

    /**
     * Legacy method for accepting collaborations (maintained for compatibility)
     */
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

        log.info("Collaboration accepted: {}", collaborationId);
    }

    /**
     * Legacy method for rejecting collaborations (maintained for compatibility)
     */
    public void rejectCollaboration(UUID collaborationId) {
        Optional<Collaboration> collaborationOpt = collaborationRepository.findById(collaborationId);
        if (collaborationOpt.isEmpty()) {
            throw new RuntimeException("Collaboration not found with id: " + collaborationId);
        }

        Collaboration collaboration = collaborationOpt.get();
        collaboration.setStatus(CollaborationStatus.REJECTED);
        collaborationRepository.save(collaboration);

        log.info("Collaboration rejected: {}", collaborationId);
    }
}