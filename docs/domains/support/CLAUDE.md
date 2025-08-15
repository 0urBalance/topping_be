# Support Domain - Claude Guidance

## Overview
The Support domain provides customer support functionality including FAQ management and inquiry submission system for the Topping platform.

## Core Entities

### FAQ
```java
@Entity
public class FAQ {
    @Id @GeneratedValue private UUID uuid;
    private String question;
    private String answer;
    @Enumerated(EnumType.STRING) private FAQCategory category;
    private int displayOrder;
    private boolean isActive = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### SupportInquiry
```java
@Entity
public class SupportInquiry {
    @Id @GeneratedValue private UUID uuid;
    @ManyToOne private User inquirer;
    private String title;
    private String content;
    private String email; // Contact email
    @Enumerated(EnumType.STRING) private InquiryCategory category;
    @Enumerated(EnumType.STRING) private InquiryStatus status = InquiryStatus.PENDING;
    private String adminResponse;
    private LocalDateTime respondedAt;
    private LocalDateTime createdAt;
}
```

### Enums
```java
public enum FAQCategory {
    GENERAL("일반"),
    ACCOUNT("계정"),
    COLLABORATION("콜라보"),
    PAYMENT("결제"),
    TECHNICAL("기술지원");
}

public enum InquiryCategory {
    GENERAL("일반 문의"),
    TECHNICAL("기술 문의"),
    BILLING("결제 문의"),
    COLLABORATION("콜라보 문의"),
    ACCOUNT("계정 문의"),
    BUG_REPORT("버그 신고");
}

public enum InquiryStatus {
    PENDING("대기중"),
    IN_PROGRESS("처리중"),
    COMPLETED("완료"),
    CLOSED("종료");
}
```

## Repository Pattern
- **Domain Interface**: `FAQRepository`, `SupportInquiryRepository`
- **JPA Interface**: `FAQJpaRepository`, `SupportInquiryJpaRepository`
- **Implementation**: `FAQRepositoryImpl`, `SupportInquiryRepositoryImpl`

## Service Layer Patterns

### FAQ Management
```java
@Service
public class SupportService {
    
    public List<FAQ> getActiveFAQsByCategory(FAQCategory category) {
        if (category != null) {
            return faqRepository.findByCategoryAndIsActiveTrueOrderByDisplayOrder(category);
        }
        return faqRepository.findByIsActiveTrueOrderByDisplayOrder();
    }
    
    public FAQ getFAQById(UUID faqId) {
        return faqRepository.findByUuidAndIsActiveTrue(faqId)
            .orElseThrow(() -> new EntityNotFoundException("FAQ를 찾을 수 없습니다"));
    }
    
    // Admin functions
    @PreAuthorize("hasRole('ADMIN')")
    public FAQ createFAQ(FAQCreateRequest request) {
        FAQ faq = FAQ.builder()
            .question(request.getQuestion())
            .answer(request.getAnswer())
            .category(request.getCategory())
            .displayOrder(request.getDisplayOrder())
            .build();
        return faqRepository.save(faq);
    }
}
```

### Inquiry Management
```java
public SupportInquiry createInquiry(InquiryRequest request, User inquirer) {
    SupportInquiry inquiry = SupportInquiry.builder()
        .inquirer(inquirer)
        .title(request.getTitle())
        .content(request.getContent())
        .email(request.getEmail())
        .category(request.getCategory())
        .build();
    
    SupportInquiry saved = supportInquiryRepository.save(inquiry);
    
    // Send notification to admin
    notificationService.createAdminInquiryNotification(saved);
    
    return saved;
}

@PreAuthorize("hasRole('ADMIN')")
public SupportInquiry respondToInquiry(UUID inquiryId, String response) {
    SupportInquiry inquiry = supportInquiryRepository.findById(inquiryId)
        .orElseThrow(() -> new EntityNotFoundException("문의를 찾을 수 없습니다"));
    
    inquiry.setAdminResponse(response);
    inquiry.setStatus(InquiryStatus.COMPLETED);
    inquiry.setRespondedAt(LocalDateTime.now());
    
    SupportInquiry saved = supportInquiryRepository.save(inquiry);
    
    // Send response notification to inquirer
    notificationService.createInquiryResponseNotification(saved);
    
    return saved;
}
```

## Controller Patterns

### Public FAQ Access
```java
@Controller
@RequestMapping("/support")
public class SupportController {
    
    @GetMapping("/cs")
    public String customerSupport(Model model, 
                                @RequestParam(required = false) FAQCategory category) {
        // Public access - no authentication required
        List<FAQ> faqs = supportService.getActiveFAQsByCategory(category);
        model.addAttribute("faqs", faqs);
        model.addAttribute("categories", FAQCategory.values());
        model.addAttribute("selectedCategory", category);
        return "support/cs";
    }
    
    @GetMapping("/faq/{faqId}")
    public String faqDetail(@PathVariable UUID faqId, Model model) {
        FAQ faq = supportService.getFAQById(faqId);
        model.addAttribute("faq", faq);
        return "support/faq-detail";
    }
}
```

### Authenticated Inquiry System
```java
@GetMapping("/inquiry-form")
public String inquiryForm(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
    // Authentication required
    if (userDetails == null) {
        return "redirect:/auth/login";
    }
    
    model.addAttribute("inquiryRequest", new InquiryRequest());
    model.addAttribute("categories", InquiryCategory.values());
    return "support/inquiry-form";
}

@PostMapping("/inquiry")
public String submitInquiry(@ModelAttribute @Valid InquiryRequest request,
                          BindingResult bindingResult,
                          @AuthenticationPrincipal UserDetailsImpl userDetails,
                          RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
        return "support/inquiry-form";
    }
    
    SupportInquiry inquiry = supportService.createInquiry(request, userDetails.getUser());
    redirectAttributes.addFlashAttribute("message", "문의가 성공적으로 제출되었습니다");
    return "redirect:/support/my-inquiries";
}

@GetMapping("/my-inquiries")
public String myInquiries(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
    List<SupportInquiry> inquiries = supportService.getUserInquiries(userDetails.getUser());
    model.addAttribute("inquiries", inquiries);
    return "support/my-inquiries";
}
```

## Template Patterns

### FAQ Display
```html
<!-- support/cs.html -->
<div class="faq-container">
    <div class="faq-categories">
        <a th:href="@{/support/cs}" 
           th:class="'category-tab' + (${selectedCategory == null} ? ' active' : '')">전체</a>
        <a th:each="category : ${categories}"
           th:href="@{/support/cs(category=${category})}"
           th:class="'category-tab' + (${selectedCategory == category} ? ' active' : '')"
           th:text="${category.displayName}">Category</a>
    </div>
    
    <div class="faq-list">
        <div th:each="faq : ${faqs}" class="faq-item">
            <div class="faq-question" onclick="toggleFAQ(this)">
                <h3 th:text="${faq.question}">Question</h3>
                <span class="faq-toggle">+</span>
            </div>
            <div class="faq-answer" style="display: none;">
                <p th:text="${faq.answer}">Answer</p>
            </div>
        </div>
    </div>
</div>
```

### Inquiry Form
```html
<!-- support/inquiry-form.html -->
<form th:object="${inquiryRequest}" method="post" action="/support/inquiry">
    <div class="form-group">
        <label for="category">문의 유형</label>
        <select th:field="*{category}" id="category" required>
            <option value="">문의 유형을 선택하세요</option>
            <option th:each="category : ${categories}" 
                    th:value="${category}" 
                    th:text="${category.displayName}">Category</option>
        </select>
    </div>
    
    <div class="form-group">
        <label for="email">연락처 이메일</label>
        <input th:field="*{email}" type="email" id="email" required 
               placeholder="답변받을 이메일 주소" />
    </div>
    
    <div class="form-group">
        <label for="title">제목</label>
        <input th:field="*{title}" type="text" id="title" required 
               placeholder="문의 제목을 입력하세요" />
    </div>
    
    <div class="form-group">
        <label for="content">문의 내용</label>
        <textarea th:field="*{content}" id="content" rows="6" required
                  placeholder="문의 내용을 자세히 작성해주세요"></textarea>
    </div>
    
    <button type="submit">문의 제출</button>
</form>
```

### Inquiry Status Display
```html
<!-- support/my-inquiries.html -->
<div class="inquiry-list">
    <div th:each="inquiry : ${inquiries}" class="inquiry-item">
        <div class="inquiry-header">
            <h3 th:text="${inquiry.title}">Inquiry Title</h3>
            <span th:class="'status-badge status-' + ${inquiry.status.name().toLowerCase()}"
                  th:text="${inquiry.status.displayName}">Status</span>
        </div>
        
        <div class="inquiry-meta">
            <span th:text="${inquiry.category.displayName}">Category</span>
            <span th:text="${#temporals.format(inquiry.createdAt, 'yyyy-MM-dd HH:mm')}">Date</span>
        </div>
        
        <div class="inquiry-content">
            <p th:text="${inquiry.content}">Content</p>
        </div>
        
        <div th:if="${inquiry.adminResponse != null}" class="admin-response">
            <h4>관리자 답변</h4>
            <p th:text="${inquiry.adminResponse}">Admin Response</p>
            <span class="response-date" 
                  th:text="'답변일: ' + ${#temporals.format(inquiry.respondedAt, 'yyyy-MM-dd HH:mm')}">Response Date</span>
        </div>
    </div>
</div>
```

## JavaScript Patterns

### FAQ Toggle Functionality
```javascript
function toggleFAQ(element) {
    const answer = element.nextElementSibling;
    const toggle = element.querySelector('.faq-toggle');
    
    if (answer.style.display === 'none' || answer.style.display === '') {
        answer.style.display = 'block';
        toggle.textContent = '-';
        element.classList.add('active');
    } else {
        answer.style.display = 'none';
        toggle.textContent = '+';
        element.classList.remove('active');
    }
}

// FAQ search functionality
function setupFAQSearch() {
    const searchInput = document.getElementById('faqSearch');
    const faqItems = document.querySelectorAll('.faq-item');
    
    searchInput.addEventListener('input', function() {
        const searchTerm = this.value.toLowerCase();
        
        faqItems.forEach(item => {
            const question = item.querySelector('.faq-question h3').textContent.toLowerCase();
            const answer = item.querySelector('.faq-answer p').textContent.toLowerCase();
            
            if (question.includes(searchTerm) || answer.includes(searchTerm)) {
                item.style.display = 'block';
            } else {
                item.style.display = 'none';
            }
        });
    });
}
```

## Admin Interface Patterns

### Admin FAQ Management
```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/support/faqs")
public String manageFAQs(Model model) {
    List<FAQ> faqs = faqRepository.findAllOrderByDisplayOrder();
    model.addAttribute("faqs", faqs);
    return "admin/support/faq-management";
}

@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/support/inquiries")
public String manageInquiries(Model model, 
                            @RequestParam(defaultValue = "PENDING") InquiryStatus status) {
    List<SupportInquiry> inquiries = supportInquiryRepository.findByStatusOrderByCreatedAtDesc(status);
    model.addAttribute("inquiries", inquiries);
    model.addAttribute("statuses", InquiryStatus.values());
    return "admin/support/inquiry-management";
}
```

## Common Pitfalls
- ❌ **Missing Authentication**: Inquiry submission requires login, FAQ viewing is public
- ❌ **Admin Authorization**: Always check ADMIN role for management functions
- ❌ **Email Validation**: Validate contact email format in inquiry forms
- ❌ **Status Management**: Properly update inquiry status when responding
- ❌ **XSS Prevention**: Sanitize user input in inquiry content

## Integration Points
- **User Domain**: User authentication for inquiry submission
- **Notification Domain**: Admin notifications for new inquiries
- **Email Service**: Automated email responses for inquiries
- **Search**: FAQ search functionality

## Testing Patterns
```java
@ActiveProfiles("test")
class SupportServiceTest {
    // Test FAQ retrieval and filtering
    // Test inquiry creation and response flow
    // Verify admin authorization
    // Test notification integration
}
```

## Related Documentation
- [Main Claude Guidance](../../../CLAUDE.md)
- [User Domain](../user/CLAUDE.md)
- [Notification Domain](../notification/CLAUDE.md)