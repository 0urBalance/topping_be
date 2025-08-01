# Development Workflow

## Adding New Features

1. **Domain Design**: Follow clean architecture principles
2. **Repository Pattern**: Implement three-layer pattern
3. **Transaction Boundaries**: Ensure all service methods have @Transactional annotations
4. **Controller Layer**: Use proper response wrappers and @Transactional(readOnly = true) for read operations
5. **Template Integration**: Follow UI/UX guidelines
6. **Security**: Implement proper authentication/authorization
7. **Testing**: Write comprehensive tests with test profile
8. **Connection Safety**: Avoid unbounded queries like findAll() - use pagination when needed
9. **Environment Configuration**: Use profile-specific properties and environment variables for external configuration

## UI/UX Development

1. **MANDATORY**: Use the Topping CSS Framework for ALL new templates
2. **CSS Architecture**: Use `base.css` or `auth.css` - NEVER use Bootstrap or multiple CSS files
3. **JavaScript**: Use `common.js` or `auth-common.js` - avoid inline scripts and external dependencies
4. **Fragments**: Always use Thymeleaf fragments for navbar, footer, and modals
5. **Design Review**: Check existing patterns and brand guidelines
6. **Responsive Design**: Mobile-first approach with CSS Variables system
7. **Accessibility**: Proper labels, keyboard navigation, ARIA attributes, screen reader support
8. **Message UI**: Use `.bubble.mine` for own messages (right-aligned, `#6B3410`) and `.bubble.their` for others (left-aligned, light gray)
9. **Badge System**: Implement unread badges with `.unread-badge` class, auto-hide on interaction
10. **Performance**: Optimize CSS/JS, minimize requests
11. **Browser Testing**: Cross-browser compatibility

## Frontend Refactoring Best Practices

1. **Component-Based Architecture**: Create reusable fragments for repeated UI elements
2. **CSS Optimization**: Use design tokens, utility classes, and eliminate inline styles
3. **JavaScript Performance**: Implement event delegation and lazy loading patterns
4. **Visual Consistency**: Apply consistent spacing, scaling, and centering across components
5. **Responsive Refinements**: Ensure proper layout scaling and spacing on all devices

## Code Quality

- **Clean Code**: Follow existing patterns and conventions
- **Error Handling**: Proper exception handling with user-friendly messages
- **Security**: Always validate inputs, protect against common vulnerabilities
- **Transaction Safety**: All database operations must be within @Transactional boundaries
- **Connection Management**: Monitor for connection leaks using HikariCP leak detection
- **Documentation**: Update relevant documentation when adding features
- **Environment Configuration**: Externalize configuration using profiles and environment variables for deployment flexibility