# Product Domain - Claude Guidance

## Overview
The Product domain handles product listings, collaboration features, and product-based matching for the Topping platform.

## Core Entities
- **Product** - Main product entity with UUID primary key
- **ProductImage** - Multiple images per product with ordering
- **Wishlist** - User product favorites
- **Review** - Product reviews and ratings

## Key Patterns

### Product Entity Structure
```java
@Entity
public class Product {
    @Id @GeneratedValue private UUID uuid;
    private String title; // ⚠️ Use 'title', NOT 'name'
    private String description;
    @ManyToOne private Store store;
    @OneToMany(mappedBy = "product") private List<ProductImage> productImages;
    private ProductType type; // COLLABORATION, INDIVIDUAL
}
```

### Repository Pattern
- **Domain Interface**: `ProductRepository`
- **JPA Interface**: `ProductJpaRepository extends JpaRepository<Product, UUID>`
- **Implementation**: `ProductRepositoryImpl implements ProductRepository`

## Product Routes & Controllers

### Route Patterns
```java
// ✅ Correct routes
@GetMapping("/products/create")     // Product creation form
@PostMapping("/products")           // Product creation
@GetMapping("/products/{id}/edit")  // Product edit form
@PutMapping("/products/{id}")       // Product update

// ❌ Avoid these patterns
// /products/register (use /products/create)
```

### Controller Patterns
```java
@Controller
@RequestMapping("/products")
public class ProductController {
    
    @GetMapping("/create")
    @PreAuthorize("hasRole('BUSINESS_OWNER') or hasRole('ADMIN')")
    public String createProductForm(Model model, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        // Verify user has a store before allowing product creation
        Store userStore = storeService.findByOwner(userDetails.getUser());
        if (userStore == null) {
            return "redirect:/stores/register";
        }
        model.addAttribute("productForm", new ProductRequestDto());
        return "products/create";
    }
}
```

## Multi-Image Management

### ProductImage Entity
```java
@Entity
public class ProductImage {
    @Id @GeneratedValue private UUID uuid;
    @ManyToOne private Product product;
    private String imagePath;
    private String originalFileName;
    private int displayOrder;
}
```

### Image Upload Integration
```java
// Service pattern for product images
public Product createProductWithImages(ProductRequestDto request, List<MultipartFile> images, User owner) {
    Product product = createProduct(request, owner);
    
    if (images != null && !images.isEmpty()) {
        for (int i = 0; i < images.size(); i++) {
            ProductImage productImage = imageUploadService.processProductImage(images.get(i), product, i);
            productImageRepository.save(productImage);
        }
    }
    return product;
}
```

## Template Patterns

### Product Field References
```html
<!-- ✅ Correct field references -->
<h1 th:text="${product.title}">Product Title</h1>
<p th:text="${product.description}">Product Description</p>

<!-- ❌ Incorrect field references -->
<!-- <h1 th:text="${product.name}"> --> <!-- Field doesn't exist -->
```

### Product Card Fragment
```html
<!-- Fragment: fragments/product-card.html -->
<div th:fragment="product-card(product)" class="product-card">
    <div class="product-image">
        <img th:if="${product.productImages != null and !product.productImages.empty}"
             th:src="@{'/uploads/' + ${product.productImages[0].imagePath}}"
             th:alt="${product.title}" />
        <div th:unless="${product.productImages != null and !product.productImages.empty}" 
             class="no-image-placeholder">
            이미지 없음
        </div>
    </div>
    <div class="product-info">
        <h3 th:text="${product.title}">Product Title</h3>
        <p th:text="${product.store.name}">Store Name</p>
    </div>
</div>
```

### Product Forms
```html
<!-- Product creation/edit form -->
<form th:object="${productForm}" method="post" enctype="multipart/form-data">
    <input th:field="*{title}" type="text" placeholder="상품명" required />
    <textarea th:field="*{description}" placeholder="상품 설명"></textarea>
    <select th:field="*{type}">
        <option value="INDIVIDUAL">개별 상품</option>
        <option value="COLLABORATION">콜라보 상품</option>
    </select>
    <input type="file" name="images" multiple accept="image/*" />
</form>
```

## Collaboration Integration

### Product-Collaboration Relationship
```java
// Product can be part of collaborations
@Entity
public class Collaboration {
    @ManyToOne private Product initiatorProduct;
    @ManyToOne private Product partnerProduct; // Can be null for service collaborations
}

// Service method for collaboration products
public List<Product> getCollaborationProducts(User user) {
    return productRepository.findByStoreOwnerAndType(user, ProductType.COLLABORATION);
}
```

### Template Collaboration Logic
```html
<!-- Conditional product display in collaborations -->
<div th:if="${collaboration.partnerProduct != null}">
    <!-- Product-based collaboration -->
    <div class="collaboration-products">
        <div th:replace="~{fragments/product-card :: product-card(${collaboration.initiatorProduct})}"></div>
        <div th:replace="~{fragments/product-card :: product-card(${collaboration.partnerProduct})}"></div>
    </div>
</div>
<div th:unless="${collaboration.partnerProduct != null}">
    <!-- Service-based collaboration -->
    <div th:replace="~{fragments/product-card :: product-card(${collaboration.initiatorProduct})}"></div>
</div>
```

## Query Optimization

### Avoiding Cartesian Product
```java
// ❌ Problematic - multiple JOIN FETCH causes Cartesian Product
@Query("SELECT p FROM Product p " +
       "LEFT JOIN FETCH p.productImages " +
       "LEFT JOIN FETCH p.store " +
       "LEFT JOIN FETCH p.reviews")

// ✅ Correct - separate queries or single JOIN FETCH
@Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.productImages WHERE p.store = :store")
```

## Common Pitfalls
- ❌ **Product Field**: Use `product.title`, NOT `product.name` in templates
- ❌ **Product Routes**: Use `/products/create`, NOT `/products/register`
- ❌ **Store Verification**: Always verify user has a store before product creation
- ❌ **Image Null Safety**: Check `productImages != null and !productImages.empty`
- ❌ **Cartesian Product**: Avoid multiple JOIN FETCH in single query

## API Response Patterns
```java
// Product list with pagination
@GetMapping("/api/products")
public ResponseEntity<ApiResponseData<List<ProductSummaryResponse>>> getProducts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    Page<Product> products = productService.getProducts(PageRequest.of(page, size));
    return ResponseEntity.ok(ApiResponseData.success(products.getContent()));
}
```

## Integration Points
- **Store Domain**: Products belong to stores, require business owner role
- **Collaboration Domain**: Products can be part of collaboration proposals
- **Image Upload**: Multi-image management system
- **Wishlist**: User favorites and product discovery

## Testing Patterns
```java
@ActiveProfiles("test")
class ProductServiceTest {
    // Test product creation with images
    // Verify store ownership requirements
    // Test collaboration product filtering
}
```

## Related Documentation
- [Main Claude Guidance](../../../CLAUDE.md)
- [Store Domain](../store/CLAUDE.md)
- [Collaboration Domain](../collaboration/CLAUDE.md)
- [Multi-Image Upload System](../../technical/image-upload.md)