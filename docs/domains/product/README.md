# Product Domain

## Purpose and Core Responsibilities

The Product domain manages product listings, creation, editing, and product-related operations within the Topping collaboration platform. This domain enables users to showcase their products and services for potential collaborations with other businesses.

## Key Models/Entities

### Product Entity
- **Location**: `src/main/java/org/balanceus/topping/domain/model/Product.java`
- **Primary Key**: UUID
- **Core Fields**:
  - `title` - Product name/title ⚠️ **Important**: uses `title`, not `name`
  - `description` - Detailed product description
  - `category` - Product category classification
  - `imageUrl` - Product image URL (optional)
  - `creator` - Many-to-one relationship with User (product owner)
  - `createdAt` - Timestamp of product creation
  - `isActive` - Boolean flag for product availability

### Product Request DTO
- **Location**: `src/main/java/org/balanceus/topping/application/dto/ProductRequestDto.java`
- **Purpose**: Data transfer object for product creation and updates
- **Validation**: Includes validation annotations for required fields

## Main APIs and Endpoints

### Web Endpoints
- **GET `/products`** - List all active products
- **GET `/products/create`** - Display product creation form ⚠️ **Not** `/products/register`
- **POST `/products/create`** - Process product creation
- **GET `/products/{id}`** - Display product details
- **GET `/products/{id}/edit`** - Display product edit form
- **POST `/products/{id}/edit`** - Process product updates

### API Endpoints
- **GET `/products/api`** - Get all active products (JSON response)
- **GET `/products/api/{id}`** - Get specific product (JSON response)

### MyPage Integration
- **GET `/mypage/product`** - Product management from MyPage interface

## Repository Pattern

### Domain Repository
- **Interface**: `src/main/java/org/balanceus/topping/domain/repository/ProductRepository.java`
- **Key Methods**:
  - `findByIsActiveTrue()` - Get all active products
  - `findByCreator(User creator)` - Get products by creator
  - `findByCategory(String category)` - Get products by category

### JPA Repository
- **Interface**: `src/main/java/org/balanceus/topping/infrastructure/persistence/ProductJpaRepository.java`
- **Extends**: `JpaRepository<Product, UUID>`

### Implementation
- **Class**: `src/main/java/org/balanceus/topping/infrastructure/persistence/ProductRepositoryImpl.java`
- **Implements**: Domain repository using JPA repository

## Business Rules and Constraints

### Access Control
- **Creation**: Authenticated users can create products
- **Ownership**: Only product creators can edit their products
- **Viewing**: All users can view active products

### Validation Rules
- **Required Fields**: title, description, category
- **Optional Fields**: imageUrl
- **Length Constraints**: Title and description have maximum length limits
- **URL Validation**: imageUrl must be valid URL format if provided

### Business Logic
- **Active Status**: Only active products are shown in public listings
- **Ownership Validation**: Service layer validates product ownership before updates
- **Creator Assignment**: Product creator is automatically set from authenticated user
- **UUID Generation**: Automatic UUID assignment for unique identification

## Integration Points

### User Domain
- **Relationship**: Many-to-one with User entity (creator)
- **Authentication**: Product operations require authenticated session

### Collaboration Domain
- **Connection**: Products can be targets or proposals in collaborations
- **Collaboration Applications**: Products are referenced in collaboration requests
- **Business Matching**: Products serve as basis for business partnerships

### Store Domain
- **Integration**: Store owners can create products for their stores
- **Navigation**: Store management includes product creation actions

### MyPage Integration
- **Product Management**: Accessible via MyPage product section
- **Statistics**: Product count displayed in MyPage dashboard
- **Quick Actions**: Direct product creation from MyPage

## Templates and Views

### Product Templates
- **Creation**: `src/main/resources/templates/products/create.html`
- **Product List**: `src/main/resources/templates/products/list.html`
- **Product Details**: `src/main/resources/templates/products/detail.html`
- **Product Edit**: `src/main/resources/templates/products/edit.html`

### MyPage Templates
- **Product Management**: `src/main/resources/templates/mypage/product.html`

## Service Layer

### ProductService
- **Location**: `src/main/java/org/balanceus/topping/application/service/ProductService.java`
- **Key Methods**:
  - `createProduct(ProductRequestDto, String userEmail)` - Create new product
  - `updateProduct(UUID, ProductRequestDto, String userEmail)` - Update existing product
  - `getProductById(UUID)` - Retrieve product by ID
  - `getAllActiveProducts()` - Get all active products
  - `getProductsByCreator(String userEmail)` - Get user's products
  - `validateProductOwnership(UUID, String userEmail)` - Validate ownership

## Critical Implementation Notes

### ⚠️ Template Field Reference
**IMPORTANT**: Product entity uses `title` field, NOT `name`
- **Correct**: `${product.title}`, `${myApp.applicantProduct.title}`
- **Incorrect**: `${product.name}`, `${myApp.applicantProduct.name}`
- **Error Result**: Thymeleaf runtime errors if using `.name`

### ⚠️ Routing Endpoints
**IMPORTANT**: Product creation endpoint is `/create`, NOT `/register`
- **Correct**: `/products/create`
- **Incorrect**: `/products/register` (causes UUID parsing errors)

## Recent Updates and Fixes

### Template Field Name Resolution (Fixed)
- ✅ Fixed all template references from `product.name` to `product.title`
- ✅ Updated MyPage templates: applications.html, ongoing.html, received.html, collabos.html
- ✅ Resolved Thymeleaf runtime errors related to field access

### Routing Issue Resolution (Fixed)
- ✅ Fixed routing conflict between `/products/register` and `/products/{id}`
- ✅ Updated all template links to use correct `/products/create` endpoint
- ✅ Ensured consistent product creation URL across all templates

### MyPage Integration Enhancement (Completed)
- ✅ Enhanced product management interface in MyPage
- ✅ Added product statistics and quick creation actions
- ✅ Improved navigation between MyPage and product management features