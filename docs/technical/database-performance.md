# Database & Performance Configuration

## HikariCP Connection Pool

- **Pool Size**: 5 minimum idle, 30 maximum connections
- **Timeouts**: 30s connection timeout, 10min idle timeout, 30min max lifetime
- **Leak Detection**: 60s threshold to identify connection leaks
- **Validation**: 5s validation timeout for connection health checks
- **Performance**: Optimized for high concurrency and connection reuse

## Async Thread Pool Configuration

- **Configuration Class**: `AsyncConfig.java` with `@EnableAsync` annotation
- **Thread Pool Settings**: 5 core, 10 max threads, 50 queue capacity
- **Thread Naming**: `custom-executor-` prefix for easy identification
- **Rejection Policy**: `ThreadPoolExecutor.DiscardOldestPolicy()` for handling overflow

## Usage Pattern

- **Annotation**: `@Async("customExecutor")` for method-level async execution
- **Service Integration**: Available for use in any Spring-managed component
- **Error Handling**: Proper initialization and resource management
- **Performance**: Optimized for concurrent image processing and background tasks

## Transaction Management

- **Service Layer**: All service classes properly annotated with `@Transactional`
- **Repository Layer**: Uses three-layer pattern without transactions (correct design)
- **Controller Layer**: Read-only transactions for controller methods accessing repositories
- **Connection Safety**: All database queries execute within proper transaction boundaries

## Database & Connection Pool Issues

- **Connection Pool Exhaustion**: Fixed transaction boundary issues in KakaoService and SggCodeController
- **HikariCP Configuration**: Optimized pool size (5-30), leak detection enabled, proper timeouts
- **Transaction Management**: All service methods properly wrapped in @Transactional boundaries
- **Query Optimization**: Replaced unbounded findAll() calls with paginated queries where appropriate
- **Connection Monitoring**: Enhanced logging for connection pool stats and leak detection

## JPA & Query Issues

- **Product Duplication**: Fixed Cartesian Product problem in StoreJpaRepository by separating multiple JOIN FETCH operations
- **Query Optimization**: Replaced problematic queries like `LEFT JOIN FETCH s.products LEFT JOIN FETCH s.images` with separate queries
- **Data Integrity**: Ensured 1:1 mapping between entities and their collections without N×M multiplication
- **JPA JOIN FETCH**: Avoid multiple JOIN FETCH in single query - causes Cartesian Product and data duplication