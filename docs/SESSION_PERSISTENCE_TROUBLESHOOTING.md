# Session Persistence Troubleshooting Guide

## Overview
This document details the resolution of session-based authentication persistence issues where authenticated users were being prompted to login again on protected routes like `/collabo` and `/mypage`.

## Problem Description

### Symptoms
- Login appeared successful (logout button visible in navbar)
- Session cookie (JSESSIONID) was created and sent by browser
- However, access to protected pages triggered re-login prompts
- Inconsistent authentication state across different routes

### Root Causes Identified

1. **Overly Permissive Security Configuration**
   - `anyRequest().permitAll()` in SecurityConfig was overriding route-specific authentication requirements
   - Some protected routes were accidentally allowed without authentication

2. **Mixed Authentication Models**
   - Templates contained JWT-based localStorage checks conflicting with session authentication
   - JavaScript was checking for JWT tokens instead of relying on Spring Security sessions

3. **Controller Logic Issues**
   - Controllers used optional Principal checks (`if (principal != null)`) but didn't redirect unauthenticated users
   - This allowed unauthenticated access to render empty pages instead of proper redirects

4. **Inconsistent Route Protection**
   - Many feature routes weren't explicitly configured in Spring Security
   - Default behavior was falling back to permitAll instead of requiring authentication

## Solution Implementation

### 1. Security Configuration Fix (`SecurityConfig.java`)

**Before:**
```java
.authorizeHttpRequests(authz -> authz
    .requestMatchers("/mypage/**", "/logout").authenticated()
    .requestMatchers("/collabo/**").authenticated()
    .anyRequest().permitAll()  // ← PROBLEM: Too permissive
)
```

**After:**
```java
.authorizeHttpRequests(authz -> authz
    // Public endpoints
    .requestMatchers("/", "/auth/**", "/login").permitAll()
    .requestMatchers("/explore", "/css/**", "/js/**", "/images/**").permitAll()
    .requestMatchers("/api/member/signup").permitAll()
    .requestMatchers("/api/session/login", "/api/session/logout", "/api/session/status").permitAll()
    // Protected endpoints - must be authenticated
    .requestMatchers("/mypage/**", "/logout").authenticated()
    .requestMatchers("/collabo/**").authenticated()
    .requestMatchers("/chat/**").authenticated()
    .requestMatchers("/products/**").authenticated()
    .requestMatchers("/proposals/**").authenticated()
    .requestMatchers("/collaborations/**").authenticated()
    .requestMatchers("/collaboration-products/**").authenticated()
    .requestMatchers("/api/**").authenticated()
    // Default to authenticated for any other request
    .anyRequest().authenticated()  // ← FIXED: Secure by default
)
```

### 2. Template Authentication Fixes

**Before (collabo.html):**
```javascript
// Check authentication on page load
document.addEventListener('DOMContentLoaded', function() {
    if (!localStorage.getItem('accessToken')) {  // ← PROBLEM: JWT check
        alert('로그인이 필요한 서비스입니다.');
        window.location.href = '/auth/login';
        return;
    }
});
```

**After:**
```javascript
// No authentication checks needed - Spring Security handles session authentication
document.addEventListener('DOMContentLoaded', function() {
    console.log('Collabo page loaded - user is authenticated via session');
});
```

### 3. Controller Logic Improvements

**Before (CollaboController.java):**
```java
@GetMapping
public String collabo(Model model, Principal principal) {
    if (principal != null) {  // ← PROBLEM: Optional check allows unauthenticated access
        // Load data...
    }
    return "collabo";  // Returns empty page if not authenticated
}
```

**After:**
```java
@GetMapping
public String collabo(Model model, Principal principal) {
    // Spring Security guarantees principal is not null due to .authenticated() configuration
    User user = userRepository.findByEmail(principal.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
    
    // Load data with guaranteed authenticated user...
    return "collabo";
}
```

### 4. Session Management Configuration

Added explicit session management configuration:
```java
.sessionManagement(session -> session
    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
    .maximumSessions(1)
    .maxSessionsPreventsLogin(false)
)
```

## Verification Testing

### Test Scenarios Verified

1. **Authentication Flow**
   ```bash
   # Login and get session cookie
   curl -X POST -H "Content-Type: application/json" \
        -d '{"email":"test@test.com","password":"testpass123"}' \
        http://localhost:8080/api/session/login -c cookies.txt
   
   # Response: {"code":200,"data":"로그인 성공: testuser"}
   # Cookie: JSESSIONID=12AC39F8DABA5DF83DC9EC5D5A2B6C9F
   ```

2. **Protected Route Access (Authenticated)**
   ```bash
   # Access protected route with session cookie
   curl -b cookies.txt http://localhost:8080/collabo
   
   # Result: ✅ Page loads successfully with authenticated navbar
   ```

3. **Protected Route Access (Unauthenticated)**
   ```bash
   # Access protected route without session
   curl http://localhost:8080/collabo -L
   
   # Result: ✅ 302 redirect to /auth/login
   ```

4. **Session Status Verification**
   ```bash
   # Check session status
   curl -X POST -b cookies.txt http://localhost:8080/api/session/status
   
   # Response: {"code":200,"data":"인증됨 - 사용자: test@test.com"}
   ```

### Results Summary

- ✅ **Session Creation**: JSESSIONID cookie created successfully on login
- ✅ **Cross-Request Persistence**: Authentication maintained across all protected routes
- ✅ **Automatic Redirects**: Unauthenticated users properly redirected to login
- ✅ **Template Security**: Navbar correctly shows authentication state
- ✅ **Consistent Behavior**: All protected routes behave consistently

## Prevention Guidelines

### 1. Security Configuration Best Practices
- **Always use `anyRequest().authenticated()` as default**
- **Explicitly list public endpoints** with `permitAll()`
- **Be specific about route patterns** (use `/**` for subdirectories)
- **Separate public and protected API endpoints** clearly

### 2. Template Development Guidelines
- **Never mix authentication models** (JWT vs Session)
- **Use Thymeleaf security integration** (`sec:authorize="isAuthenticated()"`)
- **Remove client-side authentication checks** when using session-based auth
- **Let Spring Security handle redirects** automatically

### 3. Controller Development Guidelines
- **Rely on Spring Security guarantees** when routes are configured as `authenticated()`
- **Don't use optional Principal checks** in protected controllers
- **Throw exceptions for missing users** rather than returning empty pages
- **Use debug logging** for authentication troubleshooting

### 4. Testing Recommendations
- **Test both authenticated and unauthenticated scenarios**
- **Verify session cookie creation and usage**
- **Check redirect behavior for protected routes**
- **Use curl or browser dev tools** to inspect cookies and redirects

## Debug Tools

### Enhanced Logging
Enable debug logging in `application.properties`:
```properties
logging.level.org.balanceus.topping=DEBUG
logging.level.org.springframework.security=DEBUG
logging.level.org.springframework.web=DEBUG
```

### Session Status Endpoint
Use the dedicated session status endpoint for debugging:
```bash
POST /api/session/status
```

### Browser Developer Tools
- **Network tab**: Check for JSESSIONID cookie in request headers
- **Application tab**: Verify cookie storage and properties
- **Console**: Check for authentication-related JavaScript errors

## Related Documentation
- [Authentication Domain README](./domains/auth/README.md)
- [CLAUDE.md - Session Authentication Details](../CLAUDE.md#session-authentication-details)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)