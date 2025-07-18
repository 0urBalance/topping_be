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
- **Connection Pool**: Minimized HikariCP settings for reduced resource usage (6-10 connections for dev, 2-5 for testing)

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

## 📁 Domain Reference

The platform is organized into distinct business domains. Each domain has comprehensive documentation covering models, APIs, business rules, and integration points:

### Core Domains
- **[👤 User Domain](./docs/domains/user/README.md)** - User accounts, profiles, and role-based access control
- **[🏪 Store Domain](./docs/domains/store/README.md)** - Business store registration and management
- **[📦 Product Domain](./docs/domains/product/README.md)** - Product listings and collaboration features
- **[🤝 Collaboration Domain](./docs/domains/collaboration/README.md)** - Business matching and partnership management

### Supporting Domains
- **[🔐 Authentication Domain](./docs/domains/auth/README.md)** - Session-based authentication with Spring Security
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

## 🎨 UI/UX Design Guidelines

### Brand Identity
- **Primary Color**: `#ff6b35` (Orange) - Used for buttons, links, focus states
- **Secondary Colors**: 
  - Success: `#059669` (Green)
  - Error: `#dc2626` (Red)
  - Warning: `#856404` (Amber)
  - Info: `#2563eb` (Blue)
- **Typography**: Modern system font stack (`-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif`)
- **Background**: Light gray (`#f8f9fa`) for better contrast

### Component Design Patterns

#### Buttons
- **Primary**: `btn-primary` - Orange background, white text, full width
- **Secondary**: `btn-secondary` - White background, orange border and text
- **Social**: `social-btn` - Platform-specific colors (Kakao yellow, Naver green, Google white)

#### Forms
- **Input Fields**: Clean border, rounded corners, focus states with orange accent
- **Labels**: Bold, consistent spacing, dark gray color
- **Error States**: Red border and background tint with descriptive messages
- **Success States**: Green border and background tint

#### Modals
- **Structure**: Header with title and close button, scrollable body
- **Backdrop**: Semi-transparent dark overlay
- **Responsive**: Adapts to screen sizes
- **Accessibility**: ESC key closes, outside click closes

### Layout Principles
- **Centered Design**: Main content containers centered with max-width
- **Responsive**: Mobile-first approach with breakpoints
- **Spacing**: Consistent padding and margins using 8px grid system
- **Typography Scale**: Hierarchical font sizes for readability

## Key Reminders

### Build Requirements
- JAVA_HOME must be set to `/mnt/d/projects/topping/jdk-17.0.12+7` for all Gradle commands
- All build issues resolved and tests passing
- Session-based authentication (no JWT dependencies)

### Architecture Principles
- Clean architecture with domain-driven design
- Consistent repository pattern implementation across all domains
- Role-based authorization with Spring Security
- UUID primary keys for all entities
- Session authentication with JSESSIONID cookies

### Recent Status & Improvements
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

### Session Authentication Details
- **Session Management**: Configured with `SessionCreationPolicy.IF_REQUIRED`
- **Session Persistence**: JSESSIONID cookie maintains authentication across requests
- **Route Protection**: All feature routes (`/collabo/**`, `/mypage/**`, `/products/**`, `/stores/**`, `/support/inquiry*`, `/support/my-inquiries`) require authentication
- **Template Integration**: Thymeleaf security integration with `sec:authorize="isAuthenticated()"`
- **API Security**: Session-based endpoints (`/api/session/*`) for login/logout/status
- **Role-based Access**: Store management requires `ROLE_BUSINESS_OWNER` or `ROLE_ADMIN`
- **CSRF Protection**: Cookie-based token repository prevents session creation issues

## Critical Implementation Notes

### ⚠️ Common Pitfalls
- **Product Field**: Use `product.title`, NOT `product.name` in templates
- **Product Routes**: Use `/products/create`, NOT `/products/register`
- **Store Access**: Verify user role before store operations
- **Session Auth**: Use Principal or UserDetailsImpl, not JWT tokens
- **Entity Creation**: Never manually set UUID for @GeneratedValue entities
- **Fragment Syntax**: Use `th:replace="~{fragments/navbar :: navbar}"` (Thymeleaf 3 syntax)

### Template Best Practices
- Always use `th:href="@{/path}"` for internal links
- Check user roles with `sec:authorize="hasRole('ROLE_NAME')"`
- Handle null checks with `th:if="${object != null}"`
- Use consistent variable naming (avoid 'application' - reserved word)
- Include CSRF token in all forms: `<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />`

### Modal System Guidelines
- **Policy Modals**: Use dynamic content loading with caching (`/policy/privacy-modal`, `/policy/terms-modal`)
- **Modal Structure**: Header with title and close button, body with scrollable content
- **JavaScript Integration**: Handle modal open/close, dynamic content loading, outside-click closing
- **Signup Integration**: Required agreement checkbox validation before form submission
- **Footer Integration**: Consistent modal experience across all pages with footer links
- **Accessibility**: ESC key support, focus management, proper ARIA attributes

### Customer Support System
- **Public Access**: FAQ viewing (`/support/cs`) accessible to all users
- **Authenticated Access**: Inquiry submission and management require login
- **Repository Pattern**: Follows three-layer pattern (`SupportInquiryRepository`, `FAQRepository`)
- **Entity Design**: Uses enums for categories and status tracking
- **Template Structure**: Modern responsive design with search and pagination

## 🔧 Troubleshooting & Common Issues

### Login Page Issues
- **Session Creation**: Fixed with cookie-based CSRF tokens
- **Fragment Rendering**: Updated to Thymeleaf 3 syntax
- **Template Errors**: All deprecated syntax updated
- **CSRF Issues**: Properly configured with `CookieCsrfTokenRepository.withHttpOnlyFalse()`

### Entity Management
- **Optimistic Locking**: Fixed by removing manual UUID setting in SignupController
- **UUID Generation**: Let Hibernate handle @GeneratedValue @UuidGenerator entities
- **Repository Layer**: Consistent three-layer pattern across all domains

### Build & Runtime
- **JAVA_HOME**: Must be set correctly for all Gradle operations
- **Dependencies**: All conflicts resolved, no version mismatches
- **Database**: Connection pool optimized for development and testing
- **Session Config**: Persistent sessions with proper timeout configuration

## 📋 Development Workflow

### Adding New Features
1. **Domain Design**: Follow clean architecture principles
2. **Repository Pattern**: Implement three-layer pattern
3. **Controller Layer**: Use proper response wrappers
4. **Template Integration**: Follow UI/UX guidelines
5. **Security**: Implement proper authentication/authorization
6. **Testing**: Write comprehensive tests with test profile

### UI/UX Development
1. **Design Review**: Check existing patterns and brand guidelines
2. **Responsive Design**: Mobile-first approach
3. **Accessibility**: Proper labels, keyboard navigation, ARIA attributes
4. **Performance**: Optimize CSS/JS, minimize requests
5. **Browser Testing**: Cross-browser compatibility

### Code Quality
- **Clean Code**: Follow existing patterns and conventions
- **Error Handling**: Proper exception handling with user-friendly messages
- **Security**: Always validate inputs, protect against common vulnerabilities
- **Documentation**: Update relevant documentation when adding features

## Documentation Navigation

### 📚 Main Documentation Hub
- **[Documentation Index](./docs/README.md)** - Complete documentation navigation and standards

### 🔧 Troubleshooting Guides
- **[Session Persistence Troubleshooting](./docs/SESSION_PERSISTENCE_TROUBLESHOOTING.md)** - Authentication issues and resolutions

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

- **Complete Authentication System**: Session-based with proper security
- **Full Domain Implementation**: All core business domains functional
- **Modern UI/UX**: Responsive, accessible, brand-consistent design
- **Comprehensive Testing**: Unit and integration tests with proper test profiles
- **Documentation**: Complete domain documentation and troubleshooting guides
- **Security**: CSRF protection, role-based access control, secure session management
- **Performance**: Optimized database connections, efficient queries, minimal resource usage

The platform is ready for deployment and further feature development.