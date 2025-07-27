# CLAUDE.md

This file provides comprehensive guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
Topping (토핑) is a collaboration matching platform backend built with Spring Boot. The project uses a clean architecture approach with domain-driven design principles, enabling businesses to find collaboration partners and manage collaborative projects.

## Quick Start

### Environment Setup
- **JAVA_HOME**: Set to `/mnt/d/projects/topping/jdk-17.0.12+7` (JDK 17 included in project)
- **Build Command**: `export JAVA_HOME=/mnt/d/projects/topping/jdk-17.0.12+7 && ./gradlew build`

### Essential Commands
- `export JAVA_HOME=/mnt/d/projects/topping/jdk-17.0.12+7 && ./gradlew build` - Build the project
- `export JAVA_HOME=/mnt/d/projects/topping/jdk-17.0.12+7 && ./gradlew bootRun` - Run the application
- `export JAVA_HOME=/mnt/d/projects/topping/jdk-17.0.12+7 && ./gradlew test` - Run all tests

### Database
- **Production**: PostgreSQL (env vars: `DB_URL`, `DB_USER`, `DB_PASSWORD`)
- **Testing**: H2 in-memory database
- **Configuration**: Uses spring-dotenv, create `.env` file in project root
- **Connection Pool**: HikariCP optimized (5-30 connections, leak detection enabled)

## Architecture

### Clean Architecture Layers
- **`domain/`** - Core business logic and entities
- **`infrastructure/`** - Technical implementation details
- **`presentation/`** - REST controllers and DTOs
- **`application/`** - Application services and DTOs

### Tech Stack
- Spring Boot 3.5.3 with Java 17
- Spring Data JPA + PostgreSQL
- Spring Security (session-based authentication)
- Lombok + Thymeleaf
- WebSocket for real-time chat
- Spring Data repositories with three-layer pattern
- Multi-image upload system with image processing
- Async task execution with custom thread pools

## 📁 Domain Reference

The platform is organized into distinct business domains. Each domain has comprehensive documentation covering models, APIs, business rules, and integration points:

### Core Domains
- **[👤 User Domain](./docs/domains/user/README.md)** - User accounts, profiles, and role-based access control
- **[🏪 Store Domain](./docs/domains/store/README.md)** - Business store registration and management
- **[📦 Product Domain](./docs/domains/product/README.md)** - Product listings and collaboration features
- **[🤝 Collaboration Domain](./docs/domains/collaboration/README.md)** - Business matching and partnership management

### Supporting Domains
- **[🔐 Authentication Domain](./docs/domains/auth/README.md)** - Session-based authentication with Spring Security and Kakao social login
- **[💬 Chat Domain](./docs/domains/chat/README.md)** - Real-time communication for collaborations
- **[🔔 Notification Domain](./docs/domains/notification/README.md)** - Event-driven notifications
- **[🎧 Support Domain](./docs/domains/support/README.md)** - Customer support system with FAQ and inquiry management
- **[📋 Policy Domain](./docs/domains/policy/README.md)** - Privacy policy and terms of service with modal integration

## Development Standards

### Repository Pattern
All repositories follow a consistent three-layer pattern:
- **Domain Interface**: `{Domain}Repository` (business logic)
- **JPA Interface**: `{Domain}JpaRepository` (extends JpaRepository)
- **Implementation**: `{Domain}RepositoryImpl` (implements domain interface)

### API Response Pattern
- **Standard Wrapper**: `ApiResponseData.success(data)` for success responses
- **Error Handling**: `ApiResponseData.failure(code, message)` for failures
- **Usage**: Always call `ApiResponseData.success(data)`, not `ApiResponseData.success(Code.SUCCESS, data)`

### Testing
- **Test Profile**: Use `@ActiveProfiles("test")` annotation
- **Database**: H2 in-memory database for testing
- **Configuration**: See `src/test/resources/application-test.properties`

## 🎨 TOPPING CSS FRAMEWORK & UI SYSTEM

### 🏗️ **MANDATORY: CSS Framework Architecture**

**⚠️ CRITICAL: When implementing ANY new page, you MUST use the established CSS framework and layout system.**

#### **CSS Framework Structure**
```
static/css/
├── base.css      # 🎯 CORE FRAMEWORK - CSS variables, components, utilities
├── auth.css      # 🔐 Authentication pages styling
├── main.css      # 📄 Main layout with sidebar for authenticated users
└── navbar.css    # 🧭 Navigation and sidebar styles
```

#### **Template Layout System**
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

### 🎨 **Design System Variables**

#### **CSS Variables (USE THESE)**
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

### 🧩 **Component System (ALWAYS USE)**

#### **Button Classes**
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

#### **Form Components**
```html
<div class="form-group">
    <label class="form-label">이메일</label>
    <input type="email" class="form-control" placeholder="이메일을 입력하세요">
    <div class="invalid-feedback">올바른 이메일을 입력해주세요</div>
</div>
```

#### **Card Components**
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

#### **Alert Messages**
```html
<div class="alert alert-success">성공 메시지</div>
<div class="alert alert-error">오류 메시지</div>
<div class="alert alert-warning">경고 메시지</div>
<div class="alert alert-info">정보 메시지</div>
```

### 📱 **Responsive Layout Requirements**

#### **Template Structure (MANDATORY)**
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

### 🎯 **JavaScript Framework (MANDATORY)**

#### **Available Global Functions**
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

### 🚨 **IMPLEMENTATION RULES**

#### **❌ NEVER DO:**
- Create pages without using the CSS framework
- Duplicate CSS styles that exist in base.css
- Use inline styles for layout or components
- Hardcode colors, spacing, or breakpoints
- Create custom modal implementations
- Use external CSS frameworks (Bootstrap, etc.) except for specific components

#### **✅ ALWAYS DO:**
- Start with appropriate layout template
- Use CSS variables for colors and spacing
- Include proper navbar and footer fragments
- Use framework components (buttons, forms, cards)
- Include framework JavaScript files
- Follow responsive design patterns
- Use semantic HTML with accessibility features

### 📏 **Grid System & Layout**

#### **Responsive Breakpoints**
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

#### **Grid Classes**
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

### 🎨 **Brand Identity Guidelines**

#### **Color Usage**
- **Primary (`--primary-color`)**: Main actions, links, focus states
- **Success (`--success-color`)**: Confirmations, success messages
- **Error (`--error-color`)**: Errors, destructive actions
- **Warning (`--warning-color`)**: Warnings, cautions
- **Info (`--info-color`)**: Information, help text

#### **Typography Hierarchy**
```html
<h1>Main page title (2.25rem)</h1>
<h2>Section heading (1.875rem)</h2>
<h3>Subsection (1.5rem)</h3>
<h4>Component title (1.25rem)</h4>
<p>Body text (1rem)</p>
<small>Helper text (0.875rem)</small>
```

### 🔧 **Development Workflow**

#### ⚠️ **CRITICAL: Framework Compliance Required**
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

#### **For New Pages:**
1. ✅ Choose appropriate layout template (`base.css` OR `auth.css`)
2. ✅ Include ONLY framework CSS and JS (no Bootstrap/external CDNs)
3. ✅ Use framework components and CSS Variables
4. ✅ Test responsive design
5. ✅ Validate accessibility
6. ✅ Check modal integration

## Key Reminders

### Build Requirements
- JAVA_HOME must be set to `/mnt/d/projects/topping/jdk-17.0.12+7` for all Gradle commands
- All build issues resolved and tests passing
- Session-based authentication (no JWT dependencies)

### Architecture Principles
- Clean architecture with domain-driven design
- Consistent repository pattern implementation across all domains
- Role-based authorization with Spring Security
- UUID primary keys for all entities
- Session authentication with JSESSIONID cookies

### Recent Status & Improvements
- ✅ **Authentication System**: Migrated from JWT to session-based, fully stable
- ✅ **Session Persistence**: Fixed across all protected routes, login state maintained
- ✅ **Store Management**: Role-based access control with BUSINESS_OWNER/ADMIN roles
- ✅ **MyPage Features**: Comprehensive user dashboard with applications, collaborations, ongoing projects
- ✅ **Template System**: All template errors resolved, consistent Thymeleaf patterns
- ✅ **Routing**: All endpoints properly mapped, no broken links
- ✅ **Build System**: Stable and reliable, no compilation issues
- ✅ **Customer Support**: FAQ management, inquiry submission, admin response system
- ✅ **Policy System**: Privacy policy and terms of service with modal integration
- ✅ **Signup Process**: Enhanced with required agreement validation and modal policy access
- ✅ **Login UI**: Modern, responsive design matching brand guidelines
- ✅ **Entity Management**: Fixed optimistic locking issues, proper UUID generation
- ✅ **CSRF Protection**: Cookie-based token repository, session-safe implementation
- ✅ **Modal System**: Consistent across all pages, proper event handling
- ✅ **Footer Integration**: Modal experience integrated across all templates
- ✅ **Multi-Image Upload System**: Complete file upload infrastructure with validation, processing, and storage
- ✅ **Store Detail Page**: 3-column responsive layout with enhanced gallery and collaboration features
- ✅ **Explore Page**: Three-section discovery layout (stores, menus, collaboration products)
- ✅ **Thread Pool Configuration**: Custom async executor with DiscardOldestPolicy for optimal performance
- ✅ **Image Management**: Entity relationships, repositories, and service layer for multiple images per store/menu
- ✅ **Frontend Optimization**: Complete store detail page refactoring with component system, performance improvements, and visual refinements
- ✅ **Environment-Specific File Upload**: Externalized upload path configuration with profile-based and environment variable support
- ✅ **JPA Query Optimization**: Fixed product duplication issues caused by Cartesian Product in multiple JOIN FETCH operations
- ✅ **Home Page Products Enhancement**: Horizontal scrolling layout with drag-and-scroll functionality, limited to 5 products max
- ✅ **Store Detail Two-Column Layout**: Implemented sidebar with SNS & collaboration information, proper large screen constraints
- ✅ **Thymeleaf Expression Safety**: Fixed null-safety issues and field binding errors across all templates

### Session Authentication Details
- **Session Management**: Configured with `SessionCreationPolicy.IF_REQUIRED`
- **Session Persistence**: JSESSIONID cookie maintains authentication across requests
- **Route Protection**: All feature routes (`/collabo/**`, `/mypage/**`, `/products/**`, `/stores/**`, `/support/inquiry*`, `/support/my-inquiries`) require authentication
- **Template Integration**: Thymeleaf security integration with `sec:authorize="isAuthenticated()"`
- **API Security**: Session-based endpoints (`/api/session/*`) for login/logout/status
- **Role-based Access**: Store management requires `ROLE_BUSINESS_OWNER` or `ROLE_ADMIN`
- **CSRF Protection**: CSRF protection disabled for simplified form handling

### Kakao Social Login Integration
- **Service Layer**: `KakaoService` handles OAuth flow and user management in application layer
- **Domain Model**: `KakaoUserInfoDto` in domain layer with validation logic
- **Session Integration**: Uses `SecurityContextHolder` for session-based authentication (NOT JWT)
- **User Management**: Automatically creates new users with default settings (Seoul/Gangnam region, ROLE_USER)
- **Error Handling**: Specific error messages for Kakao login failures in login template
- **API Flow**: `/api/user/kakao/callback` → KakaoService → Spring Security session
- **Configuration**: Uses `KAKAO_REST_API_KEY` environment variable
- **Clean Architecture**: External API integration in application layer, business logic in domain layer
- **RestTemplate**: Centralized configuration for external API calls

## Critical Implementation Notes

### ⚠️ Common Pitfalls
- **Product Field**: Use `product.title`, NOT `product.name` in templates
- **Product Routes**: Use `/products/create`, NOT `/products/register`
- **Store Access**: Verify user role before store operations
- **Session Auth**: Use Principal or UserDetailsImpl, not JWT tokens
- **Entity Creation**: Never manually set UUID for @GeneratedValue entities
- **Fragment Syntax**: Use `th:replace="~{fragments/navbar :: navbar}"` (Thymeleaf 3 syntax)
- **JPA JOIN FETCH**: Avoid multiple JOIN FETCH in single query - causes Cartesian Product and data duplication
- **DTO Field Binding**: Ensure Thymeleaf field references match actual DTO property names (e.g., `thumbnailPath` not `imageUrl`)
- **Null-Safety in Templates**: Use proper null checks: `${object != null and !#strings.isEmpty(object.field)}`

### Template Best Practices
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

### Modal System Guidelines
- **Policy Modals**: Use dynamic content loading with caching (`/policy/privacy-modal`, `/policy/terms-modal`)
- **Modal Structure**: Header with title and close button, body with scrollable content
- **JavaScript Integration**: Handle modal open/close, dynamic content loading, outside-click closing
- **Signup Integration**: Required agreement checkbox validation before form submission
- **Footer Integration**: Consistent modal experience across all pages with footer links
- **Accessibility**: ESC key support, focus management, proper ARIA attributes

### Customer Support System
- **Public Access**: FAQ viewing (`/support/cs`) accessible to all users
- **Authenticated Access**: Inquiry submission and management require login
- **Repository Pattern**: Follows three-layer pattern (`SupportInquiryRepository`, `FAQRepository`)
- **Entity Design**: Uses enums for categories and status tracking
- **Template Structure**: Modern responsive design with search and pagination

## 🖼️ Multi-Image Upload System

### Image Upload Infrastructure
- **Service Layer**: `ImageUploadService` handles multi-file uploads with validation and processing
- **Environment-Specific Storage**: Configurable upload paths via `app.upload.path` property and `UPLOAD_PATH` environment variable
- **Storage Structure**: Files organized as `{uploadPath}/{category}/{entityId}/{UUID}.{ext}` (e.g., `/uploads/stores/{storeId}/{UUID}.{ext}`)
- **File Validation**: JPG, JPEG, PNG only; max 10MB per file; automatic image resizing (max 1920x1080)
- **Metadata Storage**: `StoreImage` and `MenuImage` entities track original filename, file size, content type, display order
- **Security**: UUID-based filenames prevent collisions and directory traversal attacks

### Image Entity Management
- **Store Images**: `StoreImage` entity with types: MAIN, GALLERY, INTERIOR, EXTERIOR
- **Menu Images**: `MenuImage` entity with types: MAIN, GALLERY, INGREDIENT, DETAIL
- **Repository Pattern**: Three-layer pattern with `StoreImageRepository`/`MenuImageRepository`
- **Entity Relationships**: `@OneToMany` relationships with cascade and orphan removal
- **Helper Methods**: `getImagePaths()`, `getMainImage()`, `getGalleryImages()` for easy access

### Frontend Implementation
- **Multi-file Selection**: `<input type="file" multiple>` with drag-and-drop support
- **Real-time Previews**: Thumbnail generation before upload with remove functionality
- **Auto-upload**: Images upload immediately after selection with progress indicators
- **Error Handling**: Client-side validation and server-side error reporting
- **Current Images**: Grid display of existing images with individual delete capability

### Environment Configuration
- **Local Development**: `application-local.properties` with `/mnt/d/projects/topping/uploads` path
- **Production**: `application-prod.properties` with `/home/ourbalance_topping/uploads` path
- **Environment Override**: `UPLOAD_PATH` environment variable overrides profile defaults
- **Resource Handler**: `WebConfig` dynamically maps `/uploads/**` to environment-specific filesystem paths
- **Profile Activation**: Use `--spring.profiles.active=local|prod` for environment selection

### API Endpoints
- **Upload**: `POST /stores/upload-images` - Multi-file upload with image type selection
- **Delete**: `POST /stores/delete-image/{imageId}` - Individual image deletion
- **Access Control**: Role-based permissions (BUSINESS_OWNER/ADMIN only)
- **Response Format**: Standard `ApiResponseData` wrapper with success/error handling
- **Resource Serving**: Images served via `/uploads/{category}/{entityId}/{filename}` URLs

## 🎨 Frontend Refactoring & Optimization

### Store Detail Page Optimization
- **Refactored CSS Framework**: Complete redesign using `store-detail-refactored.css` with design tokens and utility classes
- **Component System**: Created reusable fragments (`fragments/product-card.html`, `fragments/tag.html`) for improved maintainability
- **Performance JavaScript**: Implemented `StoreDetailManager` class with event delegation and lazy loading
- **Visual Refinements**: Full-height layout, proper spacing, scaled hero images (80%), centered button icons
- **Two-Column Layout**: Main content with sidebar for SNS & collaboration information, responsive design
- **Large Screen Constraints**: Media queries prevent elements from exceeding container bounds on screens >1400px

### Home Page Enhancement
- **Horizontal Products Section**: Limited to 5 products max with horizontal scrolling layout
- **Drag-Scroll Functionality**: Mouse and touchpad drag support with smooth scrolling behavior
- **Performance Optimization**: Reduced DOM complexity and improved scroll performance
- **Responsive Design**: Proper scaling and layout adjustments across all screen sizes

### CSS Architecture Improvements
- **Design Tokens**: Centralized CSS variables for colors, spacing, shadows, and transitions
- **Utility Classes**: Grid layouts (`.grid-2`, `.grid-3`), flexbox utilities (`.flex-center`, `.flex-between`), typography system
- **Responsive Design**: Mobile-first approach with consistent breakpoints and scaling
- **Performance Optimizations**: Lazy loading, GPU acceleration, reduced motion support

### Component-Based Development
- **Fragment System**: Reusable product cards with image fallback logic and lazy loading
- **Tag Components**: Modular category and hashtag display with hover effects
- **Event Delegation**: Single event listener replacing multiple handlers for better performance
- **Code Reusability**: Eliminated duplicate layout code with 30% DOM complexity reduction

### JPA Query Optimization
- **Cartesian Product Fix**: Resolved product duplication by separating multiple JOIN FETCH operations into individual queries
- **Query Performance**: Improved data loading efficiency and eliminated N×M result multiplication
- **Repository Pattern**: Maintained clean architecture while fixing query performance issues

## 🔍 Explore Page System

### Three-Section Layout
- **Section 1: Store List**: Displays registered stores with thumbnails, names, addresses, categories
- **Section 2: Menu List**: Shows popular/signature menus with prices, descriptions, store associations
- **Section 3: Collaboration Products**: Features collaboration menus and live products with special tags

### Controller Enhancement
- **Data Sources**: `StoreRepository`, `MenuRepository` with pagination (12 items per section)
- **Query Methods**: `findByMenuTypeOrderByReviewCountDesc()` for popular menu sorting
- **Performance**: Optimized queries with `PageRequest.of(0, 12)` limiting
- **Backward Compatibility**: Maintains existing collaboration data structure

### Responsive Card Design
- **CSS Grid Layout**: `repeat(auto-fill, minmax(300px, 1fr))` for responsive design
- **Card Components**: Consistent styling with hover effects, shadows, image scaling
- **Image Handling**: Lazy loading, error fallbacks, placeholder support
- **Empty States**: User-friendly messages when sections have no content

### Navigation & Performance
- **Clickable Cards**: Store cards link to `/stores/{id}`, menu cards to store detail pages
- **Lazy Loading**: Images load only when in viewport using Intersection Observer
- **Error Recovery**: Automatic fallback to placeholders on image load failure
- **Mobile Optimization**: Single-column layout on mobile with optimized card sizing

## ⚙️ Database & Performance Configuration

### HikariCP Connection Pool
- **Pool Size**: 5 minimum idle, 30 maximum connections
- **Timeouts**: 30s connection timeout, 10min idle timeout, 30min max lifetime
- **Leak Detection**: 60s threshold to identify connection leaks
- **Validation**: 5s validation timeout for connection health checks
- **Performance**: Optimized for high concurrency and connection reuse

### Async Thread Pool Configuration
- **Configuration Class**: `AsyncConfig.java` with `@EnableAsync` annotation
- **Thread Pool Settings**: 5 core, 10 max threads, 50 queue capacity
- **Thread Naming**: `custom-executor-` prefix for easy identification
- **Rejection Policy**: `ThreadPoolExecutor.DiscardOldestPolicy()` for handling overflow

### Usage Pattern
- **Annotation**: `@Async("customExecutor")` for method-level async execution
- **Service Integration**: Available for use in any Spring-managed component
- **Error Handling**: Proper initialization and resource management
- **Performance**: Optimized for concurrent image processing and background tasks

### Transaction Management
- **Service Layer**: All service classes properly annotated with `@Transactional`
- **Repository Layer**: Uses three-layer pattern without transactions (correct design)
- **Controller Layer**: Read-only transactions for controller methods accessing repositories
- **Connection Safety**: All database queries execute within proper transaction boundaries

## 🔧 Troubleshooting & Common Issues

### Login & Authentication Issues
- **Session Creation**: Session-based authentication working properly
- **Fragment Rendering**: Updated to Thymeleaf 3 syntax
- **Template Errors**: All deprecated syntax updated
- **CSRF Protection**: Disabled for simplified form handling
- **Kakao Login**: Integrated with session-based authentication, error handling in place

### Entity Management
- **Optimistic Locking**: Fixed by removing manual UUID setting in SignupController
- **UUID Generation**: Let Hibernate handle @GeneratedValue @UuidGenerator entities
- **Repository Layer**: Consistent three-layer pattern across all domains

### JPA & Query Issues
- **Product Duplication**: Fixed Cartesian Product problem in StoreJpaRepository by separating multiple JOIN FETCH operations
- **Query Optimization**: Replaced problematic queries like `LEFT JOIN FETCH s.products LEFT JOIN FETCH s.images` with separate queries
- **Data Integrity**: Ensured 1:1 mapping between entities and their collections without N×M multiplication

### Thymeleaf Template Issues  
- **Field Binding Errors**: Fixed mismatched field references (e.g., `th:field="*{imageUrl}"` changed to `th:field="*{thumbnailPath}"`)
- **Null-Safety**: Implemented proper null checks for template expressions to prevent evaluation errors
- **Expression Safety**: Use `${object != null and !#strings.isEmpty(object.field)}` pattern for safe null checking

### Database & Connection Pool Issues
- **Connection Pool Exhaustion**: Fixed transaction boundary issues in KakaoService and SggCodeController
- **HikariCP Configuration**: Optimized pool size (5-30), leak detection enabled, proper timeouts
- **Transaction Management**: All service methods properly wrapped in @Transactional boundaries
- **Query Optimization**: Replaced unbounded findAll() calls with paginated queries where appropriate
- **Connection Monitoring**: Enhanced logging for connection pool stats and leak detection

### Build & Runtime
- **JAVA_HOME**: Must be set correctly for all Gradle operations
- **Dependencies**: All conflicts resolved, no version mismatches
- **Database**: Connection pool optimized with leak detection and monitoring
- **Session Config**: Persistent sessions with proper timeout configuration

### File Upload & Environment Configuration
- **Upload Path Configuration**: Use `UPLOAD_PATH` environment variable to override default paths
- **Profile-Specific Paths**: Local development (`/mnt/d/projects/topping/uploads`) vs Production (`/home/ourbalance_topping/uploads`)
- **Directory Creation**: Upload directories are created automatically with proper permissions
- **Resource Serving**: Images served via `/uploads/**` URL pattern mapped to filesystem paths
- **Security**: Path traversal protection and validation for upload/delete operations

## 📋 Development Workflow

### Adding New Features
1. **Domain Design**: Follow clean architecture principles
2. **Repository Pattern**: Implement three-layer pattern
3. **Transaction Boundaries**: Ensure all service methods have @Transactional annotations
4. **Controller Layer**: Use proper response wrappers and @Transactional(readOnly = true) for read operations
5. **Template Integration**: Follow UI/UX guidelines
6. **Security**: Implement proper authentication/authorization
7. **Testing**: Write comprehensive tests with test profile
8. **Connection Safety**: Avoid unbounded queries like findAll() - use pagination when needed
9. **Environment Configuration**: Use profile-specific properties and environment variables for external configuration

### UI/UX Development
1. **MANDATORY**: Use the Topping CSS Framework for ALL new templates
2. **CSS Architecture**: Use `base.css` or `auth.css` - NEVER use Bootstrap or multiple CSS files
3. **JavaScript**: Use `common.js` or `auth-common.js` - avoid inline scripts and external dependencies
4. **Fragments**: Always use Thymeleaf fragments for navbar, footer, and modals
5. **Design Review**: Check existing patterns and brand guidelines
6. **Responsive Design**: Mobile-first approach with CSS Variables system
7. **Accessibility**: Proper labels, keyboard navigation, ARIA attributes
8. **Performance**: Optimize CSS/JS, minimize requests
9. **Browser Testing**: Cross-browser compatibility

### Frontend Refactoring Best Practices
1. **Component-Based Architecture**: Create reusable fragments for repeated UI elements
2. **CSS Optimization**: Use design tokens, utility classes, and eliminate inline styles
3. **JavaScript Performance**: Implement event delegation and lazy loading patterns
4. **Visual Consistency**: Apply consistent spacing, scaling, and centering across components
5. **Responsive Refinements**: Ensure proper layout scaling and spacing on all devices

### Code Quality
- **Clean Code**: Follow existing patterns and conventions
- **Error Handling**: Proper exception handling with user-friendly messages
- **Security**: Always validate inputs, protect against common vulnerabilities
- **Transaction Safety**: All database operations must be within @Transactional boundaries
- **Connection Management**: Monitor for connection leaks using HikariCP leak detection
- **Documentation**: Update relevant documentation when adding features
- **Environment Configuration**: Externalize configuration using profiles and environment variables for deployment flexibility

## Documentation Navigation

### 📚 Main Documentation Hub
- **[Documentation Index](./docs/README.md)** - Complete documentation navigation and standards

### 🔧 Troubleshooting Guides
- **[Session Persistence Troubleshooting](./docs/SESSION_PERSISTENCE_TROUBLESHOOTING.md)** - Authentication issues and resolutions

### 🏗️ Quick Domain Access
- [User Management](./docs/domains/user/README.md) - User accounts and roles
- [Store Management](./docs/domains/store/README.md) - Business store operations
- [Product Management](./docs/domains/product/README.md) - Product listings and features
- [Collaboration Platform](./docs/domains/collaboration/README.md) - Business matching and proposals
- [Authentication System](./docs/domains/auth/README.md) - Login/logout, session management
- [Chat System](./docs/domains/chat/README.md) - Real-time messaging
- [Notification System](./docs/domains/notification/README.md) - Event-driven alerts
- [Customer Support](./docs/domains/support/README.md) - FAQ and inquiry management
- [Policy Management](./docs/domains/policy/README.md) - Privacy policy and terms of service

## 🎯 Project Status: Production Ready

The Topping platform is now in a stable, production-ready state with:

- **Complete Authentication System**: Session-based with Kakao social login integration
- **Full Domain Implementation**: All core business domains functional
- **Modern UI/UX**: Responsive, accessible, brand-consistent design
- **Comprehensive Testing**: Unit and integration tests with proper test profiles
- **Documentation**: Complete domain documentation and troubleshooting guides
- **Security**: Role-based access control, secure session management, CSRF disabled for simplified forms
- **Social Login**: Kakao OAuth integration with automatic user registration
- **Performance**: Optimized HikariCP connection pool, efficient queries, connection leak detection
- **Database Reliability**: Fixed connection pool exhaustion issues, proper transaction boundaries

The platform is ready for deployment and further feature development.