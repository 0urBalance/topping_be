# TOPPING CSS FRAMEWORK & UI SYSTEM

## 🏗️ **MANDATORY: CSS Framework Architecture**

**⚠️ CRITICAL: When implementing ANY new page, you MUST use the established CSS framework and layout system.**

### **CSS Framework Structure**
```
static/css/
├── base.css      # 🎯 CORE FRAMEWORK - CSS variables, components, utilities
├── auth.css      # 🔐 Authentication pages styling
├── main.css      # 📄 Main layout with sidebar for authenticated users
└── navbar.css    # 🧭 Navigation and sidebar styles
```

### **Template Layout System**
```
templates/
├── layout/
│   ├── base.html           # 🌐 Universal layout (navbar + footer)
│   ├── auth-layout.html    # 🔐 Clean auth pages (login/signup)
│   └── main-layout.html    # 👤 Authenticated user pages
├── fragments/
│   ├── navbar.html         # 🧭 Responsive navigation sidebar
│   ├── footer.html         # 🦶 Footer with modal integration
│   └── modals.html         # 🪟 Reusable modal components
```

## 🎨 **Design System Variables**

### **CSS Variables (USE THESE)**
```css
/* Brand Colors */
--primary-color: #ff6b35;
--primary-hover: #e55a2b;
--primary-light: #fff5f2;

/* Status Colors */
--success-color: #059669;
--error-color: #dc2626;
--warning-color: #856404;
--info-color: #2563eb;

/* Spacing System */
--spacing-xs: 0.25rem;  --spacing-sm: 0.5rem;   --spacing-md: 1rem;
--spacing-lg: 1.5rem;   --spacing-xl: 2rem;     --spacing-2xl: 3rem;

/* Layout Variables */
--sidebar-width: 240px;
--header-height: 60px;
```

## 🧩 **Component System (ALWAYS USE)**

### **Button Classes**
```html
<!-- Primary Actions -->
<button class="btn btn-primary">확인</button>
<button class="btn btn-secondary">취소</button>
<button class="btn btn-outline">더보기</button>

<!-- Sizes -->
<button class="btn btn-primary btn-sm">작은 버튼</button>
<button class="btn btn-primary btn-lg">큰 버튼</button>
<button class="btn btn-primary btn-block">전체 너비</button>
```

### **Form Components**
```html
<div class="form-group">
    <label class="form-label">이메일</label>
    <input type="email" class="form-control" placeholder="이메일을 입력하세요">
    <div class="invalid-feedback">올바른 이메일을 입력해주세요</div>
</div>
```

### **Card Components**
```html
<div class="card">
    <div class="card-header">
        <h3 class="card-title">제목</h3>
    </div>
    <div class="card-body">
        내용
    </div>
    <div class="card-footer">
        <button class="btn btn-primary">액션</button>
    </div>
</div>
```

### **Alert Messages**
```html
<div class="alert alert-success">성공 메시지</div>
<div class="alert alert-error">오류 메시지</div>
<div class="alert alert-warning">경고 메시지</div>
<div class="alert alert-info">정보 메시지</div>
```

## 📱 **Responsive Layout Requirements**

### **Template Structure (MANDATORY)**
```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>페이지 제목 - Topping</title>
    
    <!-- MANDATORY: Include framework CSS -->
    <link rel="stylesheet" th:href="@{/css/base.css}">
    <!-- For auth pages: th:href="@{/css/auth.css}" -->
    <!-- For main pages: th:href="@{/css/main.css}" and th:href="@{/css/navbar.css}" -->
    
    <style>
        /* Page-specific styles only - minimal overrides */
    </style>
</head>
<body class="main-body"> <!-- Use auth-body for auth pages -->
    
    <!-- MANDATORY: Include navbar for main pages -->
    <div th:replace="~{fragments/navbar :: navbar}"></div>
    
    <!-- MANDATORY: Main content with proper layout -->
    <main id="main-content" class="main-content">
        <!-- Your page content here -->
    </main>
    
    <!-- MANDATORY: Include footer -->
    <div th:replace="~{fragments/footer :: footer}"></div>
    
    <!-- MANDATORY: Include common modals -->
    <div th:replace="~{fragments/modals :: policy-modals}"></div>
    
    <!-- MANDATORY: Include framework JavaScript -->
    <script th:src="@{/js/common.js}"></script>
    <!-- For main pages: th:src="@{/js/main-common.js}" -->
    <!-- For auth pages: th:src="@{/js/auth-common.js}" -->
</body>
</html>
```

## 🎯 **JavaScript Framework (MANDATORY)**

### **Available Global Functions**
```javascript
// Modal Management
window.Topping.showConfirmation({
    title: '확인',
    message: '정말 삭제하시겠습니까?',
    onConfirm: () => deleteItem()
});

// Notifications
window.Topping.notify.success('저장되었습니다');
window.Topping.notify.error('오류가 발생했습니다');

// API Helpers
const data = await window.Topping.apiGet('/api/data');
await window.Topping.apiPost('/api/save', formData);

// Utilities
const isValid = window.Topping.isValidEmail(email);
window.Topping.copyToClipboard(text);
```

## 🚨 **IMPLEMENTATION RULES**

### **❌ NEVER DO:**
- Create pages without using the CSS framework
- Duplicate CSS styles that exist in base.css
- Use inline styles for layout or components
- Hardcode colors, spacing, or breakpoints
- Create custom modal implementations
- Use external CSS frameworks (Bootstrap, etc.) except for specific components

### **✅ ALWAYS DO:**
- Start with appropriate layout template
- Use CSS variables for colors and spacing
- Include proper navbar and footer fragments
- Use framework components (buttons, forms, cards)
- Include framework JavaScript files
- Follow responsive design patterns
- Use semantic HTML with accessibility features

## 📏 **Grid System & Layout**

### **Responsive Breakpoints**
```css
/* Mobile: < 768px */
@media (max-width: 767.98px) { 
    .main-content { margin-left: 0; } /* No sidebar */
}

/* Tablet: 768px - 1024px */
@media (min-width: 768px) and (max-width: 1023.98px) { 
    /* Compressed sidebar */
}

/* Desktop: > 1024px */
@media (min-width: 1024px) { 
    /* Full sidebar layout */
}
```

### **Grid Classes**
```html
<div class="grid grid-cols-1">Single column</div>
<div class="grid grid-cols-2">Two columns</div>
<div class="grid grid-cols-3">Three columns</div>
<div class="grid grid-cols-4">Four columns</div>

<!-- Auto-responsive grid -->
<div class="card-grid">
    <div class="feature-card">Auto-sized cards</div>
</div>
```

## 🎨 **Brand Identity Guidelines**

### **Color Usage**
- **Primary (`--primary-color`)**: Main actions, links, focus states
- **Success (`--success-color`)**: Confirmations, success messages
- **Error (`--error-color`)**: Errors, destructive actions
- **Warning (`--warning-color`)**: Warnings, cautions
- **Info (`--info-color`)**: Information, help text

### **Typography Hierarchy**
```html
<h1>Main page title (2.25rem)</h1>
<h2>Section heading (1.875rem)</h2>
<h3>Subsection (1.5rem)</h3>
<h4>Component title (1.25rem)</h4>
<p>Body text (1rem)</p>
<small>Helper text (0.875rem)</small>
```

## 🔧 **Development Workflow**

### ⚠️ **CRITICAL: Framework Compliance Required**
**ALL new templates and pages MUST use the Topping CSS Framework. Current compliance rate is only ~15%.**

**❌ FORBIDDEN PATTERNS:**
- Bootstrap CDN (`https://cdn.jsdelivr.net/npm/bootstrap@...`)
- Multiple CSS files (`navbar.css`, `main.css`, external frameworks)
- Inline styles (except minimal page-specific overrides)
- External JavaScript CDNs (FontAwesome, etc.)
- Mixed layout approaches (stick to `base.css` or `auth.css`)

**✅ REQUIRED PATTERNS:**
```html
<!-- CSS: Choose ONE framework -->
<link rel="stylesheet" th:href="@{/css/base.css}">      <!-- Main pages -->
<link rel="stylesheet" th:href="@{/css/auth.css}">     <!-- Auth pages -->

<!-- JavaScript: Use framework scripts -->
<script th:src="@{/js/common.js}"></script>            <!-- Main pages -->
<script th:src="@{/js/auth-common.js}"></script>       <!-- Auth pages -->

<!-- Fragments: Always use -->
<div th:replace="~{fragments/navbar :: navbar}"></div>
<div th:replace="~{fragments/footer :: footer}"></div>
<div th:replace="~{fragments/modals :: policy-modals}"></div>
```

### **For New Pages:**
1. ✅ Choose appropriate layout template (`base.css` OR `auth.css`)
2. ✅ Include ONLY framework CSS and JS (no Bootstrap/external CDNs)
3. ✅ Use framework components and CSS Variables
4. ✅ Test responsive design
5. ✅ Validate accessibility
6. ✅ Check modal integration

## Template Best Practices

- **MANDATORY CSS**: Use `base.css` framework for main templates, `auth.css` for authentication
- **NO BOOTSTRAP**: Never use Bootstrap CDN or external CSS frameworks
- **NO INLINE STYLES**: All styling must use CSS framework classes and CSS Variables
- **JavaScript**: Use `common.js` or `auth-common.js` - no inline scripts or external CDNs
- **Fragments**: Always use `th:replace="~{fragments/navbar :: navbar}"` for consistent layout
- Always use `th:href="@{/path}"` for internal links
- Check user roles with `sec:authorize="hasRole('ROLE_NAME')"`
- Handle null checks with `th:if="${object != null}"`
- Use consistent variable naming (avoid 'application' - reserved word)
- CSRF protection is disabled - no CSRF tokens needed in forms

## Modal System Guidelines

- **Policy Modals**: Use dynamic content loading with caching (`/policy/privacy-modal`, `/policy/terms-modal`)
- **Modal Structure**: Header with title and close button, body with scrollable content
- **JavaScript Integration**: Handle modal open/close, dynamic content loading, outside-click closing
- **Signup Integration**: Required agreement checkbox validation before form submission
- **Footer Integration**: Consistent modal experience across all pages with footer links
- **Accessibility**: ESC key support, focus management, proper ARIA attributes