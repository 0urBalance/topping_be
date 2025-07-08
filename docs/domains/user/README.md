# User Domain

## Purpose and Core Responsibilities

The User domain manages user accounts, profiles, role-based access control, and user-related operations within the Topping collaboration platform. This domain provides the foundation for authentication, authorization, and user identity management across all platform features.

## Key Models/Entities

### User Entity
- **Location**: `src/main/java/org/balanceus/topping/domain/model/User.java`
- **Primary Key**: UUID (not auto-increment integers)
- **Core Fields**:
  - `email` - User email address (unique identifier)
  - `password` - BCrypt encrypted password
  - `username` - Display name for the user
  - `role` - Role-based access control (enum)
  - `store` - One-to-one relationship with Store (optional)

### Role Enum
- **Location**: `src/main/java/org/balanceus/topping/infrastructure/security/Role.java`
- **Available Roles**:
  - `ROLE_USER` - Standard platform user
  - `ROLE_BUSINESS_OWNER` - Business owner with store management privileges
  - `ROLE_MANAGER` - Management role (future use)
  - `ROLE_ADMIN` - Administrator role with full platform access

### UserDetails Implementation
- **Location**: `src/main/java/org/balanceus/topping/infrastructure/security/UserDetailsImpl.java`
- **Purpose**: Spring Security integration for authentication
- **Features**: Maps User entity to Spring Security UserDetails interface

## Main APIs and Endpoints

### Authentication Endpoints (AuthController)
- **GET `/login`** - Display login form
- **GET `/signup`** - Display registration form
- **POST `/api/member/signup`** - Process user registration

### Session Management
- **POST `/api/session/login`** - API-based login
- **POST `/api/session/logout`** - API-based logout
- **GET `/api/session/status`** - Check authentication status

### User Management
- **Profile Management**: Integrated within MyPage functionality
- **Role-based Access**: Enforced across all protected endpoints

## Repository Pattern

### Domain Repository
- **Interface**: `src/main/java/org/balanceus/topping/domain/repository/UserRepository.java`
- **Key Methods**:
  - `findByEmail(String email)` - Lookup user by email
  - `existsByEmail(String email)` - Check if email exists
  - `findByUsername(String username)` - Find user by username

### JPA Repository
- **Interface**: `src/main/java/org/balanceus/topping/infrastructure/persistence/UserJpaRepository.java`
- **Extends**: `JpaRepository<User, UUID>`

### Implementation
- **Class**: `src/main/java/org/balanceus/topping/infrastructure/persistence/UserRepositoryImpl.java`
- **Implements**: Domain repository using JPA repository

## Business Rules and Constraints

### User Registration
- **Email Uniqueness**: Each email can only be associated with one account
- **Password Security**: Passwords encrypted using BCrypt
- **Default Role**: New users assigned `ROLE_USER` by default
- **Username Requirements**: Must be unique and non-empty

### Role-Based Access Control
- **Store Management**: Requires `ROLE_BUSINESS_OWNER` or `ROLE_ADMIN`
- **Product Creation**: Available to authenticated users
- **Administrative Functions**: Require `ROLE_ADMIN`
- **Collaboration Features**: Available to all authenticated users

### Security Constraints
- **Session-Based Authentication**: Uses JSESSIONID cookies
- **Password Encryption**: BCrypt with Spring Security defaults
- **Account Lockout**: Future implementation for security
- **Email Verification**: Future implementation for account validation

## Integration Points

### Authentication Domain
- **Session Management**: User identity maintained through Spring Security sessions
- **Login/Logout**: Integration with session-based authentication system
- **Access Control**: Role-based authorization across platform features

### Store Domain
- **One-to-One Relationship**: Business owners can have one store
- **Role Requirement**: Store management requires `ROLE_BUSINESS_OWNER`
- **Store Ownership**: Users are linked to their stores through foreign key relationship

### Product Domain
- **Product Ownership**: Users can create and manage products
- **Creator Relationship**: Many-to-one relationship between products and users
- **Access Control**: Only creators can modify their products

### Collaboration Domain
- **Collaboration Participants**: Users can be applicants or product owners in collaborations
- **Business Matching**: User roles determine collaboration capabilities
- **Collaboration History**: Users have collaboration application and proposal history

### MyPage Integration
- **User Dashboard**: Centralized user interface for account management
- **Role-based Features**: Different features available based on user role
- **Profile Management**: User information accessible through MyPage

## Service Layer

### User Service
- **Registration Logic**: Handle new user creation with proper role assignment
- **Profile Management**: Update user information and preferences
- **Role Management**: Handle role changes and validation
- **Security Operations**: Password changes and account security

## Error Handling

### Common Error Scenarios
- **Duplicate Email**: Registration with existing email address
- **Invalid Credentials**: Login with incorrect email/password
- **Access Denied**: Role-based access control violations
- **Session Expired**: Authentication timeout scenarios

### Error Responses
- **Registration Errors**: Validation messages and form error display
- **Authentication Errors**: Clear feedback for login failures
- **Authorization Errors**: Appropriate redirects and error messages

## Security Features

### Password Management
- **BCrypt Encryption**: Industry-standard password hashing
- **Salt Generation**: Automatic salt generation for password security
- **Password Validation**: Future implementation for password strength requirements

### Session Security
- **Session Timeout**: Configurable session expiration
- **Session Fixation Protection**: Spring Security session fixation prevention
- **CSRF Protection**: Cross-site request forgery protection (disabled for API endpoints)

### Role-Based Security
- **Method-level Security**: `@PreAuthorize` annotations for role checking
- **URL-based Security**: Security configuration for endpoint protection
- **Template Security**: Thymeleaf security integration for UI role-based display

## Recent Updates and Enhancements

### Role-Based Access Control Enhancement
- ✅ Enhanced store management with proper role validation
- ✅ Implemented business owner role checking across store endpoints
- ✅ Added access denied handling with user-friendly error messages

### MyPage Integration
- ✅ User information display in MyPage dashboard
- ✅ Role-based feature visibility in user interface
- ✅ Upgrade prompts for users without business owner role

### Session Authentication Improvements
- ✅ Migrated from JWT to session-based authentication
- ✅ Enhanced session persistence across protected routes
- ✅ Improved authentication debugging and logging