# Notification Domain - Claude Guidance

## Overview
콜라보 업데이트, 채팅 메시지, 시스템 이벤트에 대한 이벤트 기반 알림을 담당.

## Core Entity

### Notification 필드
- `uuid`, `recipient`(User), `title`, `message`, `type`(NotificationType)
- `relatedEntityId` (String, 관련 엔티티 UUID)
- `isRead` (boolean, default false), `readAt`, `createdAt`

### NotificationType
```java
COLLABORATION_PROPOSAL   // 새 콜라보 제안 수신
COLLABORATION_ACCEPTED   // 내 제안 수락됨
COLLABORATION_REJECTED   // 내 제안 거절됨
CHAT_MESSAGE             // 새 채팅 메시지
STORE_LIKE               // 가게 좋아요
PRODUCT_REVIEW           // 상품 리뷰
SYSTEM_ANNOUNCEMENT      // 시스템 공지
```

## Key Patterns

### @Async 필수 (메인 흐름 블로킹 방지)
```java
@Async
public void createCollaborationProposalNotification(CollaborationProposal proposal) { ... }

@Async
public void createChatMessageNotification(ChatMessage message) { ... }
```
Thread pool: core=2, max=5, queue=100 (`database-performance.md` 참조)

### 미읽음 수 폴링
- 30초 간격 `setInterval`로 `/api/notifications/unread-count` 폴링
- 뱃지 표시: `id="unreadCount"`, count > 99이면 "99+" 표시

### API 엔드포인트
```
GET  /api/notifications              → 알림 목록 (limit 파라미터)
GET  /api/notifications/unread-count → 미읽음 수
POST /api/notifications/{id}/read    → 읽음 처리
```

### 알림 클릭 시 네비게이션
- `COLLABORATION_PROPOSAL` → `/mypage/received`
- `CHAT_MESSAGE` → `/chat/rooms?room={relatedEntityId}`
- `STORE_LIKE` → `/mypage/store`

### 읽음 처리 시 소유권 검증 필수
```java
if (!notification.getRecipient().equals(user)) {
    throw new AccessDeniedException("알림에 접근할 권한이 없습니다");
}
```

## Common Pitfalls
- ❌ **블로킹 알림 생성**: 반드시 `@Async` 사용 (메인 요청 흐름 블로킹 방지)
- ❌ **소유권 검증 누락**: 읽음 처리 전 반드시 `recipient` 검증
- ❌ **폴링 과부하**: 30초 간격 유지 (더 짧은 간격 금지)
- ❌ **채팅 알림 스팸**: 채팅 메시지 알림에 rate limiting 적용 필요
- ❌ **오래된 알림 미정리**: 읽은 오래된 알림 정기 정리 구현 필요

## Integration Points
- **Collaboration Domain**: 제안/수락 알림
- **Chat Domain**: 새 메시지 알림
- **Store Domain**: 좋아요/리뷰 알림
- **User Domain**: 사용자별 알림 타겟팅

## Related Documentation
- [Main Claude Guidance](../../../CLAUDE.md)
- [Collaboration Domain](../collaboration/CLAUDE.md)
- [Chat Domain](../chat/CLAUDE.md)
- [Database & Performance](../../technical/database-performance.md)
