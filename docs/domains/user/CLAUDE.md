# User Domain - Claude Guidance

## Overview
The User domain handles user accounts, profiles, authentication, and role-based access control in the Topping platform.

## Core Entities
- **User** - Main user entity with UUID primary key
- **Role** - Enum-based role system (USER, BUSINESS_OWNER, ADMIN)
- **UserDetailsImpl** - Spring Security integration

## Key Patterns

### User Authentication
- **Session-based authentication** using Spring Security
- **Principal injection** in controllers: `@AuthenticationPrincipal UserDetailsImpl userDetails`
- **User retrieval**: `userDetails.getUser()` or `userDetails.getUsername()`

### Role-Based Access Control
```java
// Controller level authorization
@PreAuthorize("hasRole('BUSINESS_OWNER') or hasRole('ADMIN')")

// Template level checks
sec:authorize="hasRole('BUSINESS_OWNER')"
```

### Repository Pattern
- **Domain Interface**: `UserRepository` 
- **JPA Interface**: `UserJpaRepository extends JpaRepository<User, UUID>`
- **Implementation**: `UserRepositoryImpl implements UserRepository`

## Service Layer Patterns

### UserService Key Methods
- `findByEmail()` - User lookup for authentication
- `createUser()` - New user registration 
- `updateUserProfile()` - Profile management
- `checkRole()` - Role validation

### Kakao Integration
- **KakaoService** handles OAuth flow in application layer
- **Automatic user creation** with default settings (Seoul/Gangnam, ROLE_USER)
- **Session integration** via SecurityContextHolder (NOT JWT)

## Template Patterns

### User Session Access
```html
<!-- Current user access -->
<span th:text="${#authentication.principal.user.name}"></span>

<!-- Role-based rendering -->
<div sec:authorize="hasRole('BUSINESS_OWNER')">
    <a href="/stores/register">가게 등록</a>
</div>
```

### Form Binding
```html
<!-- User profile form -->
<form th:object="${userForm}" method="post">
    <input th:field="*{name}" type="text" />
    <input th:field="*{email}" type="email" />
</form>
```

## API Response Patterns
```java
// User session endpoint
@GetMapping("/api/session/user")
public ResponseEntity<ApiResponseData<SessionUserInfo>> getCurrentUser() {
    return ResponseEntity.ok(ApiResponseData.success(sessionUserInfo));
}
```

## Common Pitfalls
- ❌ **Manual UUID Setting**: Never set UUID for @GeneratedValue entities
- ❌ **JWT Usage**: This project uses session-based auth, not JWT tokens
- ❌ **Role Hardcoding**: Use Role enum constants, not string literals
- ❌ **Direct User Creation**: Always use UserService for user operations

## Integration Points
- **Store Domain**: Business owners can register stores
- **Collaboration Domain**: Users create and manage collaborations
- **Chat Domain**: User identification for chat rooms
- **Auth Domain**: Session management and Kakao login

## Testing Patterns
```java
@ActiveProfiles("test")
class UserServiceTest {
    // Use H2 in-memory database
    // Mock authentication context for role testing
}
```

## Related Documentation
- [Main Claude Guidance](../../../CLAUDE.md)
- [Auth Domain](../auth/CLAUDE.md)
- [Store Domain](../store/CLAUDE.md)