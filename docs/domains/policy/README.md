# 📋 Policy Domain

The Policy Domain manages privacy policy and terms of service content with modal integration for seamless user experience.

## Overview

The policy system provides:
- Privacy policy and terms of service content
- Modal-based policy display during signup
- Footer integration with consistent modal experience
- Dynamic content loading with caching
- Compliance with legal requirements

## Architecture

### Controller Layer

#### PolicyController
```java
@Controller
@RequestMapping("/policy")
public class PolicyController {
    
    @GetMapping("/privacy")
    public String privacyPolicy();
    
    @GetMapping("/terms")
    public String termsOfService();
    
    @GetMapping("/privacy-modal")
    public String privacyPolicyModal();
    
    @GetMapping("/terms-modal")
    public String termsOfServiceModal();
}
```

### Template Architecture

#### Policy Templates
- `policy/privacy-policy.html` - Privacy policy content
- `policy/terms-of-service.html` - Terms of service content

#### Modal Integration
- **Signup Page**: Modal integration for agreement checkbox
- **Footer**: Consistent modal experience across all pages
- **Dynamic Loading**: AJAX-based content loading with caching

## API Endpoints

### Public Endpoints
```
GET  /policy/privacy           - Privacy policy full page
GET  /policy/terms             - Terms of service full page
GET  /policy/privacy-modal     - Privacy policy modal content
GET  /policy/terms-modal       - Terms of service modal content
```

## Modal System Implementation

### Signup Integration

#### HTML Structure
```html
<div class="form-group">
    <div class="checkbox-group">
        <input type="checkbox" id="termsAgreement" name="termsAgreement" required>
        <label for="termsAgreement" class="checkbox-label">
            <a href="#" onclick="openTermsModal()" class="terms-link">이용약관</a> 및 
            <a href="#" onclick="openPrivacyModal()" class="terms-link">개인정보처리방침</a>에 동의합니다.
        </label>
    </div>
</div>
```

#### Modal Structure
```html
<div id="termsModal" class="modal">
    <div class="modal-content">
        <div class="modal-header">
            <h2 class="modal-title">이용약관</h2>
            <span class="close" onclick="closeTermsModal()">&times;</span>
        </div>
        <div class="modal-body" id="termsModalBody">
            <div class="loading-spinner">
                <div class="spinner"></div>
                <span>약관을 불러오는 중...</span>
            </div>
        </div>
    </div>
</div>
```

### JavaScript Implementation

#### Dynamic Content Loading
```javascript
async function openTermsModal() {
    const modal = document.getElementById('termsModal');
    const modalBody = document.getElementById('termsModalBody');
    
    modal.style.display = 'block';
    document.body.style.overflow = 'hidden';
    
    // Load terms content if not already loaded
    if (!modalBody.dataset.loaded) {
        try {
            const response = await fetch('/policy/terms-modal');
            const html = await response.text();
            modalBody.innerHTML = html;
            modalBody.dataset.loaded = 'true';
        } catch (error) {
            console.error('Failed to load terms:', error);
            modalBody.innerHTML = '<p style="color: #e74c3c;">약관을 불러오는 중 오류가 발생했습니다.</p>';
        }
    }
}
```

#### Form Validation
```javascript
function validateTermsAgreement() {
    const termsCheckbox = document.getElementById('termsAgreement');
    const isChecked = termsCheckbox.checked;
    
    if (isChecked) {
        hideError('termsError');
        validation.terms = true;
    } else {
        validation.terms = false;
    }
}
```

## Footer Integration

### Footer Modal Implementation
```html
<div class="footer-links">
    <a href="#" onclick="openFooterPrivacyModal(event)">개인정보 처리방침</a>
    <a href="#" onclick="openFooterTermsModal(event)">서비스 이용약관</a>
</div>
```

### Consistent Modal Experience
- **Unified Design**: Same modal styling across signup and footer
- **Event Handling**: Consistent JavaScript event management
- **Error Handling**: Graceful error states for failed content loading
- **Accessibility**: Keyboard navigation and screen reader support

## Security Configuration

### Public Access
```java
// Public policy endpoints
.requestMatchers("/policy/**").permitAll()
```

All policy endpoints are publicly accessible to ensure compliance and user access to legal documents.

## Content Management

### Privacy Policy Structure
1. **Data Collection**: Information gathering purposes
2. **Data Usage**: How personal information is used
3. **Data Retention**: Storage periods and deletion policies
4. **Third-party Sharing**: Data sharing guidelines
5. **User Rights**: Access, modification, and deletion rights
6. **Contact Information**: Privacy officer contact details

### Terms of Service Structure
1. **Service Definition**: Platform scope and functionality
2. **User Obligations**: Acceptable use policies
3. **Collaboration Rules**: Business interaction guidelines
4. **Account Management**: Registration and termination
5. **Intellectual Property**: Content ownership and usage rights
6. **Liability Limitations**: Platform responsibility boundaries
7. **Dispute Resolution**: Legal procedures and jurisdiction

## Styling and Design

### Modal Styling
```css
.modal {
    display: none;
    position: fixed;
    z-index: 1000;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.4);
}

.modal-content {
    background-color: #fefefe;
    margin: 5% auto;
    padding: 20px;
    border: none;
    border-radius: 10px;
    width: 80%;
    max-width: 600px;
    max-height: 80vh;
    overflow-y: auto;
}
```

### Responsive Design
- **Mobile-first**: Optimized for mobile devices
- **Scrollable Content**: Proper overflow handling for long content
- **Touch-friendly**: Appropriate touch targets and gestures
- **Accessibility**: WCAG compliance for screen readers

## Integration Points

### Signup Process Integration
- **Required Agreement**: Cannot submit signup without checking agreement
- **Modal Access**: Easy access to policy content during signup
- **Form Validation**: Real-time validation of agreement checkbox
- **Error States**: Clear error messages for missing agreement

### Footer Integration
- **Global Access**: Policy links available on all pages
- **Consistent Experience**: Same modal functionality across pages
- **Performance**: Cached content loading for subsequent views
- **Fallback**: Direct page access if modal fails

### Authentication Integration
- **Public Access**: No authentication required for policy content
- **Session Independence**: Works regardless of login status
- **CSRF Protection**: Proper security configuration

## Performance Optimization

### Content Caching
- **Browser Caching**: Efficient client-side caching
- **Dynamic Loading**: Load content only when needed
- **Memory Management**: Proper cleanup of modal content
- **Error Recovery**: Graceful handling of network failures

### Template Optimization
- **Minimal Dependencies**: Lightweight template structure
- **Lazy Loading**: Content loaded on demand
- **Efficient Rendering**: Optimized Thymeleaf processing
- **Mobile Performance**: Optimized for mobile devices

## Legal Compliance

### GDPR Compliance
- **Clear Language**: Plain language privacy policy
- **User Rights**: Explicit user right descriptions
- **Data Processing**: Transparent data handling descriptions
- **Contact Information**: Clear privacy officer contact

### Korean Privacy Law Compliance
- **PIPA Compliance**: Personal Information Protection Act adherence
- **Data Minimization**: Collect only necessary information
- **Consent Management**: Clear consent mechanisms
- **Data Subject Rights**: User access and deletion rights

## Future Enhancements

### Content Management System
- **Admin Interface**: Web-based policy content editing
- **Version Control**: Policy version management
- **Change Notifications**: User notification for policy updates
- **Multi-language Support**: Localization for different regions

### Advanced Features
- **Policy Acceptance Tracking**: User agreement logging
- **Update Notifications**: Automated policy change notifications
- **Compliance Dashboard**: Legal compliance monitoring
- **Analytics**: Policy access and interaction metrics

## Best Practices

### Development Guidelines
- **Content Separation**: Keep policy content in separate templates
- **Modal Reusability**: Design reusable modal components
- **Error Handling**: Comprehensive error states and recovery
- **Performance**: Optimize for fast loading and smooth interactions

### Legal Guidelines
- **Regular Updates**: Keep policies current with legal requirements
- **Clear Language**: Use plain language for user understanding
- **Comprehensive Coverage**: Address all data processing activities
- **Professional Review**: Regular legal review of policy content

### User Experience Guidelines
- **Accessibility**: Ensure screen reader compatibility
- **Mobile Optimization**: Optimize for mobile reading
- **Clear Navigation**: Easy access to policy sections
- **Reading Experience**: Proper typography and spacing

## Troubleshooting

### Common Issues
1. **Modal Not Opening**: Check JavaScript console for errors
2. **Content Loading Failures**: Verify endpoint accessibility
3. **Form Validation**: Ensure checkbox validation is working
4. **Styling Issues**: Check CSS conflicts with existing styles

### Performance Issues
- **Slow Loading**: Optimize content size and delivery
- **Memory Leaks**: Proper modal cleanup and event removal
- **Cache Issues**: Clear browser cache for updated content
- **Mobile Performance**: Optimize for mobile network conditions

### Security Considerations
- **XSS Prevention**: Proper content sanitization
- **CSRF Protection**: Maintain CSRF tokens in forms
- **Content Security**: Validate all user inputs
- **Access Control**: Ensure proper public access configuration