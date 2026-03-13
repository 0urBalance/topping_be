# Policy Domain - Claude Guidance

## Overview
개인정보 처리방침과 이용약관 페이지, 모달 통합, 회원가입 동의 검증을 담당.

## Key Patterns

### 라우트
```
GET /policy/privacy-policy    → 개인정보 처리방침 페이지
GET /policy/terms-of-service  → 이용약관 페이지
```

### 이중 모달 시스템 (핵심 — 비자명한 설계)

**두 종류의 정책 모달이 공존**:

1. **`modals.html :: policy-modals`** — `termsModal` + `privacyModal`
   - 대부분의 페이지에서 포함
   - `common.js`에서 초기화

2. **`footer.html` 자체 모달** — `footerTermsModal` + `footerPrivacyModal`
   - footer fragment 자체에 포함된 독립 모달
   - `policy-modals` fragment를 포함하지 않는 페이지에서 동작

> 두 시스템이 공존하는 이유: footer는 모든 페이지에 포함되므로, policy-modals fragment 없이도 푸터에서 정책 링크가 동작하도록 설계

### 정책 페이지 템플릿 구조
```html
<div th:replace="~{fragments/navbar :: navbar}"></div>
<main class="policy-content">
    <div class="policy-container">
        <!-- 정책 내용 -->
    </div>
</main>
<div th:replace="~{fragments/footer :: footer}"></div>
<div th:replace="~{fragments/modals :: modals}"></div>
```

CSS: `docs/technical/css-framework.md` 참조

### 회원가입 필수 동의 검증
- `termsAgreement` (필수), `privacyAgreement` (필수), `marketingAgreement` (선택)
- 두 필수 체크박스 미선택 시 submit 버튼 비활성화
- 체크박스 클릭 → 모달로 정책 내용 확인 가능

## Common Pitfalls
- ❌ **모달 fragment 누락**: 정책 템플릿에 반드시 modals fragment 포함
- ❌ **하드코딩 링크**: 정책 링크에 Thymeleaf URL 표현식 사용
- ❌ **검증 누락**: 회원가입 폼에 동의 체크박스 검증 필수
- ❌ **모바일 스크롤**: 모달 내용이 소형 화면에서 스크롤 가능해야 함
- ❌ **접근성 누락**: 모달에 ARIA 레이블 및 키보드 내비게이션 포함 필요

## Integration Points
- **Auth Domain**: 회원가입 동의 검증
- **Footer**: 모든 페이지에서 정책 접근
- **Modal System**: 일관된 모달 경험

## Related Documentation
- [Main Claude Guidance](../../../CLAUDE.md)
- [Auth Domain](../auth/CLAUDE.md)
- [CSS Framework](../../technical/css-framework.md)
