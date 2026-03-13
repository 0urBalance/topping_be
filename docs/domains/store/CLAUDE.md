# Store Domain - Claude Guidance

## Overview
사업자 가게 등록, 프로필, 다중 이미지 관리를 담당.

## Core Entities
- **Store** — UUID PK, name, description, owner(User), sggCode
- **StoreImage** — imagePath, originalFileName, displayOrder (int)
- **StoreLike** — 사용자 가게 즐겨찾기
- **SggCode** — 행정구역 코드

## Key Patterns

### 3단계 가게 등록 (아키텍처 제약)
1. **Phase 1**: 기본 정보 (이름, 설명, 위치) → Store 엔티티 생성 및 저장
2. **Phase 2**: 이미지 업로드 (multipart) → 별도 엔드포인트 처리
3. **Phase 3**: 최종 설정 및 활성화

> 이미지 업로드를 가게 생성과 분리한 이유: multipart parsing 오류 방지를 위한 아키텍처 결정

### Repository Pattern
- `StoreRepository` → `StoreRepositoryImpl implements StoreRepository`
- `StoreJpaRepository extends JpaRepository<Store, UUID>`

### 가게 소유권 검증
```java
// 수정 권한: owner 본인 또는 ADMIN만 허용
store.getOwner().equals(user) || user.getRole() == Role.ADMIN
```

### 이미지 업로드
- 직접 파일 처리 금지 → 반드시 `ImageUploadService` 사용
- 다중 이미지: `displayOrder` 필드로 순서 관리

### 상품 조회 시 Cartesian Product 방지
```java
// ❌ 단일 쿼리 다중 JOIN FETCH 금지
// ✅ 별도 쿼리 사용
Store store = storeRepository.findById(storeId);
List<Product> products = productRepository.findByStoreOrderByCreatedAtDesc(store);
```

## Common Pitfalls
- ❌ **직접 이미지 업로드**: `ImageUploadService` 경유 필수
- ❌ **소유권 검증 누락**: 수정 전 반드시 store ownership 확인
- ❌ **Cartesian Product**: 단일 쿼리에 다중 JOIN FETCH 금지
- ❌ **UUID 수동 설정**: `@GeneratedValue` 엔티티에 UUID 수동 지정 금지
- ❌ **Null Safety**: `store.getStoreImages()` null 체크 후 순회

## Integration Points
- **User Domain**: 가게 소유권, BUSINESS_OWNER 역할
- **Product Domain**: 상품은 가게에 속함
- **Collaboration Domain**: 가게 기반 콜라보 제안
- **Image Upload**: 다중 이미지 관리 시스템

## Related Documentation
- [Main Claude Guidance](../../../CLAUDE.md)
- [Multi-Image Upload System](../../technical/image-upload.md)
- [Three-Phase Registration Solution](../../troubleshooting/THREE_PHASE_REGISTRATION_SOLUTION.md)
- [Product Domain](../product/CLAUDE.md)
