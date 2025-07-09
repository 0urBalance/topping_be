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
- **Signup**: `POST /api/member/signup` - User registration with terms agreement

### API-Based Authentication
- **API Login**: `POST /api/session/login` - JSON-based login for frontend applications
- **API Logout**: `POST /api/session/logout` - JSON-based logout
- **Status Check**: `POST /api/member/login-status` - Check current authentication status
- **Email Check**: `GET /api/auth/check-email` - Verify email availability during signup

### Public Authentication Pages
- **Login Page**: `GET /auth/login` - Login form with email verification
- **Signup Page**: `GET /auth/signup` - Registration form with policy agreement
- **Password Recovery**: `GET /auth/password-recovery/find` - Password reset flow

## Controllers
- `AuthController.java` - Handles template rendering and legacy auth endpoints
- `SessionAuthController.java` - Handles programmatic session management
- `PolicyController.java` - Handles privacy policy and terms of service for signup

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

### Enhanced Signup Process
1. User visits `/auth/signup` with comprehensive registration form
2. Form includes required fields: name, email, role, region, password
3. **Policy Agreement**: User must check agreement box for privacy policy and terms of service
4. **Modal Integration**: Policy links open modals with full policy content
5. **Dynamic Content**: Policy content loaded via AJAX from `/policy/privacy-modal` and `/policy/terms-modal`
6. **Form Validation**: Cannot submit without checking agreement checkbox
7. **Email Verification**: Real-time email availability checking during input
8. **Region Selection**: Cascading dropdown for region and city selection
9. On successful validation, account created and user redirected to login

### Policy Integration Features
- **Modal System**: Privacy policy and terms of service displayed in responsive modals
- **Dynamic Loading**: Policy content loaded on-demand with caching
- **Accessibility**: Keyboard navigation and screen reader support
- **Mobile Optimization**: Responsive design for all device sizes
- **Error Handling**: Graceful fallback for content loading failures

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
- **Route Protection**: Added authentication requirements for `/collabo/**`, `/products/**`, `/chat/**`, `/support/inquiry*`, etc.
- **Template Updates**: Removed localStorage-based authentication checks from collabo.html and mypage.html
- **Controller Updates**: Enhanced CollaboController and ProductController with proper Principal handling
- **Policy Integration**: Added public access for `/policy/**` endpoints to support modal functionality
- **Support System**: Protected inquiry endpoints while keeping FAQ access public

### Verification Results
- ✅ **Session Creation**: JSESSIONID cookie created successfully on login
- ✅ **Cross-Request Persistence**: Authentication maintained across all protected routes
- ✅ **Automatic Redirects**: Unauthenticated users properly redirected to login
- ✅ **Template Security**: Navbar correctly shows authentication state
- ✅ **API Endpoints**: Session status can be verified via `/api/session/status`
- ✅ **Policy Modal System**: Privacy policy and terms of service accessible via modals
- ✅ **Signup Enhancement**: Terms agreement validation working correctly
- ✅ **Support System Integration**: FAQ public access and inquiry protection working

## Testing
- Use `@ActiveProfiles("test")` for test configurations
- H2 in-memory database for testing authentication flows
- Session authentication can be tested with cookie-based requests
- **Debug Logging**: Enhanced logging available for authentication troubleshooting

## Recent Enhancements (December 2024)

### Customer Support System Integration
- **FAQ Access**: Public access to customer support FAQ system
- **Inquiry Protection**: Authentication required for inquiry submission and management
- **Template Integration**: Support links integrated into main navigation and footer

### Policy Modal System
- **Signup Integration**: Required terms and privacy policy agreement during registration
- **Modal Functionality**: Dynamic modal loading for policy content with caching
- **Footer Integration**: Policy links available on all pages with consistent modal experience
- **Accessibility**: Full keyboard navigation and screen reader support

### Enhanced Signup Process
- **Terms Agreement**: Cannot complete signup without agreeing to terms and privacy policy
- **Modal Policy Access**: Users can read policies during signup without leaving the page
- **Form Validation**: Comprehensive validation including agreement checkbox
- **Dynamic Content**: Policy content loaded on-demand with error handling
- **Mobile Optimization**: Responsive design for all device sizes

### Security Improvements
- **Route Protection**: Enhanced protection for support inquiry endpoints
- **Public Access**: Proper public access for FAQ and policy content
- **CSRF Protection**: Maintained for all form submissions
- **Session Management**: Consistent session handling across all new features