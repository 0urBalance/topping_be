# CLAUDE.md

This file provides comprehensive guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
Topping (토핑) is a collaboration matching platform backend built with Spring Boot. The project uses a clean architecture approach with domain-driven design principles, enabling businesses to find collaboration partners and manage collaborative projects.

## Quick Start

### Environment Setup
- **JAVA_HOME**: Set to `/mnt/d/projects/topping/jdk-17.0.12+7` (JDK 17 included in project)
- **Build Command**: `export JAVA_HOME=/mnt/d/projects/topping/jdk-17.0.12+7 && ./gradlew build`

### Essential Commands
- `export JAVA_HOME=/mnt/d/projects/topping/jdk-17.0.12+7 && ./gradlew build` - Build the project
- `export JAVA_HOME=/mnt/d/projects/topping/jdk-17.0.12+7 && ./gradlew bootRun` - Run the application
- `export JAVA_HOME=/mnt/d/projects/topping/jdk-17.0.12+7 && ./gradlew test` - Run all tests

### Database
- **Production**: PostgreSQL (env vars: `DB_URL`, `DB_USER`, `DB_PASSWORD`)
- **Testing**: H2 in-memory database
- **Configuration**: Uses spring-dotenv, create `.env` file in project root
- **Connection Pool**: HikariCP optimized (5-30 connections, leak detection enabled)

## Architecture

### Clean Architecture Layers
- **`domain/`** - Core business logic and entities
- **`infrastructure/`** - Technical implementation details
- **`presentation/`** - REST controllers and DTOs
- **`application/`** - Application services and DTOs

### Tech Stack
- Spring Boot 3.5.3 with Java 17
- Spring Data JPA + PostgreSQL
- Spring Security (session-based authentication)
- Lombok + Thymeleaf
- WebSocket for real-time chat
- Spring Data repositories with three-layer pattern
- Multi-image upload system with image processing
- Async task execution with custom thread pools

## 📁 Domain Reference

The platform is organized into distinct business domains. Each domain has comprehensive documentation covering models, APIs, business rules, and integration points:

### Core Domains
- **[👤 User Domain](./docs/domains/user/README.md)** - User accounts, profiles, role-based access control, and phone number validation
  - **[Claude Guidance](./docs/domains/user/CLAUDE.md)** - User authentication patterns, role management, phone validation, and integration
- **[🏪 Store Domain](./docs/domains/store/README.md)** - Business store registration and management  
  - **[Claude Guidance](./docs/domains/store/CLAUDE.md)** - Three-phase registration, multi-image management, authorization patterns
- **[📦 Product Domain](./docs/domains/product/README.md)** - Product listings, editing capabilities, and collaboration features
  - **[Claude Guidance](./docs/domains/product/CLAUDE.md)** - Product routes, editing forms, field references, template patterns, collaboration integration
- **[🤝 Collaboration Domain](./docs/domains/collaboration/README.md)** - Business matching, partnership management, and proposal modifications
  - **[Claude Guidance](./docs/domains/collaboration/CLAUDE.md)** - Enhanced dual entity architecture, field mappings, chat integration, proposal flows and editing

### Supporting Domains  
- **[🔐 Authentication Domain](./docs/domains/auth/README.md)** - Session-based authentication with Spring Security and Kakao social login
  - **[Claude Guidance](./docs/domains/auth/CLAUDE.md)** - Session management, Kakao OAuth, route protection, authorization patterns
- **[💬 Chat Domain](./docs/domains/chat/README.md)** - Real-time communication and proposal management for collaborations
  - **[Claude Guidance](./docs/domains/chat/CLAUDE.md)** - WebSocket integration, message bubbles, unread tracking, automatic room creation, proposal modifications
- **[🔔 Notification Domain](./docs/domains/notification/README.md)** - Event-driven notifications
  - **[Claude Guidance](./docs/domains/notification/CLAUDE.md)** - Event-driven notifications, UI integration, polling patterns
- **[🎧 Support Domain](./docs/domains/support/README.md)** - Customer support system with FAQ and inquiry management  
  - **[Claude Guidance](./docs/domains/support/CLAUDE.md)** - FAQ management, inquiry systems, admin interfaces, public access patterns
- **[📋 Policy Domain](./docs/domains/policy/README.md)** - Privacy policy and terms of service with modal integration
  - **[Claude Guidance](./docs/domains/policy/CLAUDE.md)** - Modal integration, agreement validation, footer integration patterns

## Development Standards

### Repository Pattern
All repositories follow a consistent three-layer pattern:
- **Domain Interface**: `{Domain}Repository` (business logic)
- **JPA Interface**: `{Domain}JpaRepository` (extends JpaRepository)
- **Implementation**: `{Domain}RepositoryImpl` (implements domain interface)

### API Response Pattern
- **Standard Wrapper**: `ApiResponseData.success(data)` for success responses
- **Error Handling**: `ApiResponseData.failure(code, message)` for failures
- **Usage**: Always call `ApiResponseData.success(data)`, not `ApiResponseData.success(Code.SUCCESS, data)`

### Testing
- **Test Profile**: Use `@ActiveProfiles("test")` annotation
- **Database**: H2 in-memory database for testing
- **Configuration**: See `src/test/resources/application-test.properties`

## 🎨 UI/UX Framework

**⚠️ CRITICAL: ALL new templates must use the [Topping CSS Framework](./docs/technical/css-framework.md)**

### Quick Reference
- **Main Pages**: Use `base.css` + framework components
- **Auth Pages**: Use `auth.css` + framework components
- **JavaScript**: Use `common.js` or `auth-common.js` (no external CDNs)
- **Fragments**: Always use navbar, footer, and modals fragments
- **❌ FORBIDDEN**: Bootstrap CDN, inline styles, external CSS frameworks

### Mobile Navigation System
- **Responsive Design**: Sidebar navigation with hamburger menu for mobile devices
- **JavaScript Integration**: Dual event handling (inline + event listeners) for maximum compatibility
- **CSS Framework**: Proper z-index layering and mobile breakpoints at 768px
- **Accessibility**: Keyboard navigation, focus management, and screen reader support
- **Touch Support**: Swipe gestures for mobile sidebar open/close functionality

## Key Technical References

### Specialized Documentation
- **[🎨 CSS Framework & UI System](./docs/technical/css-framework.md)** - Complete UI framework guide
- **[🖼️ Multi-Image Upload System](./docs/technical/image-upload.md)** - File upload infrastructure
- **[🤝 Collaboration Forms](./docs/technical/collaboration-forms.md)** - Dynamic form system
- **[⚡ Frontend Optimization](./docs/technical/frontend-optimization.md)** - Performance improvements
- **[⚙️ Database & Performance](./docs/technical/database-performance.md)** - Connection pool & async config

### Troubleshooting & Workflow
- **[🔧 Common Issues](./docs/troubleshooting/common-issues.md)** - Troubleshooting guide
- **[📋 Development Workflow](./docs/development-workflow.md)** - Best practices and standards

## Build Requirements
- JAVA_HOME must be set to `/mnt/d/projects/topping/jdk-17.0.12+7` for all Gradle commands
- All build issues resolved and tests passing
- Session-based authentication (no JWT dependencies)

## Architecture Principles
- Clean architecture with domain-driven design
- Consistent repository pattern implementation across all domains
- Role-based authorization with Spring Security
- UUID primary keys for all entities
- Session authentication with JSESSIONID cookies

## ⚠️ Common Pitfalls

**IMPORTANT**: For domain-specific patterns and pitfalls, see individual domain Claude guidance files:
- **[User Domain Pitfalls](./docs/domains/user/CLAUDE.md#common-pitfalls)**
- **[Store Domain Pitfalls](./docs/domains/store/CLAUDE.md#common-pitfalls)**  
- **[Product Domain Pitfalls](./docs/domains/product/CLAUDE.md#common-pitfalls)**
- **[Collaboration Domain Pitfalls](./docs/domains/collaboration/CLAUDE.md#common-pitfalls)**
- **[Auth Domain Pitfalls](./docs/domains/auth/CLAUDE.md#common-pitfalls)**
- **[Chat Domain Pitfalls](./docs/domains/chat/CLAUDE.md#common-pitfalls)**

### Universal Platform Pitfalls
- **Session Auth**: Use Principal or UserDetailsImpl, not JWT tokens
- **Entity Creation**: Never manually set UUID for @GeneratedValue entities
- **Fragment Syntax**: Use `th:replace="~{fragments/navbar :: navbar}"` (Thymeleaf 3 syntax)
- **JPA JOIN FETCH**: Avoid multiple JOIN FETCH in single query - causes Cartesian Product and data duplication
- **Null-Safety in Templates**: Use proper null checks: `${object != null and !#strings.isEmpty(object.field)}`
- **Template Parsing**: Avoid complex nested `th:if` and `th:each` within JavaScript inline sections - use JSON injection instead
- **Entity Persistence**: Ensure POST endpoints create and save entities, not just validate form data
- **Entity Method Names**: All entities use `getUuid()` method, NOT `getUserId()`, `getRoomId()`, etc. (Lombok generates getters from field names)
- **Mobile Navbar**: Use both inline `onclick` and JavaScript event listeners for maximum browser compatibility
- **API Content-Type**: Check response content-type before parsing JSON to avoid "Unexpected token '<'" errors

## Domain-Specific Implementation Details

**For detailed implementation patterns, see individual domain Claude guidance files:**

### Authentication & Authorization
- **[Auth Domain Guide](./docs/domains/auth/CLAUDE.md)** - Session management, Kakao OAuth, route protection
- **[User Domain Guide](./docs/domains/user/CLAUDE.md)** - Role-based access control, user management patterns

### Business Logic Domains  
- **[Store Domain Guide](./docs/domains/store/CLAUDE.md)** - Three-phase registration, multi-image management
- **[Product Domain Guide](./docs/domains/product/CLAUDE.md)** - Product routes, editing forms, field references, collaboration integration
- **[Collaboration Domain Guide](./docs/domains/collaboration/CLAUDE.md)** - Enhanced dual entity architecture, proposal flows, and modification capabilities

### Communication & Support
- **[Chat Domain Guide](./docs/domains/chat/CLAUDE.md)** - WebSocket integration, message bubbles, automatic room creation, proposal modifications
- **[Notification Domain Guide](./docs/domains/notification/CLAUDE.md)** - Event-driven notifications, UI integration
- **[Support Domain Guide](./docs/domains/support/CLAUDE.md)** - FAQ management, inquiry systems
- **[Policy Domain Guide](./docs/domains/policy/CLAUDE.md)** - Modal integration, agreement validation

## Recent Status & Improvements
- ✅ **Authentication System**: Migrated from JWT to session-based, fully stable
- ✅ **Session Persistence**: Fixed across all protected routes, login state maintained
- ✅ **Store Management**: Role-based access control with BUSINESS_OWNER/ADMIN roles
- ✅ **MyPage Features**: Comprehensive user dashboard with applications, collaborations, ongoing projects
- ✅ **Template System**: All template errors resolved, consistent Thymeleaf patterns
- ✅ **Routing**: All endpoints properly mapped, no broken links
- ✅ **Build System**: Stable and reliable, no compilation issues
- ✅ **Customer Support**: FAQ management, inquiry submission, admin response system
- ✅ **Policy System**: Privacy policy and terms of service with modal integration
- ✅ **Signup Process**: Enhanced with required agreement validation and modal policy access
- ✅ **Login UI**: Modern, responsive design matching brand guidelines
- ✅ **Entity Management**: Fixed optimistic locking issues, proper UUID generation
- ✅ **CSRF Protection**: Cookie-based token repository, session-safe implementation
- ✅ **Modal System**: Consistent across all pages, proper event handling
- ✅ **Footer Integration**: Modal experience integrated across all templates
- ✅ **Multi-Image Upload System**: Complete file upload infrastructure with validation, processing, and storage
- ✅ **Store Detail Page**: 3-column responsive layout with enhanced gallery and collaboration features
- ✅ **Explore Page**: Three-section discovery layout (stores, menus, collaboration products)
- ✅ **Thread Pool Configuration**: Custom async executor with DiscardOldestPolicy for optimal performance
- ✅ **Image Management**: Entity relationships, repositories, and service layer for multiple images per store/menu
- ✅ **Frontend Optimization**: Complete store detail page refactoring with component system, performance improvements, and visual refinements
- ✅ **Environment-Specific File Upload**: Externalized upload path configuration with profile-based and environment variable support
- ✅ **JPA Query Optimization**: Fixed product duplication issues caused by Cartesian Product in multiple JOIN FETCH operations
- ✅ **Home Page Products Enhancement**: Horizontal scrolling layout with drag-and-scroll functionality, limited to 5 products max
- ✅ **Store Detail Two-Column Layout**: Implemented sidebar with SNS & collaboration information, proper large screen constraints
- ✅ **Thymeleaf Expression Safety**: Fixed null-safety issues and field binding errors across all templates
- ✅ **Three-Phase Store Registration**: Resolved multipart parsing errors with architectural solution separating store creation from image upload
- ✅ **Multipart Debug Resolution**: Comprehensive debugging and testing infrastructure for servlet-level multipart handling
- ✅ **Collaboration Form Enhancement**: Refactored `/collaborations/apply` with auto-selection functionality, enhanced calendar UI, and critical bug fixes for entity creation and MyPage integration
- ✅ **Dual Collaboration System**: Unified `/mypage/received` dashboard displaying both guest applications and business proposals with consistent statistics and management actions
- ✅ **Chat System Integration**: Real-time chat with automatic room creation for accepted collaborations, modern UI with dual collaboration support
- ✅ **Chat Service Architecture**: Clean service layer with automatic chat room management for both `Collaboration` and `CollaborationProposal` entities  
- ✅ **Modern Chat Interface**: Complete UI redesign with sidebar navigation, WebSocket integration, search functionality, and responsive design
- ✅ **Chat API Endpoints**: Complete JSON API implementation with room data, user session, and message sending endpoints
- ✅ **WebSocket Integration**: Modern SockJS/STOMP implementation with proper error handling and reconnection logic
- ✅ **Mobile Navbar Fixes**: Resolved hamburger menu functionality with proper event handling and CSS positioning
- ✅ **Chat UI Enhancement**: Modern message bubble design with proper alignment, color scheme (#6B3410 for own messages), and accessibility features
- ✅ **Unread Message System**: Red circular badges with real-time count tracking, automatic read status management, and ARIA labels for accessibility
- ✅ **Message Read Tracking**: Database-level read/unread status with `ChatMessage.readAt` and `ChatMessage.isRead` fields
- ✅ **Chat UX Improvements**: Auto-hide badges on room selection, proper message alignment (right for own, left for others), and enhanced visual feedback
- ✅ **Chat System Refactoring**: Unified single-page interface, removed legacy Bootstrap template, modernized with CSS framework compliance
- ✅ **Real-time Message Broadcasting**: Fixed immediate message display with `SimpMessagingTemplate` WebSocket broadcasting after database save
- ✅ **Message Bubble Alignment**: Fixed "mine" vs "their" message styling with proper UUID comparison and session data extraction
- ✅ **Timestamp Display Fix**: Korean-formatted timestamps (오전/오후 HH:mm) with robust date parsing and error handling
- ✅ **Session Integration Enhancement**: Proper handling of `ApiResponseData<SessionUserInfo>` wrapper for accurate user identification
- ✅ **SpringEL Evaluation Error Resolution**: Comprehensive fix for all non-existent entity field references across templates (37 total fixes)
  - Fixed `collaboration.product` → conditional `partnerProduct`/`initiatorProduct` logic (12 occurrences)
  - Fixed `applicantProduct` → `initiatorProduct` field references (12 occurrences)  
  - Fixed `application.product` → conditional product field logic (6 occurrences)
  - Fixed `receivedApp.product` → conditional product field logic (6 occurrences)
  - Fixed `.message` → `.description` field references (3 occurrences)
  - **Zero Runtime SpringEL Exceptions**: All "Property or field 'X' cannot be found" errors eliminated
- ✅ **Phone Number Duplicate Validation**: Enhanced user registration with phone number uniqueness checks and validation
- ✅ **Product Edit System**: Complete product modification functionality with form validation and data persistence
- ✅ **Home Page Advanced Filtering**: Implemented sophisticated filtering system for store and product discovery
- ✅ **Collaboration Entity Refactoring**: Major architectural improvements to collaboration system with enhanced data models
- ✅ **Chat-Proposal Integration**: Advanced integration allowing proposal modifications directly from chat interface
- ✅ **Social Login Enhancements**: Fixed Kakao OAuth redirect URLs and improved authentication flow reliability
- ✅ **Store Category Management**: Enhanced store category editing with improved data integrity and user experience
- ✅ **Profile Screen Implementation**: Comprehensive MyPage profile management with user information display and editing capabilities
- ✅ **MyPage Design Enhancements**: Improved visual design and user experience across all MyPage sections
- ✅ **Collaboration Product Display**: Enhanced store detail pages to show collaborative products instead of just collaborative stores
- ✅ **Repository Error Resolution**: Fixed logging errors in StoreRepositoryImpl.findAll() method with proper pageable handling
- ✅ **Error Template Implementation**: Added comprehensive 500 error template for better error handling
- ✅ **Test Infrastructure Improvements**: Updated test imports and configurations for better test reliability
- ✅ **Kakao Social Login Stabilization**: Complete implementation with database storage, session management, and IP configuration handling

## Documentation Navigation

### 📚 Main Documentation Hub
- **[Documentation Index](./docs/README.md)** - Complete documentation navigation and standards

### 🔧 Technical Guides
- **[CSS Framework & UI System](./docs/technical/css-framework.md)** - Complete UI framework documentation
- **[Multi-Image Upload System](./docs/technical/image-upload.md)** - File upload infrastructure
- **[Collaboration Forms](./docs/technical/collaboration-forms.md)** - Dynamic form system
- **[Collaboration Received Page](./docs/technical/COLLABORATION_RECEIVED_PAGE.md)** - Dual collaboration system architecture
- **[Chat System Integration](./docs/technical/chat-system-integration.md)** - Real-time chat with automatic room creation
- **[Chat UI Enhancements](./docs/technical/chat-ui-enhancements.md)** - Message bubble design & unread message system
- **[Chat Real-time Messaging](./docs/technical/chat-real-time-messaging.md)** - Unified interface, immediate message display, and WebSocket broadcasting
- **[Frontend Optimization](./docs/technical/frontend-optimization.md)** - Performance improvements
- **[Database & Performance](./docs/technical/database-performance.md)** - Connection pool & async config

### 🔧 Troubleshooting Guides
- **[Common Issues](./docs/troubleshooting/common-issues.md)** - General troubleshooting
- **[Session Persistence](./docs/SESSION_PERSISTENCE_TROUBLESHOOTING.md)** - Authentication issues and resolutions
- **[Three-Phase Registration](./docs/troubleshooting/THREE_PHASE_REGISTRATION_SOLUTION.md)** - Store registration architectural solution
- **[Multipart Debug Resolution](./docs/troubleshooting/multipart/COMPLETE_MULTIPART_DEBUG_RESOLUTION.md)** - Comprehensive multipart debugging journey

### 🏗️ Quick Domain Access
- [User Management](./docs/domains/user/README.md) - User accounts and roles | [Claude Guide](./docs/domains/user/CLAUDE.md)
- [Store Management](./docs/domains/store/README.md) - Business store operations | [Claude Guide](./docs/domains/store/CLAUDE.md)
- [Product Management](./docs/domains/product/README.md) - Product listings and features | [Claude Guide](./docs/domains/product/CLAUDE.md)
- [Collaboration Platform](./docs/domains/collaboration/README.md) - Business matching and proposals | [Claude Guide](./docs/domains/collaboration/CLAUDE.md)
- [Authentication System](./docs/domains/auth/README.md) - Login/logout, session management | [Claude Guide](./docs/domains/auth/CLAUDE.md)
- [Chat System](./docs/domains/chat/README.md) - Real-time messaging | [Claude Guide](./docs/domains/chat/CLAUDE.md)
- [Notification System](./docs/domains/notification/README.md) - Event-driven alerts | [Claude Guide](./docs/domains/notification/CLAUDE.md)
- [Customer Support](./docs/domains/support/README.md) - FAQ and inquiry management | [Claude Guide](./docs/domains/support/CLAUDE.md)
- [Policy Management](./docs/domains/policy/README.md) - Privacy policy and terms of service | [Claude Guide](./docs/domains/policy/CLAUDE.md)

## 🎯 Project Status: Production Ready

The Topping platform is now in a stable, production-ready state with:

- **Complete Authentication System**: Session-based with Kakao social login integration
- **Full Domain Implementation**: All core business domains functional
- **Modern UI/UX**: Responsive, accessible, brand-consistent design
- **Comprehensive Testing**: Unit and integration tests with proper test profiles
- **Documentation**: Complete domain documentation and troubleshooting guides
- **Security**: Role-based access control, secure session management, CSRF disabled for simplified forms
- **Social Login**: Kakao OAuth integration with automatic user registration
- **Performance**: Optimized HikariCP connection pool, efficient queries, connection leak detection
- **Database Reliability**: Fixed connection pool exhaustion issues, proper transaction boundaries

The platform is ready for deployment and further feature development.