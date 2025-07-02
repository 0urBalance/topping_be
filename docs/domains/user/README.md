# User Domain

## Overview
The user domain manages user accounts, profiles, and role-based access control within the collaboration platform.

## Domain Model
- **Entity**: `User.java`
- **Repository**: `UserRepository.java` (domain interface)
- **JPA Repository**: `UserJpaRepository.java` (extends JpaRepository)
- **Implementation**: `UserRepositoryImpl.java` (implements domain interface)

## User Entity Structure
- **Primary Key**: UUID (not auto-increment integers)
- **Core Fields**: username, email, password (encrypted)
- **Role-based Access**: Uses `Role` enum for authorization
- **Authentication**: Integrated with Spring Security UserDetails

## User Roles
- `ROLE_USER` - Standard user role
- `ROLE_BUSINESS_OWNER` - Business owner role for collaboration features
- Additional roles as defined in `Role.java`

## Controllers
- `UserController.java` - User management endpoints
- `AuthController.java` - User registration (signup)

## Repository Pattern
Follows the consistent three-layer pattern:
1. **Domain Interface**: `UserRepository` - Business logic interface
2. **JPA Interface**: `UserJpaRepository` - Data access interface (extends JpaRepository)
3. **Implementation**: `UserRepositoryImpl` - Concrete implementation

## Key Operations
- User registration with role assignment
- User authentication (handled by auth domain)
- Profile management
- Role-based authorization

## Integration Points
- **Authentication**: Users authenticated via session-based system
- **Collaboration**: Users can be business owners or collaborators
- **Products**: Users can create and manage products
- **Chat**: Users participate in chat rooms

## Database Configuration
- **Production**: PostgreSQL with UUID primary keys
- **Testing**: H2 in-memory database
- **Password Storage**: BCrypt encryption via Spring Security

## API Endpoints
- `POST /api/member/signup` - User registration
- User profile endpoints (to be documented as implemented)

## Security Considerations
- Passwords encrypted with BCrypt
- Role-based access control via Spring Security
- Session-based authentication prevents unauthorized access
- UUID primary keys prevent enumeration attacks