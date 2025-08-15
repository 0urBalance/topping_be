# Policy Domain - Claude Guidance

## Overview
The Policy domain manages privacy policy and terms of service with modal integration across the Topping platform.

## Core Features
- **Privacy Policy**: Data collection, usage, and protection policies
- **Terms of Service**: Platform usage terms and conditions  
- **Modal Integration**: Seamless modal experience across all pages
- **Signup Integration**: Required agreement validation during registration

## Controller Patterns

### Policy Routes
```java
@Controller
@RequestMapping("/policy")
public class PolicyController {
    
    @GetMapping("/privacy-policy")
    public String privacyPolicy() {
        return "policy/privacy-policy";
    }
    
    @GetMapping("/terms-of-service") 
    public String termsOfService() {
        return "policy/terms-of-service";
    }
}
```

## Template Structure

### Privacy Policy Template
```html
<!-- policy/privacy-policy.html -->
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>개인정보 처리방침 - Topping</title>
    <link rel="stylesheet" th:href="@{/css/base.css}">
</head>
<body>
    <div th:replace="~{fragments/navbar :: navbar}"></div>
    
    <main class="policy-content">
        <div class="policy-container">
            <h1>개인정보 처리방침</h1>
            
            <section class="policy-section">
                <h2>1. 개인정보의 처리목적</h2>
                <p>토핑(Topping)은 다음의 목적을 위하여 개인정보를 처리합니다...</p>
            </section>
            
            <section class="policy-section">
                <h2>2. 개인정보의 처리 및 보유기간</h2>
                <p>개인정보는 법령에 따른 개인정보 보유·이용기간 또는 정보주체로부터...</p>
            </section>
            
            <!-- Additional policy sections -->
        </div>
    </main>
    
    <div th:replace="~{fragments/footer :: footer}"></div>
    <div th:replace="~{fragments/modals :: modals}"></div>
</body>
</html>
```

### Terms of Service Template  
```html
<!-- policy/terms-of-service.html -->
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>이용약관 - Topping</title>
    <link rel="stylesheet" th:href="@{/css/base.css}">
</head>
<body>
    <div th:replace="~{fragments/navbar :: navbar}"></div>
    
    <main class="policy-content">
        <div class="policy-container">
            <h1>이용약관</h1>
            
            <section class="policy-section">
                <h2>제1조 (목적)</h2>
                <p>이 약관은 토핑(Topping) 서비스의 이용조건 및 절차...</p>
            </section>
            
            <section class="policy-section">
                <h2>제2조 (정의)</h2>
                <p>이 약관에서 사용하는 용어의 정의는 다음과 같습니다...</p>
            </section>
            
            <!-- Additional terms sections -->
        </div>
    </main>
    
    <div th:replace="~{fragments/footer :: footer}"></div>
    <div th:replace="~{fragments/modals :: modals}"></div>
</body>
</html>
```

## Modal Integration

### Footer Integration
```html
<!-- fragments/footer.html -->
<footer class="footer">
    <div class="footer-content">
        <div class="footer-links">
            <a href="#" onclick="openModal('/policy/privacy-policy', '개인정보 처리방침')">개인정보 처리방침</a>
            <a href="#" onclick="openModal('/policy/terms-of-service', '이용약관')">이용약관</a>
            <a href="/support/cs">고객센터</a>
        </div>
        
        <div class="footer-info">
            <p>&copy; 2024 Topping. All rights reserved.</p>
        </div>
    </div>
</footer>
```

### Modal System
```html
<!-- fragments/modals.html -->
<div id="policyModal" class="modal" style="display: none;">
    <div class="modal-content">
        <div class="modal-header">
            <h2 id="modalTitle">정책</h2>
            <button class="modal-close" onclick="closeModal()">&times;</button>
        </div>
        <div class="modal-body" id="modalBody">
            <!-- Policy content loaded here -->
        </div>
        <div class="modal-footer">
            <button class="btn btn-secondary" onclick="closeModal()">닫기</button>
        </div>
    </div>
</div>
```

### JavaScript Modal Functionality
```javascript
// Modal management for policy documents
function openModal(url, title) {
    const modal = document.getElementById('policyModal');
    const modalTitle = document.getElementById('modalTitle');
    const modalBody = document.getElementById('modalBody');
    
    modalTitle.textContent = title;
    modalBody.innerHTML = '<div class="loading">로딩 중...</div>';
    
    // Fetch policy content
    fetch(url)
        .then(response => response.text())
        .then(html => {
            // Extract main content from full page
            const parser = new DOMParser();
            const doc = parser.parseFromString(html, 'text/html');
            const content = doc.querySelector('.policy-content .policy-container');
            
            if (content) {
                modalBody.innerHTML = content.innerHTML;
            } else {
                modalBody.innerHTML = '<p>내용을 불러올 수 없습니다.</p>';
            }
        })
        .catch(error => {
            console.error('Error loading policy:', error);
            modalBody.innerHTML = '<p>내용을 불러오는 중 오류가 발생했습니다.</p>';
        });
    
    modal.style.display = 'block';
    document.body.style.overflow = 'hidden'; // Prevent background scrolling
}

function closeModal() {
    const modal = document.getElementById('policyModal');
    modal.style.display = 'none';
    document.body.style.overflow = 'auto'; // Restore scrolling
}

// Close modal when clicking outside
window.onclick = function(event) {
    const modal = document.getElementById('policyModal');
    if (event.target === modal) {
        closeModal();
    }
}

// Keyboard navigation
document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        closeModal();
    }
});
```

## Signup Integration

### Agreement Validation
```html
<!-- auth/signup/step1.html -->
<form th:object="${signupRequest}" method="post">
    <!-- User input fields -->
    
    <div class="agreement-section">
        <div class="agreement-item">
            <label>
                <input type="checkbox" id="termsAgreement" name="termsAgreement" required>
                <a href="#" onclick="openModal('/policy/terms-of-service', '이용약관')">이용약관</a>에 동의합니다 (필수)
            </label>
        </div>
        
        <div class="agreement-item">
            <label>
                <input type="checkbox" id="privacyAgreement" name="privacyAgreement" required>
                <a href="#" onclick="openModal('/policy/privacy-policy', '개인정보 처리방침')">개인정보 처리방침</a>에 동의합니다 (필수)
            </label>
        </div>
        
        <div class="agreement-item">
            <label>
                <input type="checkbox" id="marketingAgreement" name="marketingAgreement">
                마케팅 정보 수신에 동의합니다 (선택)
            </label>
        </div>
    </div>
    
    <button type="submit" id="signupSubmit" disabled>다음 단계</button>
</form>
```

### Agreement Validation JavaScript
```javascript
// Signup agreement validation
function setupAgreementValidation() {
    const termsCheckbox = document.getElementById('termsAgreement');
    const privacyCheckbox = document.getElementById('privacyAgreement');
    const submitButton = document.getElementById('signupSubmit');
    
    function validateAgreements() {
        const isValid = termsCheckbox.checked && privacyCheckbox.checked;
        submitButton.disabled = !isValid;
        
        if (isValid) {
            submitButton.classList.add('enabled');
        } else {
            submitButton.classList.remove('enabled');
        }
    }
    
    termsCheckbox.addEventListener('change', validateAgreements);
    privacyCheckbox.addEventListener('change', validateAgreements);
    
    // Initial validation
    validateAgreements();
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', setupAgreementValidation);
```

## CSS Styling

### Policy Page Styles
```css
/* Policy page styling */
.policy-content {
    max-width: 800px;
    margin: 2rem auto;
    padding: 0 1rem;
}

.policy-container {
    background: white;
    border-radius: 8px;
    padding: 2rem;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.policy-section {
    margin-bottom: 2rem;
}

.policy-section h2 {
    color: #6B3410;
    border-bottom: 2px solid #6B3410;
    padding-bottom: 0.5rem;
    margin-bottom: 1rem;
}

/* Modal styling */
.modal {
    position: fixed;
    z-index: 1000;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0,0,0,0.5);
}

.modal-content {
    background-color: white;
    margin: 5% auto;
    border-radius: 8px;
    width: 90%;
    max-width: 800px;
    max-height: 80vh;
    overflow: hidden;
}

.modal-header {
    padding: 1rem 1.5rem;
    border-bottom: 1px solid #eee;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.modal-body {
    padding: 1.5rem;
    max-height: 60vh;
    overflow-y: auto;
}

.modal-close {
    background: none;
    border: none;
    font-size: 1.5rem;
    cursor: pointer;
    color: #666;
}

/* Agreement section styling */
.agreement-section {
    margin: 1.5rem 0;
    padding: 1rem;
    background: #f8f9fa;
    border-radius: 8px;
}

.agreement-item {
    margin-bottom: 0.75rem;
}

.agreement-item label {
    display: flex;
    align-items: center;
    cursor: pointer;
}

.agreement-item input[type="checkbox"] {
    margin-right: 0.5rem;
}

.agreement-item a {
    color: #6B3410;
    text-decoration: underline;
    margin: 0 0.25rem;
}
```

## Common Pitfalls
- ❌ **Missing Modal Integration**: Always include modal fragments in policy templates
- ❌ **Hardcoded Links**: Use Thymeleaf URL expressions for policy links
- ❌ **Missing Validation**: Require agreement validation in signup forms
- ❌ **Poor Mobile UX**: Ensure modal content is scrollable on small screens
- ❌ **Accessibility Issues**: Include proper ARIA labels and keyboard navigation

## Integration Points
- **Auth Domain**: Signup agreement validation
- **Footer**: Universal policy access across all pages
- **Modal System**: Consistent modal experience
- **Base Templates**: Policy access from any page

## Testing Patterns
```java
@ActiveProfiles("test")
class PolicyControllerTest {
    // Test policy page rendering
    // Verify modal content loading
    // Test agreement validation in signup
}
```

## Related Documentation
- [Main Claude Guidance](../../../CLAUDE.md)
- [Auth Domain](../auth/CLAUDE.md)
- [CSS Framework](../../technical/css-framework.md)