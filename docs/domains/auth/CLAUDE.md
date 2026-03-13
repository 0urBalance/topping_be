# Auth Domain - Claude Guidance

## Overview
세션 기반 인증, Spring Security 설정, Kakao 소셜 로그인 통합을 담당.

## Key Patterns

### 세션 관리
- **JSESSIONID 쿠키**로 인증 상태 유지
- `SessionCreationPolicy.IF_REQUIRED` — 필요 시 세션 생성
- CSRF: 폼 처리 단순화를 위해 비활성화

### 라우트 권한 규칙
```
/                           → permitAll
/auth/**                    → permitAll
/api/auth/**                → permitAll
/support/cs, /support/faq/** → permitAll
/mypage/**                  → authenticated
/products/**                → authenticated
/collaborations/**          → authenticated
/chat/**                    → authenticated
/stores/register            → BUSINESS_OWNER or ADMIN
/admin/**                   → ADMIN only
```

### Kakao 소셜 로그인
1. 인가 코드 수신 → Kakao 액세스 토큰 교환
2. Kakao 사용자 정보 조회
3. DB에서 사용자 조회 (없으면 자동 생성: role=USER, sggCode="11680")
4. Spring Security 세션 생성 (JWT 아님)
- Kakao 액세스 토큰은 초기 인증에만 사용, 저장 금지

### Template 권한 확인
```html
<div sec:authorize="hasRole('BUSINESS_OWNER')">...</div>
<div sec:authorize="isAuthenticated()">...</div>
<div sec:authorize="!isAuthenticated()">...</div>
<span th:text="${#authentication.principal.user.name}"></span>
```

## Common Pitfalls
- ❌ **JWT 사용**: 세션 기반 인증 사용, JWT 토큰 방식 아님
- ❌ **SecurityContext 수동 생성**: 인증 흐름 통해서만 처리
- ❌ **Role 하드코딩**: `Role` enum 상수 사용
- ❌ **CSRF 주의**: CSRF 비활성화 상태지만 폼 제출 시 인지 필요
- ❌ **Kakao 토큰 저장**: Kakao 액세스 토큰 DB 저장 금지

## Integration Points
- **User Domain**: User 엔티티, role 관리
- **Store Domain**: BUSINESS_OWNER 역할 검증
- **Chat Domain**: 채팅 접근 인증
- **전 도메인**: 세션 기반 접근 제어

## Related Documentation
- [Main Claude Guidance](../../../CLAUDE.md)
- [User Domain](../user/CLAUDE.md)
- [Session Persistence Troubleshooting](../../SESSION_PERSISTENCE_TROUBLESHOOTING.md)
