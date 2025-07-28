# Troubleshooting & Common Issues

## Login & Authentication Issues

- **Session Creation**: Session-based authentication working properly
- **Fragment Rendering**: Updated to Thymeleaf 3 syntax
- **Template Errors**: All deprecated syntax updated
- **CSRF Protection**: Disabled for simplified form handling
- **Kakao Login**: Integrated with session-based authentication, error handling in place

## Entity Management

- **Optimistic Locking**: Fixed by removing manual UUID setting in SignupController
- **UUID Generation**: Let Hibernate handle @GeneratedValue @UuidGenerator entities
- **Repository Layer**: Consistent three-layer pattern across all domains

## JPA & Query Issues

- **Product Duplication**: Fixed Cartesian Product problem in StoreJpaRepository by separating multiple JOIN FETCH operations
- **Query Optimization**: Replaced problematic queries like `LEFT JOIN FETCH s.products LEFT JOIN FETCH s.images` with separate queries
- **Data Integrity**: Ensured 1:1 mapping between entities and their collections without N×M multiplication

## Thymeleaf Template Issues  

- **Field Binding Errors**: Fixed mismatched field references (e.g., `th:field="*{imageUrl}"` changed to `th:field="*{thumbnailPath}"`)
- **Null-Safety**: Implemented proper null checks for template expressions to prevent evaluation errors
- **Expression Safety**: Use `${object != null and !#strings.isEmpty(object.field)}` pattern for safe null checking

## Database & Connection Pool Issues

- **Connection Pool Exhaustion**: Fixed transaction boundary issues in KakaoService and SggCodeController
- **HikariCP Configuration**: Optimized pool size (5-30), leak detection enabled, proper timeouts
- **Transaction Management**: All service methods properly wrapped in @Transactional boundaries
- **Query Optimization**: Replaced unbounded findAll() calls with paginated queries where appropriate
- **Connection Monitoring**: Enhanced logging for connection pool stats and leak detection

## Build & Runtime

- **JAVA_HOME**: Must be set correctly for all Gradle operations
- **Dependencies**: All conflicts resolved, no version mismatches
- **Database**: Connection pool optimized with leak detection and monitoring
- **Session Config**: Persistent sessions with proper timeout configuration

## File Upload & Environment Configuration

- **Upload Path Configuration**: Use `UPLOAD_PATH` environment variable to override default paths
- **Profile-Specific Paths**: Local development (`/mnt/d/projects/topping/uploads`) vs Production (`/home/ourbalance_topping/uploads`)
- **Directory Creation**: Upload directories are created automatically with proper permissions
- **Resource Serving**: Images served via `/uploads/**` URL pattern mapped to filesystem paths
- **Security**: Path traversal protection and validation for upload/delete operations

## Store Registration & Multipart Processing

- **Three-Phase Registration**: Resolved multipart parsing errors by separating store creation from image upload
- **Architectural Solution**: Store registration → image setup page → completion flow prevents chicken-and-egg problems
- **Phase 1**: Basic store information registration without multipart handling
- **Phase 2**: Dedicated image setup page (`/stores/setup-images`) with existing upload API
- **Phase 3**: Completion with optional skip functionality
- **Debugging Infrastructure**: Comprehensive test suite with H2 database proving multipart functionality works correctly
- **Error Resolution**: Moved from servlet-level multipart configuration attempts to architectural separation of concerns