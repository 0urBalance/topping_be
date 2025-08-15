# Auth Domain - Claude Guidance

## Overview
The Auth domain handles session-based authentication, authorization, and Kakao social login integration for the Topping platform.

## Authentication Architecture

### Session-Based Authentication
```java
// SecurityConfig patterns
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SessionCreationPolicy sessionCreationPolicy() {
        return SessionCreationPolicy.IF_REQUIRED; // Creates sessions as needed
    }
    
    // CSRF disabled for simplified form handling
    .csrf(csrf -> csrf.disable())
}
```

### Session Management
- **JSESSIONID Cookie**: Maintains authentication across requests
- **Spring Security**: Integrated with UserDetailsImpl and UserDetailsServiceImpl
- **Session Persistence**: Configured for reliable authentication state

## User Authentication Flow

### Login Process
```java
@PostMapping("/api/session/login")
public ResponseEntity<ApiResponseData<String>> login(@RequestBody LoginRequest request, 
                                                   HttpServletRequest httpRequest) {
    // Authenticate user
    Authentication auth = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
    );
    
    // Set security context
    SecurityContextHolder.getContext().setAuthentication(auth);
    
    // Create session
    HttpSession session = httpRequest.getSession(true);
    
    return ResponseEntity.ok(ApiResponseData.success("로그인 성공"));
}
```

### Session Status Check
```java
@GetMapping("/api/session/user")
public ResponseEntity<ApiResponseData<SessionUserInfo>> getCurrentUser(
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
    if (userDetails == null) {
        return ResponseEntity.ok(ApiResponseData.failure("USER_NOT_AUTHENTICATED", "인증되지 않은 사용자"));
    }
    
    SessionUserInfo userInfo = SessionUserInfo.from(userDetails.getUser());
    return ResponseEntity.ok(ApiResponseData.success(userInfo));
}
```

## Kakao Social Login Integration

### OAuth Flow
```java
@Service
public class KakaoService {
    
    @Value("${kakao.rest-api-key}")
    private String kakaoRestApiKey;
    
    public String authenticateWithKakao(String code) {
        // 1. Exchange code for access token
        String accessToken = getKakaoAccessToken(code);
        
        // 2. Get user info from Kakao
        KakaoUserInfoDto kakaoUser = getKakaoUserInfo(accessToken);
        
        // 3. Find or create user
        User user = findOrCreateUser(kakaoUser);
        
        // 4. Create Spring Security session (NOT JWT)
        createAuthenticationSession(user);
        
        return "로그인 성공";
    }
    
    private User findOrCreateUser(KakaoUserInfoDto kakaoUser) {
        return userRepository.findByEmail(kakaoUser.getEmail())
            .orElseGet(() -> createNewKakaoUser(kakaoUser));
    }
    
    private User createNewKakaoUser(KakaoUserInfoDto kakaoUser) {
        return User.builder()
            .email(kakaoUser.getEmail())
            .name(kakaoUser.getNickname())
            .role(Role.USER) // Default role
            .sggCode("11680") // Default Seoul/Gangnam
            .build();
    }
}
```

### Kakao User Data Model
```java
@Data
public class KakaoUserInfoDto {
    private Long id;
    private String nickname;
    private String email;
    private String profileImage;
    
    // Domain validation logic
    public boolean isValidForRegistration() {
        return email != null && !email.trim().isEmpty() 
            && nickname != null && !nickname.trim().isEmpty();
    }
}
```

## Authorization Patterns

### Controller Level Authorization
```java
// Method level security
@PreAuthorize("hasRole('BUSINESS_OWNER') or hasRole('ADMIN')")
@GetMapping("/stores/register")
public String storeRegistrationForm() {
    return "store/register";
}

// Authentication required
@GetMapping("/mypage")
public String myPage(@AuthenticationPrincipal UserDetailsImpl userDetails) {
    if (userDetails == null) {
        return "redirect:/auth/login";
    }
    return "mypage/dashboard";
}
```

### Template Level Authorization
```html
<!-- Role-based rendering -->
<div sec:authorize="hasRole('BUSINESS_OWNER')">
    <a href="/stores/register" class="btn">가게 등록</a>
</div>

<!-- Authentication check -->
<div sec:authorize="isAuthenticated()">
    <span th:text="${#authentication.principal.user.name}">사용자명</span>
    <a href="/auth/logout">로그아웃</a>
</div>

<div sec:authorize="!isAuthenticated()">
    <a href="/auth/login">로그인</a>
    <a href="/auth/signup">회원가입</a>
</div>
```

## Route Protection

### Security Configuration
```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                // Public routes
                .requestMatchers("/", "/auth/**", "/api/auth/**").permitAll()
                .requestMatchers("/support/cs", "/support/faq/**").permitAll()
                
                // Protected routes
                .requestMatchers("/mypage/**").authenticated()
                .requestMatchers("/products/**").authenticated()
                .requestMatchers("/collaborations/**").authenticated()
                .requestMatchers("/chat/**").authenticated()
                
                // Admin/Business owner routes
                .requestMatchers("/stores/register").hasAnyRole("BUSINESS_OWNER", "ADMIN")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            .build();
    }
}
```

## User Details Integration

### UserDetailsImpl
```java
@Getter
public class UserDetailsImpl implements UserDetails {
    private final User user;
    
    public UserDetailsImpl(User user) {
        this.user = user;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
    }
    
    @Override
    public String getUsername() {
        return user.getEmail();
    }
    
    // Additional UserDetails methods...
}
```

### Service Implementation
```java
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
        
        return new UserDetailsImpl(user);
    }
}
```

## Common Pitfalls
- ❌ **JWT Usage**: This project uses session-based auth, NOT JWT tokens
- ❌ **Manual Session**: Don't manually create SecurityContext, use authentication flow
- ❌ **Hard-coded Roles**: Use Role enum constants, not string literals
- ❌ **Missing CSRF**: CSRF is disabled, but be aware for form submissions
- ❌ **Kakao Token Storage**: Don't store Kakao access tokens, only use for initial auth

## API Response Patterns
```java
// Session endpoints with ApiResponseData wrapper
@PostMapping("/api/session/logout")
public ResponseEntity<ApiResponseData<String>> logout(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) {
        session.invalidate();
    }
    SecurityContextHolder.clearContext();
    return ResponseEntity.ok(ApiResponseData.success("로그아웃 성공"));
}
```

## Integration Points
- **User Domain**: User entities and role management
- **Store Domain**: Business owner verification for store operations
- **Chat Domain**: User authentication for chat access
- **All Domains**: Session-based access control across all features

## Testing Patterns
```java
@ActiveProfiles("test")
class AuthControllerTest {
    // Mock authentication context
    // Test session creation and destruction
    // Verify role-based access control
    
    @WithMockUser(roles = "BUSINESS_OWNER")
    @Test
    void testStoreAccess() {
        // Test business owner access
    }
}
```

## Related Documentation
- [Main Claude Guidance](../../../CLAUDE.md)
- [User Domain](../user/CLAUDE.md)
- [Session Persistence Troubleshooting](../../SESSION_PERSISTENCE_TROUBLESHOOTING.md)