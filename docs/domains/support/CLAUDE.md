# Support Domain - Claude Guidance

## Overview
FAQ 관리와 문의 제출 시스템을 담당. FAQ는 공개 접근, 문의 제출은 로그인 필수.

## Core Entities

### FAQ 필드
- `uuid`, `question`, `answer`, `category`(FAQCategory), `displayOrder`, `isActive`, `createdAt`

### SupportInquiry 필드
- `uuid`, `inquirer`(User), `title`, `content`, `email`(연락처), `category`, `status`, `adminResponse`, `respondedAt`

### Enum 값

**FAQCategory**: `GENERAL`, `ACCOUNT`, `COLLABORATION`, `PAYMENT`, `TECHNICAL`

**InquiryCategory**: `GENERAL`, `TECHNICAL`, `BILLING`, `COLLABORATION`, `ACCOUNT`, `BUG_REPORT`

**InquiryStatus**: `PENDING` → `IN_PROGRESS` → `COMPLETED` → `CLOSED`

## Key Patterns

### 접근 제어 규칙 (핵심)
- **FAQ 조회** (`/support/cs`, `/support/faq/**`): **인증 불필요 (public)**
- **문의 제출/조회** (`/support/inquiry*`, `/support/my-inquiries`): **로그인 필수**
- **관리자 기능** (`/admin/support/**`): **ADMIN 역할 필수**

### 라우트 테이블
```
GET  /support/cs                  → FAQ 목록 (public, category 파라미터 선택)
GET  /support/faq/{faqId}         → FAQ 상세 (public)
GET  /support/inquiry-form        → 문의 폼 (authenticated)
POST /support/inquiry             → 문의 제출 (authenticated)
GET  /support/my-inquiries        → 내 문의 목록 (authenticated)
GET  /admin/support/faqs          → FAQ 관리 (ADMIN)
GET  /admin/support/inquiries     → 문의 관리 (ADMIN, status 파라미터)
```

### Repository Pattern
- `FAQRepository`, `FAQJpaRepository`, `FAQRepositoryImpl`
- `SupportInquiryRepository`, `SupportInquiryJpaRepository`, `SupportInquiryRepositoryImpl`

### 문의 제출 후 알림
- 새 문의 제출 시 → 관리자 알림 생성
- 답변 완료 시 → 문의자 알림 생성

## Common Pitfalls
- ❌ **인증 혼동**: FAQ는 public, 문의 제출은 authenticated (헷갈리기 쉬운 부분)
- ❌ **Admin 권한 누락**: 관리 기능에 반드시 ADMIN 역할 확인
- ❌ **이메일 검증 누락**: 문의 폼의 연락처 이메일 형식 검증 필수
- ❌ **상태 관리**: 답변 시 InquiryStatus 올바르게 업데이트 필요
- ❌ **XSS 방어**: 문의 내용 사용자 입력에 XSS 방지 처리 필수

## Integration Points
- **User Domain**: 문의 제출 인증
- **Notification Domain**: 새 문의/답변 알림
- **Admin**: 관리자 FAQ 및 문의 관리 인터페이스

## Related Documentation
- [Main Claude Guidance](../../../CLAUDE.md)
- [User Domain](../user/CLAUDE.md)
- [Notification Domain](../notification/CLAUDE.md)
