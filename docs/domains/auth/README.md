# Authentication Domain

## Overview
The authentication system manages user login, logout, and session management using Spring Security's session-based authentication.

## Architecture
- **Authentication Method**: Spring Security session-based authentication (migrated from JWT)
- **Session Management**: Server-side session storage with JSESSIONID cookie
- **Security Features**: Session invalidation on logout, cookie deletion, role-based access control

## Endpoints

### Form-Based Authentication
- **Login**: `POST /login` - Standard HTML form authentication
- **Logout**: `POST /logout` - Standard logout with redirect

### API-Based Authentication
- **API Login**: `POST /api/session/login` - JSON-based login for frontend applications
- **API Logout**: `POST /api/session/logout` - JSON-based logout
- **Status Check**: `POST /api/member/login-status` - Check current authentication status

## Controllers
- `AuthController.java` - Handles template rendering and legacy auth endpoints
- `SessionAuthController.java` - Handles programmatic session management

## Security Configuration
- `SecurityConfig.java` - Spring Security configuration with form login/logout
- `UserDetailsServiceImpl.java` - User details service for authentication
- `UserDetailsImpl.java` - User details implementation

## Authentication Flow

### Login Process
1. User submits credentials to `/login` or `/api/session/login`
2. Spring Security authenticates against `UserDetailsService`
3. On success, creates HTTP session with `JSESSIONID` cookie
4. Session stored server-side, cookie sent to client

### Session Maintenance
- Session automatically maintained via `JSESSIONID` cookie
- No token storage needed in localStorage
- Session persists across browser tabs/windows
- Session expires based on server timeout settings

### Logout Process
1. User calls `/logout` or `/api/session/logout`
2. Server invalidates session
3. `JSESSIONID` cookie deleted
4. User redirected to home page

## Migration Notes
- **From JWT**: Successfully migrated from JWT-based to session-based authentication
- **Removed Components**: JWT service, filters, DTOs, and dependencies
- **Frontend Impact**: Frontend applications need to remove JWT token handling and use session cookies

## Session Persistence Fixes (Latest)

### Issues Resolved
- **Mixed Authentication Models**: Removed conflicting JWT localStorage checks from templates
- **Route Protection**: Fixed overly permissive `anyRequest().permitAll()` configuration
- **Controller Logic**: Updated controllers to rely on Spring Security authentication guarantees
- **Template Integration**: Ensured consistent `sec:authorize="isAuthenticated()"` usage

### Technical Changes
- **SecurityConfig.java**: Changed to `anyRequest().authenticated()` with explicit public exceptions
- **Route Protection**: Added authentication requirements for `/collabo/**`, `/products/**`, `/chat/**`, etc.
- **Template Updates**: Removed localStorage-based authentication checks from collabo.html and mypage.html
- **Controller Updates**: Enhanced CollaboController and ProductController with proper Principal handling

### Verification Results
- ✅ **Session Creation**: JSESSIONID cookie created successfully on login
- ✅ **Cross-Request Persistence**: Authentication maintained across all protected routes
- ✅ **Automatic Redirects**: Unauthenticated users properly redirected to login
- ✅ **Template Security**: Navbar correctly shows authentication state
- ✅ **API Endpoints**: Session status can be verified via `/api/session/status`

## Testing
- Use `@ActiveProfiles("test")` for test configurations
- H2 in-memory database for testing authentication flows
- Session authentication can be tested with cookie-based requests
- **Debug Logging**: Enhanced logging available for authentication troubleshooting