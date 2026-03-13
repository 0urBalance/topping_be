# CLAUDE.md

This file provides guidance to Claude Code when working with this repository.

## Critical Rules

> These override everything else. Violating any of these causes bugs or build failures.

1. **Session auth only** — Use `Principal` or `@AuthenticationPrincipal UserDetailsImpl`. JWT 사용 금지
2. **Entity UUID** — 모든 엔티티는 `getUuid()` 사용. `getUserId()`, `getRoomId()` 등 존재하지 않음 (Lombok이 필드명에서 getter 생성)
3. **API 응답** — `ApiResponseData.success(data)` 사용. `ApiResponseData.success(Code.SUCCESS, data)` 형태 사용 금지
4. **Thymeleaf fragment** — `th:replace="~{fragments/navbar :: navbar}"` (Thymeleaf 3 문법)
5. **CSS 금지** — Bootstrap CDN, inline styles, 외부 CSS 프레임워크 사용 금지
6. **UUID 자동 생성** — `@GeneratedValue` 엔티티에 UUID 수동 설정 금지
7. **JPA JOIN FETCH** — 단일 쿼리에 다중 JOIN FETCH 금지 (Cartesian Product 발생 → 데이터 중복)

---

## Project Overview

Topping(토핑)은 Spring Boot 기반 협업 매칭 플랫폼. 사업자 간 콜라보 파트너 매칭 및 협업 프로젝트 관리.

## Quick Start

### Environment
- **JAVA_HOME**: `/mnt/d/projects/topping/jdk-17.0.12+7`

### Commands
```bash
export JAVA_HOME=/mnt/d/projects/topping/jdk-17.0.12+7
./gradlew build      # 빌드
./gradlew bootRun    # 실행
./gradlew test       # 테스트
```

### Database
- **Production**: PostgreSQL (`DB_URL`, `DB_USER`, `DB_PASSWORD` 환경변수)
- **Testing**: H2 in-memory
- **Config**: spring-dotenv 사용, 프로젝트 루트에 `.env` 파일 생성
- **Pool**: HikariCP (5-30 connections, leak detection enabled)

---

## Architecture

### Clean Architecture Layers
- **`domain/`** — 핵심 비즈니스 로직, 엔티티
- **`infrastructure/`** — 기술 구현 (JPA, 파일 저장 등)
- **`presentation/`** — REST 컨트롤러, DTO
- **`application/`** — 애플리케이션 서비스, DTO

### Tech Stack
- Spring Boot 3.5.3 + Java 17
- Spring Data JPA + PostgreSQL
- Spring Security (session-based)
- Lombok + Thymeleaf
- WebSocket (SockJS + STOMP)

---

## Development Standards

### Repository Pattern (전 도메인 공통)
- **Domain Interface**: `{Domain}Repository`
- **JPA Interface**: `{Domain}JpaRepository extends JpaRepository<Entity, UUID>`
- **Implementation**: `{Domain}RepositoryImpl implements {Domain}Repository`

### API Response
- **성공**: `ApiResponseData.success(data)`
- **실패**: `ApiResponseData.failure(code, message)`

### Testing
- `@ActiveProfiles("test")` 어노테이션 사용
- H2 in-memory DB
- 설정: `src/test/resources/application-test.properties`

---

## UI/UX Framework

**⚠️ CRITICAL: 모든 새 템플릿은 [Topping CSS Framework](./docs/technical/css-framework.md) 사용**

- **Main Pages**: `base.css` + framework components
- **Auth Pages**: `auth.css` + framework components
- **JavaScript**: `common.js` 또는 `auth-common.js` (외부 CDN 금지)
- **Fragments**: navbar, footer, modals fragments 항상 사용
- **❌ FORBIDDEN**: Bootstrap CDN, inline styles, 외부 CSS 프레임워크
- **Mobile**: 768px 브레이크포인트, 햄버거 메뉴 — inline `onclick` + JS event listener 이중 처리 필수

---

## Domains

| Domain | Claude Guide | Key Concerns |
|---|---|---|
| User | [CLAUDE.md](./docs/domains/user/CLAUDE.md) | Session auth, RBAC (USER/BUSINESS_OWNER/ADMIN), getUuid() |
| Store | [CLAUDE.md](./docs/domains/store/CLAUDE.md) | 3단계 등록, multi-image upload, 소유권 검증 |
| Product | [CLAUDE.md](./docs/domains/product/CLAUDE.md) | `product.title` (not `name`), Cartesian Product, /products/create |
| Collaboration | [CLAUDE.md](./docs/domains/collaboration/CLAUDE.md) | Dual entity (Collaboration vs Proposal), 필드 참조, chat room 자동 생성 |
| Auth | [CLAUDE.md](./docs/domains/auth/CLAUDE.md) | Session config, Kakao OAuth, 라우트 권한 |
| Chat | [CLAUDE.md](./docs/domains/chat/CLAUDE.md) | WebSocket, unread badge, `data.data.uuid` 추출 패턴 |
| Notification | [CLAUDE.md](./docs/domains/notification/CLAUDE.md) | @Async, 30s polling, NotificationType enum |
| Support | [CLAUDE.md](./docs/domains/support/CLAUDE.md) | FAQ=public, 문의=authenticated, Admin RBAC |
| Policy | [CLAUDE.md](./docs/domains/policy/CLAUDE.md) | 이중 모달 시스템, signup 필수 동의 |

---

## Common Pitfalls

- **Session Auth**: `Principal` 또는 `UserDetailsImpl` 사용. JWT 토큰 사용 금지
- **Entity UUID**: `@GeneratedValue` 엔티티에 UUID 수동 설정 금지
- **Fragment Syntax**: `th:replace="~{fragments/navbar :: navbar}"` (Thymeleaf 3)
- **JPA JOIN FETCH**: 단일 쿼리에 다중 JOIN FETCH 금지 → Cartesian Product → 데이터 중복
- **Null-Safety**: `${object != null and !#strings.isEmpty(object.field)}`
- **Template JS**: 중첩 `th:if`/`th:each` in JavaScript 섹션 금지 → JSON injection 사용
- **Entity Persistence**: POST 엔드포인트에서 실제로 엔티티 저장 확인 (validation만으로 끝내지 말 것)
- **Entity Getter**: `getUuid()` 사용. `getUserId()`, `getRoomId()` 등은 존재하지 않음
- **Mobile Navbar**: inline `onclick` + JS event listener 이중 처리 (브라우저 호환성)
- **API Content-Type**: JSON 파싱 전 response content-type 확인 (`"Unexpected token '<'"` 오류 방지)
- **Collaboration Fields**: `collaboration.product` 필드 없음 → `partnerProduct`/`initiatorProduct` 조건 분기
- **Collaboration.message**: `description` 필드 사용 (`message` 필드 없음)

---

## Environment & Tooling Pitfalls

### Jetson Nano 빌드 환경

- **JAVA_HOME** (`gim` 사용자 기준) — PyCharm JBR 사용:
  ```bash
  export JAVA_HOME=/home/gim/Downloads/pycharm-community-2023.3.3-aarch64/pycharm-community-2023.3.3/jbr
  ```
  (`/usr/lib/jvm/java-17-openjdk-arm64`는 root 전용 — `gim` 사용자 접근 불가)

- **빌드 디렉토리 권한 충돌** — root로 `bootRun` 실행 중이면 `build/` 와 `.gradle/` 이 root 소유:
  ```bash
  ./gradlew compileJava --no-daemon \
    --project-cache-dir /tmp/gradle-cache-topping \
    -PbuildDir=/tmp/topping-build
  ```

- **Gradle lock 파일 stale** — 죽은 프로세스가 lock 보유 시:
  ```bash
  # gim 소유 lock만 삭제 가능
  find .gradle -name "*.lock" -user gim -delete
  # root 소유 lock → --project-cache-dir 옵션으로 우회
  ```

### 파일 편집 도구 주의사항

- **CRLF line endings** — Windows에서 편집된 파일은 Edit 도구가 `old_string` 찾지 못함:
  ```bash
  sed -i 's/\r//' <파일경로>   # LF 정규화 후 편집
  ```

- **탭 들여쓰기 파일** — Read 도구 출력의 스페이스 ≠ 실제 탭 → Edit 실패 시 Python 직접 치환:
  ```bash
  python3 -c "
  with open('File.java','r') as f: c=f.read()
  c=c.replace('OLD_STRING','NEW_STRING')
  with open('File.java','w') as f: f.write(c)
  "
  ```

### Repository 3계층 패턴 — 새 메서드 추가 규칙

새 쿼리/메서드 추가 시 **3곳 모두** 수정 필수 (누락 시 컴파일 오류 또는 런타임 오류):
1. `{Domain}JpaRepository` — JPQL/native 쿼리 정의
2. `{Domain}Repository` (domain interface) — 메서드 선언
3. `{Domain}RepositoryImpl` — jpaRepository 위임 구현

### @PreAuthorize + 혼합 컨트롤러 (페이지/API)

페이지 반환 메서드와 `@ResponseBody` API 메서드가 같은 컨트롤러에 혼재 시 `AccessDeniedHandler` 필요:
- `SecurityConfig.exceptionHandling()` 에 등록
- `/api/` 경로 또는 `Accept: application/json` → 403 JSON 반환
- 그 외 → `redirect:/mypage?error=access_denied`

### 사전 실패 테스트 (pre-existing failures)

내 변경과 무관하게 이미 실패 중인 테스트들:
- `StoreRegistrationTest` — `StoreViewService`, `StoreEngagementService`, `CollaborationService` @MockBean 누락
- `CollaborationControllerTest` — CSRF/인증 설정 불일치 (5개)
- `KakaoLoginControllerTest`, `KakaoLoginIntegrationTest` — Kakao OAuth 환경 없음

확인 방법: `git stash` → 테스트 실행 → `git stash pop`

---

## Technical Documentation

- [CSS Framework & UI System](./docs/technical/css-framework.md)
- [Multi-Image Upload System](./docs/technical/image-upload.md)
- [Collaboration Forms](./docs/technical/collaboration-forms.md)
- [Frontend Optimization](./docs/technical/frontend-optimization.md)
- [Database & Performance](./docs/technical/database-performance.md)
- [Common Issues](./docs/troubleshooting/common-issues.md)
- [Development Workflow](./docs/development-workflow.md)
- [Chat System Integration](./docs/technical/chat-system-integration.md)
- [Chat UI Enhancements](./docs/technical/chat-ui-enhancements.md)
