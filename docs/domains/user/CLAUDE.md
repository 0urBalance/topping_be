# User Domain - Claude Guidance

## Overview
사용자 계정, 프로필, 세션 인증, 역할 기반 접근 제어(RBAC)를 담당.

## Core Entities
- **User** — UUID PK, email, name, role, sggCode, phoneNumber
- **Role** — `USER` | `BUSINESS_OWNER` | `ADMIN`
- **UserDetailsImpl** — Spring Security 통합 래퍼

## Key Patterns

### 인증 정보 접근
```java
// Controller에서 현재 사용자 조회
@AuthenticationPrincipal UserDetailsImpl userDetails
userDetails.getUser()       // User 엔티티
userDetails.getUsername()   // email

// Template에서 접근
${#authentication.principal.user.name}
```

### Role 기반 접근 제어
```java
// Controller
@PreAuthorize("hasRole('BUSINESS_OWNER') or hasRole('ADMIN')")

// Template
sec:authorize="hasRole('BUSINESS_OWNER')"
sec:authorize="isAuthenticated()"
sec:authorize="!isAuthenticated()"
```

### Kakao 소셜 로그인
- KakaoService에서 OAuth 처리 (application layer)
- 신규 사용자 자동 생성: 기본 role=USER, sggCode="11680" (서울 강남)
- **세션** 방식 (SecurityContextHolder) — JWT 토큰 방식 아님

## Common Pitfalls
- ❌ **UUID 수동 설정**: `@GeneratedValue` 엔티티에 UUID 수동 지정 금지
- ❌ **JWT 사용**: 세션 기반 인증 사용, JWT 토큰 방식 아님
- ❌ **Role 하드코딩**: `Role` enum 상수 사용, 문자열 리터럴 금지
- ❌ **직접 사용자 생성**: 항상 `UserService`를 통해 사용자 생성

## Integration Points
- **Store Domain**: BUSINESS_OWNER만 가게 등록 가능
- **Collaboration Domain**: 사용자가 콜라보 생성/관리
- **Chat Domain**: 채팅방 참가자 식별
- **Auth Domain**: 세션 관리, Kakao 로그인

## Related Documentation
- [Main Claude Guidance](../../../CLAUDE.md)
- [Auth Domain](../auth/CLAUDE.md)
- [Store Domain](../store/CLAUDE.md)
