# Topping Platform Documentation

This directory contains comprehensive documentation for the Topping collaboration platform.

## Quick Navigation

### 🔧 Development Guides
- **[CLAUDE.md](../CLAUDE.md)** - Primary development guide and project overview
- **[Session Persistence Troubleshooting](./SESSION_PERSISTENCE_TROUBLESHOOTING.md)** - Authentication troubleshooting guide

### 🏗️ Architecture Documentation

#### Domain-Specific Documentation
- **[🔐 Authentication](./domains/auth/README.md)** - Login/logout, session management
- **[👤 User Management](./domains/user/README.md)** - User accounts and roles  
- **[🤝 Collaboration](./domains/collaboration/README.md)** - Business matching and proposals
- **[💬 Chat System](./domains/chat/README.md)** - Real-time messaging
- **[📦 Product Management](./domains/product/README.md)** - Product listings and features
- **[🔔 Notification System](./domains/notification/README.md)** - Event-driven alerts

### 📋 Migration Documentation
- **[Documentation Migration](./DOCUMENTATION_MIGRATION.md)** - Migration status and process

## Documentation Standards

### File Organization
```
docs/
├── README.md                                    # This index file
├── SESSION_PERSISTENCE_TROUBLESHOOTING.md      # Technical troubleshooting guides
├── DOCUMENTATION_MIGRATION.md                  # Migration documentation
└── domains/                                    # Domain-specific documentation
    ├── auth/README.md                          # Authentication domain
    ├── user/README.md                          # User management domain
    ├── collaboration/README.md                 # Collaboration domain
    ├── chat/README.md                          # Chat domain
    ├── product/README.md                       # Product domain
    └── notification/README.md                  # Notification domain
```

### Documentation Types

1. **Technical Guides** - Step-by-step implementation and troubleshooting
2. **Domain Documentation** - Business logic and architectural details
3. **API Documentation** - Endpoint specifications and usage
4. **Migration Guides** - Process documentation for major changes

## Key Information

### Authentication System
- **Method**: Session-based authentication with Spring Security
- **Session Cookie**: JSESSIONID with HttpOnly flag
- **Route Protection**: All feature routes require authentication by default
- **Template Integration**: Thymeleaf security with `sec:authorize="isAuthenticated()"`

### Recent Major Changes
- ✅ **JWT to Session Migration**: Completed migration from JWT to session-based auth
- ✅ **Session Persistence Fix**: Resolved authentication persistence issues across protected routes
- ✅ **CSRF Protection**: Configured for form-based auth, disabled for API endpoints
- ✅ **Route Security**: Implemented secure-by-default authorization rules

### Development Environment
- **Java**: JDK 17 (included in project at `/mnt/d/projects/topping/jdk-17.0.12+7`)
- **Build Tool**: Gradle with wrapper
- **Database**: PostgreSQL (production), H2 (testing)
- **Framework**: Spring Boot 3.5.3

## Getting Help

### Common Issues
1. **Authentication Problems**: See [Session Persistence Troubleshooting](./SESSION_PERSISTENCE_TROUBLESHOOTING.md)
2. **Build Issues**: Check JAVA_HOME setting in [CLAUDE.md](../CLAUDE.md)
3. **Database Issues**: Verify environment variables and connection pool settings

### Debug Resources
- **Enhanced Logging**: Configured for authentication and security debugging
- **Session Status API**: `POST /api/session/status` for authentication verification
- **Developer Tools**: Browser network/application tabs for cookie inspection

For project setup and development workflow, always start with **[CLAUDE.md](../CLAUDE.md)**.