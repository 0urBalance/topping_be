# Collaboration Domain - Claude Guidance

## Overview
The Collaboration domain handles business matching, partnership proposals, and collaborative project management for the Topping platform.

## Core Entities
- **Collaboration** - Accepted collaborations with chat integration
- **CollaborationProposal** - Pending collaboration proposals
- **ProposalSource** - Enum for proposal origin (GUEST_TO_BUSINESS, BUSINESS_TO_BUSINESS)

## Dual Entity Architecture

### Entity Relationships
```java
// Collaboration (Accepted collaborations)
@Entity
public class Collaboration {
    @Id @GeneratedValue private UUID uuid;
    private String title;
    private String description; // ⚠️ Use 'description', NOT 'message'
    @ManyToOne private Product initiatorProduct;
    @ManyToOne private Product partnerProduct; // Can be null for services
    @ManyToOne private Store initiatorStore;
    @ManyToOne private Store partnerStore;
    @ManyToOne private User initiator;
    @ManyToOne private User partner;
}

// CollaborationProposal (Pending proposals)
@Entity
public class CollaborationProposal {
    @Id @GeneratedValue private UUID uuid;
    private String title;
    private String description;
    @ManyToOne private Product initiatorProduct;
    @ManyToOne private Product partnerProduct;
    @ManyToOne private Store initiatorStore;
    @ManyToOne private Store partnerStore;
    @Enumerated private ProposalSource source;
}
```

### Repository Pattern
```java
// Separate repositories for each entity
// CollaborationRepository - for accepted collaborations
// CollaborationProposalRepository - for pending proposals

// Service method for unified view
public List<Object> getAllUserCollaborations(User user) {
    List<Collaboration> collaborations = collaborationRepository.findByUser(user);
    List<CollaborationProposal> proposals = proposalRepository.findByUser(user);
    // Combine and return unified view
}
```

## Template Field References

### Critical Field Mappings
```html
<!-- ✅ Correct field references -->
<h2 th:text="${collaboration.title}">Collaboration Title</h2>
<p th:text="${collaboration.description}">Description</p>

<!-- Product conditional logic -->
<div th:if="${collaboration.partnerProduct != null}">
    <span th:text="${collaboration.partnerProduct.title}">Partner Product</span>
</div>
<div th:unless="${collaboration.partnerProduct != null}">
    <span th:text="${collaboration.initiatorProduct.title}">Initiator Product</span>
</div>

<!-- ❌ Common mistakes to avoid -->
<!-- th:text="${collaboration.product}" --> <!-- Field doesn't exist -->
<!-- th:text="${collaboration.message}" --> <!-- Use 'description' -->
<!-- th:text="${collaboration.applicantProduct}" --> <!-- Use 'initiatorProduct' -->
```

### MyPage Integration Templates
```html
<!-- Unified received page showing both entities -->
<div class="collaboration-section">
    <h3>받은 제안서 (CollaborationProposal)</h3>
    <div th:each="proposal : ${proposals}">
        <div class="proposal-card">
            <h4 th:text="${proposal.title}">Proposal Title</h4>
            <p th:text="${proposal.description}">Proposal Description</p>
            <div th:if="${proposal.partnerProduct != null}">
                <span th:text="${proposal.partnerProduct.title}">Product Name</span>
            </div>
        </div>
    </div>
</div>

<div class="collaboration-section">
    <h3>진행 중인 콜라보 (Collaboration)</h3>
    <div th:each="collabo : ${collaborations}">
        <div class="collaboration-card">
            <h4 th:text="${collabo.title}">Collaboration Title</h4>
            <p th:text="${collabo.description}">Collaboration Description</p>
        </div>
    </div>
</div>
```

## Controller Patterns

### Proposal Acceptance Flow
```java
@PostMapping("/collaborations/{proposalId}/accept")
public String acceptProposal(@PathVariable UUID proposalId, 
                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
    // 1. Find the proposal
    CollaborationProposal proposal = proposalService.findById(proposalId);
    
    // 2. Create accepted collaboration
    Collaboration collaboration = collaborationService.acceptProposal(proposal, userDetails.getUser());
    
    // 3. Create chat room automatically
    chatService.createChatRoomForCollaboration(collaboration);
    
    // 4. Delete the proposal
    proposalService.delete(proposalId);
    
    return "redirect:/mypage/ongoing";
}
```

### Collaboration Application
```java
@PostMapping("/collaborations/apply")
public String applyCollaboration(@ModelAttribute CollaborationApplicationForm form,
                               @AuthenticationPrincipal UserDetailsImpl userDetails) {
    // Create proposal entity
    CollaborationProposal proposal = proposalService.createProposal(form, userDetails.getUser());
    
    // Automatic chat room creation happens only after acceptance
    return "redirect:/mypage/applications";
}
```

## Chat Integration

### Automatic Room Creation
```java
// Service method in ChatService
@Transactional
public ChatRoom createChatRoomForCollaboration(Collaboration collaboration) {
    // Check if room already exists
    Optional<ChatRoom> existingRoom = chatRoomRepository.findByCollaborationUuid(collaboration.getUuid());
    if (existingRoom.isPresent()) {
        return existingRoom.get();
    }
    
    // Create new room
    ChatRoom chatRoom = ChatRoom.builder()
        .name(collaboration.getTitle() + " 채팅방")
        .collaborationUuid(collaboration.getUuid())
        .build();
    
    return chatRoomRepository.save(chatRoom);
}

// Also supports CollaborationProposal
public ChatRoom createChatRoomForProposal(CollaborationProposal proposal) {
    // Similar logic for proposals
}
```

## Service Layer Patterns

### Collaboration Statistics
```java
public CollaborationStats getUserCollaborationStats(User user) {
    long ongoingCount = collaborationRepository.countByUserAndStatus(user, ONGOING);
    long proposalsCount = proposalRepository.countByTargetUser(user);
    long applicationsCount = proposalRepository.countByInitiator(user);
    
    return CollaborationStats.builder()
        .ongoingCollaborations(ongoingCount)
        .receivedProposals(proposalsCount)
        .sentApplications(applicationsCount)
        .build();
}
```

### Form Processing
```java
// Dynamic form system for collaboration applications
@Service
public class CollaborationService {
    
    public CollaborationProposal createApplicationProposal(CollaborationApplicationForm form, User applicant) {
        Store targetStore = storeRepository.findById(form.getStoreId());
        Product applicantProduct = productRepository.findById(form.getProductId());
        
        return CollaborationProposal.builder()
            .title(form.getTitle())
            .description(form.getDescription())
            .initiatorProduct(applicantProduct)
            .initiatorStore(applicant.getStore())
            .partnerStore(targetStore)
            .source(ProposalSource.GUEST_TO_BUSINESS)
            .build();
    }
}
```

## Common Pitfalls
- ❌ **Field References**: Never use `collaboration.product` → use conditional logic with `partnerProduct`/`initiatorProduct`
- ❌ **Message Field**: Use `collaboration.description`, NOT `collaboration.message`
- ❌ **Applicant Fields**: Use `initiatorProduct`, NOT `applicantProduct`
- ❌ **Chat Room Creation**: Only create rooms for accepted collaborations, not proposals
- ❌ **Entity Confusion**: Distinguish between `Collaboration` (accepted) and `CollaborationProposal` (pending)
- ❌ **Null Safety**: Always check if `partnerProduct` is null for service-based collaborations

## API Response Patterns
```java
// Unified collaboration data
@GetMapping("/api/collaborations/summary")
public ResponseEntity<ApiResponseData<CollaborationSummaryResponse>> getCollaborationSummary(
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
    CollaborationSummaryResponse response = collaborationService.getSummary(userDetails.getUser());
    return ResponseEntity.ok(ApiResponseData.success(response));
}
```

## Integration Points
- **Chat Domain**: Automatic chat room creation for accepted collaborations
- **Product Domain**: Product-based and service-based collaborations
- **Store Domain**: Store-to-store business partnerships
- **User Domain**: User roles and ownership verification

## Testing Patterns
```java
@ActiveProfiles("test")
class CollaborationServiceTest {
    // Test proposal creation and acceptance flow
    // Verify chat room creation integration
    // Test dual entity queries and statistics
}
```

## Related Documentation
- [Main Claude Guidance](../../../CLAUDE.md)
- [Chat Domain](../chat/CLAUDE.md)
- [Collaboration Received Page](../../technical/COLLABORATION_RECEIVED_PAGE.md)
- [Collaboration Forms](../../technical/collaboration-forms.md)