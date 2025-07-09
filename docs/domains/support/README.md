# 🎧 Support Domain

The Support Domain provides comprehensive customer support functionality including FAQ management and inquiry submission system.

## Overview

The support system enables users to:
- Browse and search FAQ content (public access)
- Submit support inquiries (authenticated users)
- Track inquiry status and responses
- Manage FAQ categories and content (admin)

## Architecture

### Domain Models

#### SupportInquiry
```java
@Entity
public class SupportInquiry extends BaseEntity {
    private String title;
    private InquiryCategory category;
    private String content;
    private User user;
    private InquiryStatus status;
    private String response;
    private LocalDateTime responseDate;
    private String attachmentFileName;
    private String attachmentPath;
}
```

**Enums:**
- `InquiryCategory`: GENERAL, ACCOUNT, COLLABORATION, PAYMENT, TECHNICAL, URGENT, FEEDBACK
- `InquiryStatus`: PENDING, IN_PROGRESS, COMPLETED, CLOSED

#### FAQ
```java
@Entity
public class FAQ extends BaseEntity {
    private String question;
    private String answer;
    private FAQCategory category;
    private boolean isActive;
    private int sortOrder;
    private int viewCount;
}
```

**Enums:**
- `FAQCategory`: GENERAL, ACCOUNT, COLLABORATION, PAYMENT, TECHNICAL

### Repository Pattern

#### Domain Layer
- `SupportInquiryRepository` - Domain interface for business logic
- `FAQRepository` - Domain interface for business logic

#### Infrastructure Layer
- `SupportInquiryJpaRepository` - JPA interface extending JpaRepository
- `FAQJpaRepository` - JPA interface extending JpaRepository
- `SupportInquiryRepositoryImpl` - Implementation connecting domain and JPA
- `FAQRepositoryImpl` - Implementation connecting domain and JPA

## API Endpoints

### Public Endpoints
```
GET  /support/cs                    - Customer support main page
GET  /support/faq/{id}              - FAQ detail view
GET  /support/api/faqs              - FAQ API for AJAX
```

### Authenticated Endpoints
```
GET  /support/inquiry-form          - Inquiry submission form
POST /support/inquiry               - Submit new inquiry
GET  /support/my-inquiries          - User's inquiry history
GET  /support/inquiry/{id}          - Inquiry detail view
```

## Templates

### Main Templates
- `support/cs.html` - Main customer support page with FAQ display
- `support/inquiry-form.html` - Inquiry submission form
- `support/inquiry-detail.html` - Individual inquiry view
- `support/my-inquiries.html` - User inquiry history
- `support/faq-detail.html` - FAQ detail view

### Features
- **Responsive Design**: Mobile-friendly interface
- **Search Functionality**: FAQ search and filtering
- **Category Filtering**: Browse FAQs by category
- **Pagination**: Efficient handling of large datasets
- **Modal Integration**: Seamless user experience

## Security Configuration

### Public Access
```java
.requestMatchers("/support/cs", "/support/faq/**").permitAll()
.requestMatchers("/support/api/faqs").permitAll()
```

### Protected Access
```java
.requestMatchers("/support/inquiry-form", "/support/inquiry", 
                "/support/my-inquiries", "/support/inquiry/**").authenticated()
```

## Key Features

### FAQ Management
- **Categorized FAQs**: Organized by business categories
- **Search Functionality**: Full-text search across questions and answers
- **View Tracking**: Popular FAQ identification
- **Sort Ordering**: Customizable FAQ display order

### Inquiry System
- **Category-based Submission**: Organized inquiry types
- **File Attachment Support**: Document and image uploads (UI ready)
- **Status Tracking**: Real-time inquiry status updates
- **Response Management**: Admin response functionality

### User Experience
- **Modern UI**: Clean, responsive design
- **Real-time Validation**: Form validation with feedback
- **Loading States**: Progress indicators for async operations
- **Error Handling**: Graceful error messaging

## Data Transfer Objects

### InquiryRequest
```java
public class InquiryRequest {
    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max = 200)
    private String title;
    
    @NotNull(message = "카테고리를 선택해주세요.")
    private SupportInquiry.InquiryCategory category;
    
    @NotBlank(message = "내용을 입력해주세요.")
    @Size(max = 5000)
    private String content;
}
```

## Controller Implementation

### SupportController
- **FAQ Display**: Paginated FAQ listing with search
- **Inquiry Management**: CRUD operations for inquiries
- **Category Filtering**: Dynamic category-based filtering
- **User Authentication**: Session-based access control

## Database Schema

### Support Inquiry Table
```sql
CREATE TABLE support_inquiry (
    id UUID PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    category VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    user_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    response TEXT,
    response_date TIMESTAMP,
    attachment_file_name VARCHAR(255),
    attachment_path VARCHAR(500),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### FAQ Table
```sql
CREATE TABLE faq (
    id UUID PRIMARY KEY,
    question VARCHAR(500) NOT NULL,
    answer TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    sort_order INTEGER DEFAULT 0,
    view_count INTEGER DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

## Integration Points

### Authentication Integration
- **Session-based Authentication**: Uses existing UserDetailsImpl
- **Role-based Access**: User role validation for protected endpoints
- **Login Redirection**: Automatic redirect for unauthenticated users

### MyPage Integration
- **Inquiry History**: Link to user's support inquiries
- **Status Updates**: Real-time inquiry status in MyPage
- **Quick Access**: Direct links to support functions

### Navigation Integration
- **Main Navigation**: Support link in primary navigation
- **Footer Integration**: FAQ and support links in footer
- **Breadcrumb Navigation**: Consistent navigation experience

## Future Enhancements

### Admin Features
- **Admin Dashboard**: Inquiry management interface
- **Response System**: Admin response functionality
- **FAQ Management**: CRUD operations for FAQ content
- **Analytics**: Support metrics and reporting

### Advanced Features
- **File Upload**: Complete file attachment implementation
- **Email Notifications**: Automated email responses
- **Priority System**: Urgent inquiry handling
- **Knowledge Base**: Extended documentation system

## Best Practices

### Development Guidelines
- **Repository Pattern**: Follow three-layer repository architecture
- **Entity Design**: Use enums for categories and status
- **Validation**: Comprehensive form validation with user feedback
- **Error Handling**: Graceful error messages and fallbacks

### Template Guidelines
- **Responsive Design**: Mobile-first approach
- **Accessibility**: Screen reader and keyboard navigation support
- **Performance**: Lazy loading and pagination for large datasets
- **Consistency**: Uniform design language across all templates

### Security Guidelines
- **Input Validation**: Server-side validation for all forms
- **CSRF Protection**: Enabled for all POST endpoints
- **Authentication**: Proper session validation for protected routes
- **Authorization**: User ownership validation for inquiry access

## Troubleshooting

### Common Issues
1. **Repository Bean Conflicts**: Ensure domain interfaces don't extend Spring Repository
2. **Template Errors**: Check Thymeleaf security integration
3. **Authentication Issues**: Verify session configuration
4. **File Upload**: Implement multipart configuration for attachments

### Performance Optimization
- **Pagination**: Implement for FAQ and inquiry lists
- **Caching**: Consider caching for frequently accessed FAQs
- **Database Indexing**: Index category and status columns
- **Template Optimization**: Minimize template complexity for large lists