# Collaboration Domain - Claude Guidance

## Overview
사업자 간 협업 매칭, 파트너십 제안, 콜라보 프로젝트 관리를 담당. **두 개의 별도 엔티티**로 구성.

## Core Entities

### 이중 엔티티 구조 (핵심)
- **`Collaboration`** — **수락된** 콜라보. 채팅방이 연결됨
- **`CollaborationProposal`** — **대기 중인** 제안. 수락 시 `Collaboration`으로 전환
- **`ProposalSource`** — `GUEST_TO_BUSINESS` | `BUSINESS_TO_BUSINESS`

### 필드 참조 매핑 (자주 틀리는 부분)

| 잘못된 참조 | 올바른 참조 |
|---|---|
| `collaboration.product` | 없음 → 아래 조건 분기 사용 |
| `collaboration.message` | `collaboration.description` |
| `collaboration.applicantProduct` | `collaboration.initiatorProduct` |
| `application.product` | 조건 분기로 `initiatorProduct`/`partnerProduct` 선택 |

```html
<!-- ✅ 올바른 상품 표시 패턴 -->
<div th:if="${collaboration.partnerProduct != null}">
    <span th:text="${collaboration.partnerProduct.title}">파트너 상품</span>
</div>
<div th:unless="${collaboration.partnerProduct != null}">
    <span th:text="${collaboration.initiatorProduct.title}">제안자 상품</span>
</div>
```

### 공통 필드 (Collaboration & CollaborationProposal)
- `uuid`, `title`, `description`
- `initiatorProduct` (ManyToOne, 서비스형 콜라보는 null 가능)
- `partnerProduct` (ManyToOne, null 가능)
- `initiatorStore`, `partnerStore`, `initiator`, `partner`

## Key Patterns

### 제안 수락 흐름
1. `CollaborationProposal` 조회
2. `Collaboration` 생성 (수락된 콜라보)
3. **채팅방 자동 생성** (`chatService.createChatRoomForCollaboration(collaboration)`)
4. `CollaborationProposal` 삭제
5. `/mypage/ongoing` 리다이렉트

### Repository Pattern
- `CollaborationRepository` — 수락된 콜라보
- `CollaborationProposalRepository` — 대기 중인 제안
- 각각 3-layer pattern 적용

## Common Pitfalls
- ❌ **`collaboration.product`**: 이 필드 없음 → `partnerProduct`/`initiatorProduct` 조건 분기 사용
- ❌ **`collaboration.message`**: `description` 필드 사용
- ❌ **`applicantProduct`**: `initiatorProduct` 사용
- ❌ **채팅방 조기 생성**: 제안 수락 시에만 채팅방 생성 (제안 단계에서 금지)
- ❌ **엔티티 혼동**: `Collaboration`(수락) vs `CollaborationProposal`(대기) 구분 필수
- ❌ **Null Safety**: 서비스형 콜라보는 `partnerProduct == null` 가능

## Integration Points
- **Chat Domain**: 수락된 콜라보에 채팅방 자동 생성
- **Product Domain**: `initiatorProduct`/`partnerProduct` 참조
- **Store Domain**: 가게 간 비즈니스 파트너십
- **User Domain**: 사용자 역할 및 소유권 검증

## Related Documentation
- [Main Claude Guidance](../../../CLAUDE.md)
- [Chat Domain](../chat/CLAUDE.md)
- [Collaboration Received Page](../../technical/COLLABORATION_RECEIVED_PAGE.md)
- [Collaboration Forms](../../technical/collaboration-forms.md)
