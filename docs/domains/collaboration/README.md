# Collaboration Domain

## Overview
The collaboration system enables business matching and partnership management between users and business owners on the platform. The system supports two distinct types of collaboration requests:

- **Applications** (`Collaboration`): Guest users applying to collaborate on business owner's products
- **Proposals** (`CollaborationProposal`): Business partners/owners proposing collaborations to other business owners

## Domain Models
- **Collaboration**: `Collaboration.java` - Core collaboration entity for guest-to-business applications
- **CollaborationProposal**: `CollaborationProposal.java` - Business-to-business partnership proposals
- **CollaborationProduct**: `CollaborationProduct.java` - Product-specific collaboration features

## Repositories
Following the consistent three-layer pattern:
- **Domain Interfaces**: `CollaborationRepository`, `CollaborationProposalRepository`, `CollaborationProductRepository`
- **JPA Interfaces**: `CollaborationJpaRepository`, `CollaborationProposalJpaRepository`, `CollaborationProductJpaRepository`
- **Implementations**: `CollaborationRepositoryImpl`, `CollaborationProposalRepositoryImpl`, `CollaborationProductRepositoryImpl`

## Controllers
- `CollaborationController.java` - Main collaboration management and application processing
- `CollaborationProposalController.java` - Business proposal submission and management
- `CollaborationProductController.java` - Product-specific collaboration features
- `CollaboController.java` - Collaboration UI and templates
- `MyPageController.java` - User dashboard with received collaboration management

## Key Features

### Dual Collaboration System

#### Guest Applications (`Collaboration`)
- **Guest-to-Business**: Regular users apply to collaborate on business owner's products
- **Product-Based**: Applications tied to specific products
- **Simple Workflow**: User applies → Business owner accepts/rejects
- **Fields**: `product`, `applicant`, `applicantProduct`, `message`, `status`

#### Business Proposals (`CollaborationProposal`)
- **Business-to-Business**: Business owners propose partnerships to other business owners
- **Partnership-Based**: General business collaboration proposals
- **Enhanced Workflow**: Proposer submits → Target business owner accepts/rejects
- **Fields**: `title`, `description`, `category`, `proposer`, `targetBusinessOwner`, `revenueSharePreference`, `status`

### Management Interface
- **Unified Dashboard**: `/mypage/received` shows both applications and proposals
- **Separate Sections**: Pending, accepted, rejected, and all items
- **Consistent Statistics**: Counts include both collaboration types
- **Action Management**: Accept/reject functionality for both types

## Collaboration Workflows

### Guest Application Workflow
1. **Discovery**: Guest user browses products on the platform
2. **Application**: User submits collaboration application via `/collaborations/apply`
3. **Review**: Business owner views application in `/mypage/received`
4. **Decision**: Business owner accepts/rejects via form-based actions
5. **Execution**: If accepted, collaboration appears in ongoing section

### Business Proposal Workflow
1. **Proposal Creation**: Business owner creates proposal via enhanced form
2. **Targeting**: Proposal submitted to specific target business owner
3. **Review**: Target business owner reviews in `/mypage/received`
4. **Decision**: Target accepts/rejects proposal with notification system
5. **Partnership**: If accepted, collaboration becomes active partnership

## Integration Points
- **User Domain**: Role-based collaboration access
- **Product Domain**: Product-specific collaboration features
- **Chat Domain**: Communication during active collaborations
- **Notification Domain**: Updates and alerts for collaboration events

## Templates and UI
- `collaborations/apply.html` - Unified collaboration application form (supports both user types)
- `mypage/received.html` - Unified received collaboration management dashboard
- `mypage/applications.html` - User's submitted applications tracking
- `mypage/ongoing.html` - Active collaboration management
- `proposals/` - Business proposal-specific templates (browse, suggest, dashboard)

## API Endpoints

### Application Management
- `POST /collaborations/apply` - Submit collaboration application
- `POST /collaborations/{id}/accept` - Accept collaboration application (form-based)
- `POST /collaborations/{id}/reject` - Reject collaboration application (form-based)
- `GET /collaborations/apply` - Display application form with role-based logic

### Proposal Management
- `POST /proposals/suggest` - Submit business collaboration proposal
- `POST /proposals/{id}/accept/form` - Accept business proposal (form-based)
- `POST /proposals/{id}/reject/form` - Reject business proposal (form-based)
- `POST /proposals/{id}/accept` - Accept proposal (API response)
- `POST /proposals/{id}/reject` - Reject proposal (API response)

### Dashboard Endpoints
- `GET /mypage/received` - View received applications and proposals
- `GET /mypage/applications` - View submitted applications
- `GET /mypage/ongoing` - View active collaborations

## Business Logic
- **AI Integration**: Profit-sharing recommendation system
- **Status Management**: Collaboration and proposal state tracking
- **Access Control**: Role-based collaboration permissions
- **Matching Algorithm**: Business-user matching based on requirements

## Recent Development
- **Dual System Integration**: Unified display of both applications and proposals in `/mypage/received`
- **Enhanced Management Interface**: Comprehensive dashboard with statistics, filtering, and action management
- **Form-Based Actions**: Added form-based accept/reject endpoints for seamless page redirects
- **Consistent Statistics**: Synchronized counts between main dashboard and received page
- **Improved UX**: Enhanced error handling, success messaging, and empty state management
- **Role-Based Authorization**: Proper access control for both collaboration types
- **Template Optimization**: Responsive card layouts with distinct styling for applications vs proposals