# Product Domain - Claude Guidance

## Overview
상품 등록, 수정, 다중 이미지 관리, 콜라보 연동을 담당.

## Core Entities
- **Product** — UUID PK, `title`(⚠️ `name` 아님), description, store, type(COLLABORATION/INDIVIDUAL)
- **ProductImage** — imagePath, originalFileName, displayOrder (int)
- **Wishlist** — 사용자 상품 즐겨찾기
- **Review** — 상품 리뷰/평점

## Key Patterns

### 라우트 규칙
```
/products/create      ← 상품 생성 폼 (⚠️ /products/register 아님)
/products             ← 상품 생성 POST
/products/{id}/edit   ← 상품 수정 폼
/products/{id}        ← 상품 수정 PUT
```

### 상품 생성 전 가게 보유 확인 (필수)
```java
Store userStore = storeService.findByOwner(userDetails.getUser());
if (userStore == null) {
    return "redirect:/stores/register";
}
```

### Repository Pattern
- `ProductRepository` → `ProductRepositoryImpl implements ProductRepository`
- `ProductJpaRepository extends JpaRepository<Product, UUID>`

### Template 필드 참조
```html
<!-- ✅ 올바른 참조 -->
<h1 th:text="${product.title}">제목</h1>

<!-- ❌ 잘못된 참조 - 필드 없음 -->
<!-- th:text="${product.name}" -->
```

### Cartesian Product 방지
```java
// ❌ 잘못됨 - 다중 JOIN FETCH
@Query("SELECT p FROM Product p LEFT JOIN FETCH p.productImages LEFT JOIN FETCH p.reviews")

// ✅ 올바름 - 단일 JOIN FETCH 또는 별도 쿼리
@Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.productImages WHERE p.store = :store")
```

### Null Safety (이미지 접근 시)
```html
th:if="${product.productImages != null and !product.productImages.empty}"
```

## Common Pitfalls
- ❌ **필드명**: `product.title` 사용. `product.name` 필드 없음
- ❌ **라우트**: `/products/create` 사용. `/products/register` 아님
- ❌ **가게 검증 누락**: 상품 생성 전 반드시 owner의 가게 존재 확인
- ❌ **이미지 Null**: `productImages != null and !productImages.empty` 체크 필수
- ❌ **Cartesian Product**: 단일 쿼리에 다중 JOIN FETCH 금지

## Integration Points
- **Store Domain**: 상품은 가게에 속함, BUSINESS_OWNER 역할 필요
- **Collaboration Domain**: 상품이 콜라보 제안의 `initiatorProduct`/`partnerProduct`
- **Image Upload**: 다중 이미지 관리 시스템

## Related Documentation
- [Main Claude Guidance](../../../CLAUDE.md)
- [Store Domain](../store/CLAUDE.md)
- [Collaboration Domain](../collaboration/CLAUDE.md)
- [Multi-Image Upload System](../../technical/image-upload.md)
