# Store Domain

## Purpose and Core Responsibilities

The Store domain manages business store registration, profiles, and store-related operations within the Topping collaboration platform. This domain enables business owners to create and manage their physical or virtual stores, providing a foundation for business-to-business collaborations.

## Key Models/Entities

### Store Entity
- **Location**: `src/main/java/org/balanceus/topping/domain/model/Store.java`
- **Primary Key**: UUID
- **Core Fields**:
  - `name` - Store name/title
  - `address` - Physical or business address
  - `contactNumber` - Contact phone number
  - `businessHours` - Operating hours description
  - `category` - Store category (Cafe, Restaurant, etc.)
  - `mainImageUrl` - Store main image URL (optional)
  - `snsOrWebsiteLink` - Social media or website link (optional)
  - `user` - One-to-one relationship with User (store owner)

### Store Registration DTO
- **Location**: `src/main/java/org/balanceus/topping/application/dto/StoreRegistrationRequest.java`
- **Purpose**: Data transfer object for store registration and updates
- **Validation**: Includes validation annotations for required fields

## Main APIs and Endpoints

### Web Endpoints
- **GET `/stores/register`** - Display store registration form
- **POST `/stores/register`** - Process store registration
- **GET `/stores/my-store`** - Display current user's store details
- **GET `/stores/edit`** - Display store edit form
- **POST `/stores/edit`** - Process store updates

### API Endpoints
- **POST `/stores/api/register`** - Register store via API (returns JSON)

### MyPage Integration
- **GET `/mypage/store`** - Store management from MyPage interface

## Repository Pattern

### Domain Repository
- **Interface**: `src/main/java/org/balanceus/topping/domain/repository/StoreRepository.java`
- **Key Methods**:
  - `findByUser(User user)` - Find store by owner
  - `existsByUser(User user)` - Check if user has a store

### JPA Repository
- **Interface**: `src/main/java/org/balanceus/topping/infrastructure/persistence/StoreJpaRepository.java`
- **Extends**: `JpaRepository<Store, UUID>`

### Implementation
- **Class**: `src/main/java/org/balanceus/topping/infrastructure/persistence/StoreRepositoryImpl.java`
- **Implements**: Domain repository using JPA repository

## Business Rules and Constraints

### Access Control
- **Role Requirement**: Only users with `ROLE_BUSINESS_OWNER` or `ROLE_ADMIN` can manage stores
- **Ownership**: One store per user (one-to-one relationship)
- **Authentication**: All store operations require authenticated session

### Validation Rules
- **Required Fields**: name, address, contactNumber, businessHours, category
- **Optional Fields**: mainImageUrl, snsOrWebsiteLink
- **Phone Format**: Recommended pattern `010-XXXX-XXXX`
- **URL Validation**: mainImageUrl and snsOrWebsiteLink must be valid URLs if provided

### Store Categories
Available categories:
- Cafe, Dessert, Restaurant, Bakery
- Korean, Western, Asian, Fast Food
- Bar, Other

### Business Logic
- **Single Store Policy**: Users can only have one store
- **Update Restrictions**: Only store owner can modify their store
- **Redirect Logic**: 
  - If user has no store → redirect to registration
  - If user has store → redirect to store details
  - Access denied for non-business users → redirect with error message

## Integration Points

### User Domain
- **Relationship**: One-to-one with User entity
- **Role Integration**: Validates business owner role before store operations

### Product Domain
- **Connection**: Store owners can create and manage products
- **Navigation**: Store management page includes "Register Product" action

### MyPage Integration
- **Access**: Store management accessible via MyPage dashboard
- **Role-based Display**: Store card shows different states based on user role
- **Navigation Flow**: MyPage → Store Management → Store Details/Registration

## Templates and Views

### Store Templates
- **Registration**: `src/main/resources/templates/store/register.html`
- **Store Details**: `src/main/resources/templates/store/my-store.html`
- **Store Edit**: `src/main/resources/templates/store/edit.html`

### Navigation Features
- **Breadcrumbs**: Clear navigation path in all store templates
- **Back Links**: Return to MyPage functionality
- **Action Buttons**: Edit store, register products, view collaborations

## Error Handling

### Common Error Scenarios
- **Access Denied**: Non-business users attempting store operations
- **Store Not Found**: Accessing store operations without registered store
- **Validation Errors**: Invalid data in registration/update forms
- **Duplicate Registration**: Attempting to register multiple stores

### Error Responses
- **Web**: Redirect with error parameters and flash messages
- **API**: JSON response with error codes and messages
- **Template**: Display validation errors and success messages

## Recent Updates

### Role-Based Access Control Enhancement
- Added comprehensive role validation across all store endpoints
- Implemented access denied handling with user-friendly error messages
- Enhanced MyPage integration with role-based store card visibility

### Template Integration
- Fixed routing issues between MyPage and store management
- Added consistent navigation elements across store templates
- Implemented upgrade prompts for non-business users