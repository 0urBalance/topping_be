# Collaboration Domain

## Overview
The collaboration system enables business matching and partnership management between users and business owners on the platform.

## Domain Models
- **Collaboration**: `Collaboration.java` - Core collaboration entity
- **CollaborationProposal**: `CollaborationProposal.java` - Proposal management
- **CollaborationProduct**: `CollaborationProduct.java` - Product collaboration features

## Repositories
Following the consistent three-layer pattern:
- **Domain Interfaces**: `CollaborationRepository`, `CollaborationProposalRepository`, `CollaborationProductRepository`
- **JPA Interfaces**: `CollaborationJpaRepository`, `CollaborationProposalJpaRepository`, `CollaborationProductJpaRepository`
- **Implementations**: `CollaborationRepositoryImpl`, `CollaborationProposalRepositoryImpl`, `CollaborationProductRepositoryImpl`

## Controllers
- `CollaborationController.java` - Main collaboration management
- `CollaborationProposalController.java` - Proposal submission and management
- `CollaborationProductController.java` - Product-specific collaboration features
- `CollaboController.java` - Collaboration UI and templates

## Key Features

### Collaboration Matching
- Business owners and users can initiate collaborations
- Matching system based on business requirements and user skills
- Role-based access control (ROLE_BUSINESS_OWNER vs ROLE_USER)

### Proposal System
- Structured proposal submission workflow
- Proposal status tracking and updates
- Notification system for proposal changes
- Business owner review and approval process

### Product Integration
- Collaboration tied to specific products
- Product-based collaboration workflows
- AI-based profit-sharing recommendations

## Collaboration Workflow
1. **Initiation**: Business owner or user initiates collaboration request
2. **Proposal**: Detailed proposal submitted with requirements and terms
3. **Review**: Counter-party reviews and responds to proposal
4. **Negotiation**: Back-and-forth negotiation through proposal updates
5. **Acceptance**: Final agreement and collaboration activation
6. **Execution**: Active collaboration with chat, product management, etc.

## Integration Points
- **User Domain**: Role-based collaboration access
- **Product Domain**: Product-specific collaboration features
- **Chat Domain**: Communication during active collaborations
- **Notification Domain**: Updates and alerts for collaboration events

## Templates and UI
- `collaboration-products/` - Product collaboration templates
- `collaborations/` - General collaboration templates
- `proposals/` - Proposal management UI
- `collabo.html` - Main collaboration interface

## API Endpoints
- Collaboration management endpoints
- Proposal submission and tracking
- Product collaboration features
- (Detailed endpoint documentation to be added as implemented)

## Business Logic
- **AI Integration**: Profit-sharing recommendation system
- **Status Management**: Collaboration and proposal state tracking
- **Access Control**: Role-based collaboration permissions
- **Matching Algorithm**: Business-user matching based on requirements

## Recent Development
- Added comprehensive collaboration proposal system
- Implemented notification system for proposal updates
- Enhanced core entities with collaboration features
- Updated infrastructure and presentation layers