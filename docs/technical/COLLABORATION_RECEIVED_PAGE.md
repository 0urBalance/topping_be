# Collaboration Received Page Enhancement

## Overview
The `/mypage/received` page has been enhanced to support a dual collaboration system, displaying both guest applications and business proposals in a unified interface.

## Problem Solved
**Issue**: Statistics showed correct combined counts but the page body only displayed guest applications (`Collaboration` entities), while business proposals (`CollaborationProposal` entities) were counted but not displayed.

**Root Cause**: Template had display loops only for `pendingReceived` but not `pendingProposals`, despite controller loading both types.

## Architecture Changes

### 1. Controller Enhancement (`MyPageController.java`)
```java
// Added proposal data loading
List<CollaborationProposal> receivedProposals = proposalRepository.findByTargetBusinessOwner(user);

// Updated statistics to include both types
int receivedApplicationCount = receivedApplications.size() + receivedProposals.size();

// Enhanced pending actions count
int pendingActionsCount = receivedApplications.stream()
    .mapToInt(collab -> collab.getStatus() == Collaboration.CollaborationStatus.PENDING ? 1 : 0)
    .sum() + 
    receivedProposals.stream()
    .mapToInt(prop -> prop.getStatus() == CollaborationProposal.ProposalStatus.PENDING ? 1 : 0)
    .sum();
```

### 2. Template Structure (`mypage/received.html`)

#### Section Organization
- **Pending Section**: Displays both `pendingReceived` and `pendingProposals`
- **All Section**: Displays both `receivedApplications` and `receivedProposals`  
- **Accepted Section**: Displays both `acceptedReceived` and `acceptedProposals`
- **Rejected Section**: Displays both `rejectedReceived` and `rejectedProposals`

#### Card Templates
**Application Cards**: Display product-based collaboration data
```html
<div th:each="receivedApp : ${pendingReceived}" class="application-card pending">
    <!-- Product information, applicant details, message -->
</div>
```

**Proposal Cards**: Display business partnership data
```html
<div th:each="pendingProp : ${pendingProposals}" class="application-card pending">
    <!-- Title, description, category, proposer details -->
</div>
```

### 3. Action Management (`CollaborationProposalController.java`)
Added form-based endpoints for seamless page integration:
```java
@PostMapping("/{proposalId}/accept/form")
public String acceptProposalForm(@PathVariable UUID proposalId, Principal principal) {
    // Authorization and validation logic
    // Status update and notification
    return "redirect:/mypage/received?success=proposal_accepted";
}

@PostMapping("/{proposalId}/reject/form") 
public String rejectProposalForm(@PathVariable UUID proposalId, Principal principal) {
    // Similar logic for rejection
    return "redirect:/mypage/received?success=proposal_rejected";
}
```

## Data Flow

### 1. Guest Application Flow
```
Guest User → /collaborations/apply → Collaboration Entity → /mypage/received (Applications)
```

### 2. Business Proposal Flow  
```
Business Owner → /proposals/suggest → CollaborationProposal Entity → /mypage/received (Proposals)
```

### 3. Unified Display
```
MyPageController → Load both Applications & Proposals → Template displays unified view
```

## UI/UX Features

### Statistics Integration
- **Combined Counts**: All statistics show applications + proposals
- **Consistent Numbers**: `/mypage` and `/mypage/received` show same counts
- **Real-time Updates**: Counts update after accept/reject actions

### Visual Distinction
- **Application Cards**: Blue accent, product-focused layout
- **Proposal Cards**: Green accent, partnership-focused layout
- **Status Indicators**: Different styling for pending/accepted/rejected

### Empty States
- **Smart Logic**: Only shows empty state when both types are empty
- **Context-Aware**: Different messages for different sections

## Success/Error Handling

### Message Types
- `collaboration_accepted` / `collaboration_rejected` - For applications
- `proposal_accepted` / `proposal_rejected` - For proposals
- `collaboration_not_found` / `proposal_not_found` - Entity errors
- `unauthorized_action` / `already_processed` - Action errors

### User Feedback
```html
<div th:if="${param.success[0] == 'proposal_accepted'}" class="alert-content">
    <strong>✅ 성공!</strong> 콜라보 제안을 수락했습니다.
</div>
```

## Performance Considerations

### Query Optimization
- Separate queries for applications and proposals
- Sorted by creation date (newest first)
- Filtered by status in Java streams (small datasets)

### Template Efficiency
- Minimal duplicate loops
- Consistent empty state checks
- Optimized Thymeleaf expressions

## Testing Strategy

### Controller Testing
- Verify both entity types are loaded
- Check statistics calculations
- Validate model attributes

### Integration Testing
- Test accept/reject actions for both types
- Verify redirect URLs and success messages
- Check authorization for different user roles

### Template Testing
- Verify both types display in all sections
- Check empty states with various data combinations
- Validate action button rendering

## Maintenance Notes

### Adding New Collaboration Types
1. Update controller to load new entity type
2. Add new template loop in each section
3. Create corresponding action endpoints
4. Update statistics calculations
5. Add success/error message handling

### Debugging Display Issues
1. Check controller model attributes
2. Verify template loop variables match controller
3. Ensure empty state conditions include all types
4. Validate Thymeleaf field references

## Related Documentation
- [Collaboration Domain](../domains/collaboration/README.md)
- [MyPage Features](../domains/user/README.md#mypage-features)
- [Template System](./css-framework.md)