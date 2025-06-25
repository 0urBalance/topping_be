# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
Topping (토핑) is a collaboration matching platform backend built with Spring Boot. The project uses a clean architecture approach with domain-driven design principles.

## Build & Development Commands

### Build & Run
- `./gradlew build` - Build the project
- `./gradlew bootRun` - Run the Spring Boot application
- `./gradlew test` - Run all tests
- `./gradlew test --tests ClassName` - Run specific test class
- `./gradlew test --tests ClassName.methodName` - Run specific test method

### Database
- PostgreSQL database configured via environment variables
- Required env vars: `DB_URL`, `DB_USER`, `DB_PASSWORD`
- Uses spring-dotenv for environment configuration
- Create `.env` file in project root with database credentials

## Architecture

### Package Structure
- `domain/` - Core business logic and entities
  - `model/` - JPA entities (User with UUID primary keys)
  - `repository/` - Repository interfaces
- `infrastructure/` - Technical implementation details
  - `persistence/` - JPA repository implementations
  - `security/` - Spring Security configuration (Role-based auth)
  - `response/` - Standardized API response wrappers
  - `exception/` - Global exception handling
- `presentation/` - REST controllers and DTOs
- `application/` - Application services and DTOs

### Key Components
- **ApiResponseData**: Standardized response wrapper with code/message/data structure
- **GlobalExceptionHandler**: Centralized exception handling with custom BaseException support
- **User Entity**: Uses UUID as primary key with Role-based authentication
- **Spring Security**: Configured for role-based access control

### Dependencies
- Spring Boot 3.5.3 with Java 17
- Spring Data JPA + PostgreSQL
- Spring Security
- Lombok for boilerplate reduction
- Thymeleaf for server-side rendering