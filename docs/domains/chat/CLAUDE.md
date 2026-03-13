# Chat Domain - Claude Guidance

## Overview
수락된 콜라보에 대한 실시간 채팅, 자동 채팅방 생성, WebSocket 통합을 담당.

## Core Entities

### ChatRoom
- `uuid`, `name`, `collaborationUuid` (Collaboration 또는 CollaborationProposal UUID 연결)
- 참가자는 collaboration 엔티티에서 동적으로 조회 (`@Transient`)

### ChatMessage
- `uuid`, `chatRoom`, `sender`(User), `content`, `sentAt`
- **`isRead`** (boolean, default false) — 읽음 상태
- **`readAt`** (LocalDateTime) — 읽은 시각

## Key Patterns

### 자동 채팅방 생성
콜라보 수락 시 자동 생성. 중복 생성 방지:
```java
// ChatService
Optional<ChatRoom> existingRoom = chatRoomRepository.findByCollaborationUuid(collaboration.getUuid());
if (existingRoom.isPresent()) return existingRoom.get();
// 없으면 새로 생성
```

### WebSocket (SockJS + STOMP)
- 구독: `/topic/chat/{roomId}`
- 발신: `/app/chat/message`
- 메시지 저장 후 즉시 `SimpMessagingTemplate`으로 브로드캐스트 필수

### 메시지 정렬 (내 메시지 vs 상대방)

**⚠️ 핵심 패턴**: `/api/session/user` 응답은 `ApiResponseData` 래퍼 구조이므로 UUID 추출 시 주의:

```javascript
fetch('/api/session/user')
    .then(res => res.json())
    .then(data => {
        // ⚠️ data.data.uuid 사용 (data.uuid 아님)
        const currentUserUuid = data.data.uuid;

        bubble.className = (message.sender.uuid === currentUserUuid)
            ? 'bubble mine'    // 오른쪽, 갈색 (#6B3410)
            : 'bubble their';  // 왼쪽, 연회색
    });
```

### 읽음 처리
- 채팅방 접근 시 자동으로 해당 방 메시지를 읽음 처리
- 미읽음 뱃지: `.unread-badge` 클래스, `.hidden` 클래스로 숨김 (`display: none` 직접 사용 금지)

### 타임스탬프 포맷
Korean locale: `date.toLocaleTimeString('ko-KR', { hour12: true })` → `오전/오후 HH:mm`

## Common Pitfalls
- ❌ **채팅방 미생성**: 콜라보 수락 시 반드시 채팅방 자동 생성 호출
- ❌ **WebSocket 라이브러리 순서**: SockJS → STOMP → 커스텀 JS 순서로 로드
- ❌ **UUID 추출**: `data.uuid` 아님 → `data.data.uuid` (`ApiResponseData` 래퍼)
- ❌ **읽음 처리 누락**: 채팅방 접근 시 `markMessagesAsRead()` 호출 필수
- ❌ **뱃지 숨김**: `display: none` 직접 사용 금지 → `.hidden` 클래스 사용
- ❌ **즉시 표시 누락**: 메시지 DB 저장 후 WebSocket 브로드캐스트 필수

## Integration Points
- **Collaboration Domain**: 수락된 콜라보에 채팅방 자동 연결
- **User Domain**: 세션 기반 사용자 식별
- **WebSocket**: SockJS/STOMP 실시간 브로드캐스트
- **MyPage**: 콜라보 관리에서 채팅방 접근

## Related Documentation
- [Main Claude Guidance](../../../CLAUDE.md)
- [Collaboration Domain](../collaboration/CLAUDE.md)
- [Chat System Integration](../../technical/chat-system-integration.md)
- [Chat UI Enhancements](../../technical/chat-ui-enhancements.md)
- [Chat Real-time Messaging](../../technical/chat-real-time-messaging.md)
