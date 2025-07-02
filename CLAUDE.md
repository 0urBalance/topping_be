# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
Topping (토핑) is a collaboration matching platform backend built with Spring Boot. The project uses a clean architecture approach with domain-driven design principles.

## Build & Development Commands

### Environment Setup
- **JAVA_HOME**: Set to `/mnt/d/projects/topping/jdk-17.0.12+7` (JDK 17 included in project)
- **Build Command**: `export JAVA_HOME=/mnt/d/projects/topping/jdk-17.0.12+7 && ./gradlew build`

### Build & Run
- `export JAVA_HOME=/mnt/d/projects/topping/jdk-17.0.12+7 && ./gradlew build` - Build the project
- `export JAVA_HOME=/mnt/d/projects/topping/jdk-17.0.12+7 && ./gradlew bootRun` - Run the Spring Boot application
- `export JAVA_HOME=/mnt/d/projects/topping/jdk-17.0.12+7 && ./gradlew test` - Run all tests
- `export JAVA_HOME=/mnt/d/projects/topping/jdk-17.0.12+7 && ./gradlew test --tests ClassName` - Run specific test class
- `export JAVA_HOME=/mnt/d/projects/topping/jdk-17.0.12+7 && ./gradlew test --tests ClassName.methodName` - Run specific test method

### Database
- PostgreSQL database configured via environment variables
- Required env vars: `DB_URL`, `DB_USER`, `DB_PASSWORD`
- Uses spring-dotenv for environment configuration
- Create `.env` file in project root with database credentials

## Architecture

### Package Structure
- `domain/` - Core business logic and entities
  - `model/` - JPA entities (User with UUID primary keys)
  - `repository/` - Repository interfaces
- `infrastructure/` - Technical implementation details
  - `persistence/` - JPA repository implementations
  - `security/` - Spring Security configuration (Role-based auth)
  - `response/` - Standardized API response wrappers
  - `exception/` - Global exception handling
- `presentation/` - REST controllers and DTOs
- `application/` - Application services and DTOs

### Key Components
- **ApiResponseData**: Standardized response wrapper with code/message/data structure
- **GlobalExceptionHandler**: Centralized exception handling with custom BaseException support
- **User Entity**: Uses UUID as primary key with Role-based authentication
- **Spring Security**: Session-based authentication with form login/logout and cookie management

### Dependencies
- Spring Boot 3.5.3 with Java 17
- Spring Data JPA + PostgreSQL
- Spring Security
- Lombok for boilerplate reduction
- Thymeleaf for server-side rendering

## Testing Configuration
- **H2 Database**: In-memory database for testing (see `src/test/resources/application-test.properties`)
- **Test Profile**: Use `@ActiveProfiles("test")` annotation for test classes
- **Test Dependencies**: H2 database and Spring Boot Test starters included

## Repository Pattern
- **Consistent Architecture**: All repositories follow the same pattern:
  - Domain repository interfaces in `domain/repository/`
  - JPA repository interfaces in `infrastructure/persistence/` (extends `JpaRepository`)
  - Repository implementations in `infrastructure/persistence/` (implements domain interface)
- **Example**: `UserRepository` (domain) → `UserJpaRepository` (JPA) → `UserRepositoryImpl` (implementation)

## API Response Pattern
- **ApiResponseData**: Standardized response wrapper
  - `success(T data)` - Success response with data
  - `success(T data, String message)` - Success response with custom message
  - `failure(Integer code, String message)` - Failure response
- **Usage**: Always call `ApiResponseData.success(data)` not `ApiResponseData.success(Code.SUCCESS, data)`

## Authentication System

### Session-Based Authentication
- **Authentication Method**: Spring Security session-based authentication (migrated from JWT)
- **Login Endpoints**: 
  - `/login` - Standard form login (handled by Spring Security)
  - `/api/session/login` - API login with JSON request/response
- **Logout Endpoints**:
  - `/logout` - Standard form logout (handled by Spring Security)
  - `/api/session/logout` - API logout with JSON response
- **Session Management**: Server-side session storage with JSESSIONID cookie
- **Security Features**: Session invalidation on logout, cookie deletion, role-based access control

### Authentication Endpoints
- **Form Login**: `POST /login` - Standard HTML form authentication
- **API Login**: `POST /api/session/login` - JSON-based login for frontend applications
- **Form Logout**: `POST /logout` - Standard logout with redirect
- **API Logout**: `POST /api/session/logout` - JSON-based logout
- **Status Check**: `POST /api/member/login-status` - Check current authentication status

## Recent Development History

### 🛠 Task Summary (Latest Session)
- **Authentication Migration**: Successfully migrated from JWT-based to session-based authentication
- **JWT Removal**: Removed all JWT dependencies, services, filters, and DTOs
- **Session Configuration**: Configured Spring Security for form login/logout with session management
- **API Updates**: Updated authentication controllers for session-based endpoints
- **Build Verification**: Confirmed successful compilation and test execution

### 🐞 Bug Fixes (Build Issues Resolved)
1. **ApiResponseData Method Signature Errors**
   - **Context**: Controllers calling `ApiResponseData.success(Code.SUCCESS, data)` 
   - **Symptoms**: Compilation errors - "no suitable method found for success(Code,Object)"
   - **Resolution**: Updated all controller calls to `ApiResponseData.success(data)`
   - **Files**: `ChatController.java`, `CollaborationProductController.java`, `CollaborationProposalController.java`

2. **SecurityConfig Deprecation Warning**
   - **Context**: Spring Security frameOptions() method deprecated
   - **Symptoms**: Build warning about deprecated frameOptions() usage
   - **Resolution**: Updated to `headers.frameOptions(frameOptions -> frameOptions.disable())`
   - **Files**: `SecurityConfig.java`

3. **Test Database Connection Failures**
   - **Context**: Tests trying to connect to PostgreSQL without configuration
   - **Symptoms**: `HibernateException` and `ServiceException` during test execution
   - **Resolution**: Added H2 in-memory database for tests with dedicated configuration
   - **Files**: `build.gradle`, `application-test.properties`, `ToppingApplicationTests.java`

4. **Inconsistent Repository Pattern**
   - **Context**: `UserJpaRepository` extending both domain and JPA interfaces
   - **Symptoms**: Potential bean configuration conflicts
   - **Resolution**: Created `UserRepositoryImpl` following consistent pattern
   - **Files**: `UserJpaRepository.java`, `UserRepositoryImpl.java`

### ⚙️ Commands & Configurations Modified
- **Build Dependencies**: Added `testImplementation 'com.h2database:h2'`
- **Test Configuration**: Created `application-test.properties` with H2 settings
- **Repository Structure**: Standardized all repositories to follow domain → JPA → implementation pattern
- **Security Config**: Updated deprecated Spring Security syntax

### 🧪 Test Results & Coverage Improvements
- **Before**: Tests failing due to database connection issues
- **After**: All tests passing with H2 in-memory database
- **Coverage**: Basic context loading test passes, foundation for comprehensive testing
- **Performance**: Tests run quickly with in-memory database

### 🎯 Git Commit History (Feature-Based Organization)
1. `feat: Add ROLE_BUSINESS_OWNER to security role enum`
2. `feat: Add collaboration proposal submission system`
3. `feat: Add notification system for proposal updates`
4. `feat: Add chat room system for accepted collaborations`
5. `feat: Add collaboration product management system`
6. `feat: Add AI-based profit-sharing recommendation system`
7. `feat: Enhance core entities for collaboration features`
8. `feat: Update infrastructure layer for collaboration support`
9. `feat: Update presentation layer with collaboration UI`
10. `build: Update project configuration for collaboration features`
11. `docs: Update project documentation for collaboration platform`
12. `fix: Resolve build failures and test configuration issues`

### ✅ Result
- **✅ Clean Build**: `./gradlew build` completes successfully without errors (with JAVA_HOME set)
- **✅ Test Suite**: All tests pass using H2 in-memory database
- **✅ Authentication Migration**: Successfully migrated from JWT to session-based authentication
- **✅ Consistent Architecture**: All repositories follow the same implementation pattern
- **✅ API Standards**: Controllers use proper ApiResponseData method signatures
- **✅ Modern Spring Security**: Session-based authentication with proper form login/logout configuration

### 🚀 Development Readiness
- Build system stable and reliable (requires JAVA_HOME=/mnt/d/projects/topping/jdk-17.0.12+7)
- Test infrastructure properly configured
- Authentication system migrated to session-based (removed JWT dependencies)
- Repository patterns consistent across codebase
- API response handling standardized
- Documentation comprehensive and up-to-date
- Ready for feature development and collaboration workflows

## Memorized Items
- Project follows clean architecture with domain-driven design
- All build issues resolved and tests passing
- Authentication system uses session-based authentication (no JWT)
- JAVA_HOME must be set to `/mnt/d/projects/topping/jdk-17.0.12+7` for builds
- Consistent repository pattern implementation required
- Use H2 database for testing, PostgreSQL for production
- ApiResponseData.success() takes data as first parameter, not Code enum
- Session authentication endpoints: `/login`, `/logout`, `/api/session/login`, `/api/session/logout`