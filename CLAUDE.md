# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
Topping (토핑) is a collaboration matching platform backend built with Spring Boot. The project uses a clean architecture approach with domain-driven design principles.

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

## Domain Areas

The platform is organized into distinct business domains, each with detailed documentation:

### 🔐 Authentication
Session-based authentication with Spring Security
- **Details**: [docs/domains/auth/README.md](./docs/domains/auth/README.md)
- **Endpoints**: `/login`, `/logout`, `/api/session/login`, `/api/session/logout`

### 👤 User Management
User accounts, profiles, and role-based access control
- **Details**: [docs/domains/user/README.md](./docs/domains/user/README.md)
- **Roles**: `ROLE_USER`, `ROLE_BUSINESS_OWNER`

### 🤝 Collaboration
Business matching and partnership management system
- **Details**: [docs/domains/collaboration/README.md](./docs/domains/collaboration/README.md)
- **Features**: Proposals, matching, AI-based profit-sharing

### 💬 Chat
Real-time communication for active collaborations
- **Details**: [docs/domains/chat/README.md](./docs/domains/chat/README.md)
- **Technology**: WebSocket-based messaging

### 📦 Products
Product listings and collaboration-based product features
- **Details**: [docs/domains/product/README.md](./docs/domains/product/README.md)
- **Integration**: Product-specific collaborations

### 🔔 Notifications
Event-driven notifications for platform activities
- **Details**: [docs/domains/notification/README.md](./docs/domains/notification/README.md)
- **Events**: Collaboration updates, chat alerts, system notifications

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
- ✅ Build system stable and reliable
- ✅ Test infrastructure properly configured
- ✅ All repositories follow consistent patterns
- ✅ Ready for feature development

## Domain Navigation

For detailed information about specific business areas, refer to the domain-specific documentation:

- [Authentication System](./docs/domains/auth/README.md) - Login/logout, session management
- [User Management](./docs/domains/user/README.md) - User accounts and roles
- [Collaboration Platform](./docs/domains/collaboration/README.md) - Business matching and proposals
- [Chat System](./docs/domains/chat/README.md) - Real-time messaging
- [Product Management](./docs/domains/product/README.md) - Product listings and features
- [Notification System](./docs/domains/notification/README.md) - Event-driven alerts

Each domain README contains detailed information about models, repositories, controllers, workflows, and integration points specific to that business area.