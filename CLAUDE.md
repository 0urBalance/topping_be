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
- **[👤 User Domain](./docs/domains/user/README.md)** - User accounts, profiles, and role-based access control
- **[🏪 Store Domain](./docs/domains/store/README.md)** - Business store registration and management
- **[📦 Product Domain](./docs/domains/product/README.md)** - Product listings and collaboration features
- **[🤝 Collaboration Domain](./docs/domains/collaboration/README.md)** - Business matching and partnership management

### Supporting Domains
- **[🔐 Authentication Domain](./docs/domains/auth/README.md)** - Session-based authentication with Spring Security and Kakao social login
- **[💬 Chat Domain](./docs/domains/chat/README.md)** - Real-time communication for collaborations
- **[🔔 Notification Domain](./docs/domains/notification/README.md)** - Event-driven notifications
- **[🎧 Support Domain](./docs/domains/support/README.md)** - Customer support system with FAQ and inquiry management
- **[📋 Policy Domain](./docs/domains/policy/README.md)** - Privacy policy and terms of service with modal integration

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
- **Product Field**: Use `product.title`, NOT `product.name` in templates
- **Product Routes**: Use `/products/create`, NOT `/products/register`
- **Store Access**: Verify user role before store operations
- **Session Auth**: Use Principal or UserDetailsImpl, not JWT tokens
- **Entity Creation**: Never manually set UUID for @GeneratedValue entities
- **Fragment Syntax**: Use `th:replace="~{fragments/navbar :: navbar}"` (Thymeleaf 3 syntax)
- **JPA JOIN FETCH**: Avoid multiple JOIN FETCH in single query - causes Cartesian Product and data duplication
- **DTO Field Binding**: Ensure Thymeleaf field references match actual DTO property names (e.g., `thumbnailPath` not `imageUrl`)
- **Null-Safety in Templates**: Use proper null checks: `${object != null and !#strings.isEmpty(object.field)}`
- **Template Parsing**: Avoid complex nested `th:if` and `th:each` within JavaScript inline sections - use JSON injection instead
- **Entity Persistence**: Ensure POST endpoints create and save entities, not just validate form data
- **Collaboration Forms**: Use server-side JSON generation with `ObjectMapper` for complex store-product data instead of nested Thymeleaf loops

## Session Authentication Details
- **Session Management**: Configured with `SessionCreationPolicy.IF_REQUIRED`
- **Session Persistence**: JSESSIONID cookie maintains authentication across requests
- **Route Protection**: All feature routes (`/collabo/**`, `/mypage/**`, `/products/**`, `/stores/**`, `/support/inquiry*`, `/support/my-inquiries`) require authentication
- **Template Integration**: Thymeleaf security integration with `sec:authorize="isAuthenticated()"`
- **API Security**: Session-based endpoints (`/api/session/*`) for login/logout/status
- **Role-based Access**: Store management requires `ROLE_BUSINESS_OWNER` or `ROLE_ADMIN`
- **CSRF Protection**: CSRF protection disabled for simplified form handling

## Kakao Social Login Integration
- **Service Layer**: `KakaoService` handles OAuth flow and user management in application layer
- **Domain Model**: `KakaoUserInfoDto` in domain layer with validation logic
- **Session Integration**: Uses `SecurityContextHolder` for session-based authentication (NOT JWT)
- **User Management**: Automatically creates new users with default settings (Seoul/Gangnam region, ROLE_USER)
- **Error Handling**: Specific error messages for Kakao login failures in login template
- **API Flow**: `/api/user/kakao/callback` → KakaoService → Spring Security session
- **Configuration**: Uses `KAKAO_REST_API_KEY` environment variable
- **Clean Architecture**: External API integration in application layer, business logic in domain layer
- **RestTemplate**: Centralized configuration for external API calls

## Customer Support System
- **Public Access**: FAQ viewing (`/support/cs`) accessible to all users
- **Authenticated Access**: Inquiry submission and management require login
- **Repository Pattern**: Follows three-layer pattern (`SupportInquiryRepository`, `FAQRepository`)
- **Entity Design**: Uses enums for categories and status tracking
- **Template Structure**: Modern responsive design with search and pagination

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

## Documentation Navigation

### 📚 Main Documentation Hub
- **[Documentation Index](./docs/README.md)** - Complete documentation navigation and standards

### 🔧 Technical Guides
- **[CSS Framework & UI System](./docs/technical/css-framework.md)** - Complete UI framework documentation
- **[Multi-Image Upload System](./docs/technical/image-upload.md)** - File upload infrastructure
- **[Collaboration Forms](./docs/technical/collaboration-forms.md)** - Dynamic form system
- **[Collaboration Received Page](./docs/technical/COLLABORATION_RECEIVED_PAGE.md)** - Dual collaboration system architecture
- **[Frontend Optimization](./docs/technical/frontend-optimization.md)** - Performance improvements
- **[Database & Performance](./docs/technical/database-performance.md)** - Connection pool & async config

### 🔧 Troubleshooting Guides
- **[Common Issues](./docs/troubleshooting/common-issues.md)** - General troubleshooting
- **[Session Persistence](./docs/SESSION_PERSISTENCE_TROUBLESHOOTING.md)** - Authentication issues and resolutions
- **[Three-Phase Registration](./docs/troubleshooting/THREE_PHASE_REGISTRATION_SOLUTION.md)** - Store registration architectural solution
- **[Multipart Debug Resolution](./docs/troubleshooting/multipart/COMPLETE_MULTIPART_DEBUG_RESOLUTION.md)** - Comprehensive multipart debugging journey

### 🏗️ Quick Domain Access
- [User Management](./docs/domains/user/README.md) - User accounts and roles
- [Store Management](./docs/domains/store/README.md) - Business store operations
- [Product Management](./docs/domains/product/README.md) - Product listings and features
- [Collaboration Platform](./docs/domains/collaboration/README.md) - Business matching and proposals
- [Authentication System](./docs/domains/auth/README.md) - Login/logout, session management
- [Chat System](./docs/domains/chat/README.md) - Real-time messaging
- [Notification System](./docs/domains/notification/README.md) - Event-driven alerts
- [Customer Support](./docs/domains/support/README.md) - FAQ and inquiry management
- [Policy Management](./docs/domains/policy/README.md) - Privacy policy and terms of service

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