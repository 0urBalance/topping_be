# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

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

### Recent Status
- ✅ Authentication migrated from JWT to session-based
- ✅ Session persistence fixed across all protected routes
- ✅ Store management integration with role-based access control
- ✅ MyPage functionality enhanced with comprehensive features
- ✅ Template errors resolved (Product field name fixes)
- ✅ Routing issues resolved (Product creation endpoints)
- ✅ Build system stable and reliable
- ✅ Ready for feature development

### Session Authentication Details
- **Session Management**: Configured with `SessionCreationPolicy.IF_REQUIRED`
- **Session Persistence**: JSESSIONID cookie maintains authentication across requests
- **Route Protection**: All feature routes (`/collabo/**`, `/mypage/**`, `/products/**`, `/stores/**`) require authentication
- **Template Integration**: Thymeleaf security integration with `sec:authorize="isAuthenticated()"`
- **API Security**: Session-based endpoints (`/api/session/*`) for login/logout/status
- **Role-based Access**: Store management requires `ROLE_BUSINESS_OWNER` or `ROLE_ADMIN`

## Critical Implementation Notes

### ⚠️ Common Pitfalls
- **Product Field**: Use `product.title`, NOT `product.name` in templates
- **Product Routes**: Use `/products/create`, NOT `/products/register`
- **Store Access**: Verify user role before store operations
- **Session Auth**: Use Principal or UserDetailsImpl, not JWT tokens

### Template Best Practices
- Always use `th:href="@{/path}"` for internal links
- Check user roles with `sec:authorize="hasRole('ROLE_NAME')"`
- Handle null checks with `th:if="${object != null}"`
- Use consistent variable naming (avoid 'application' - reserved word)

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