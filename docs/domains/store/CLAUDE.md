# Store Domain - Claude Guidance

## Overview
The Store domain manages business store registration, profiles, and image management for the Topping platform.

## Core Entities
- **Store** - Main store entity with UUID primary key
- **StoreImage** - Multiple images per store with ordering
- **StoreLike** - User favorites/likes for stores
- **SggCode** - Administrative region codes

## Key Patterns

### Three-Phase Store Registration
The store registration follows a three-phase architectural pattern:

1. **Phase 1**: Basic store information (name, description, location)
2. **Phase 2**: Store images upload via multipart form
3. **Phase 3**: Final setup and activation

```java
// Controller pattern
@PostMapping("/stores/register")
public String registerStore(@ModelAttribute StoreForm storeForm, 
                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
    // Create store entity first
    Store store = storeService.createStore(storeForm, userDetails.getUser());
    return "redirect:/stores/" + store.getUuid() + "/setup-images";
}
```

### Repository Pattern
- **Domain Interface**: `StoreRepository`
- **JPA Interface**: `StoreJpaRepository extends JpaRepository<Store, UUID>`
- **Implementation**: `StoreRepositoryImpl implements StoreRepository`

## Multi-Image Management

### StoreImage Entity
```java
@Entity
public class StoreImage {
    @Id @GeneratedValue private UUID uuid;
    @ManyToOne private Store store;
    private String imagePath;
    private String originalFileName;
    private int displayOrder; // For image ordering
}
```

### Image Upload Patterns
```java
// Service layer - batch image processing
public void saveStoreImages(UUID storeId, List<MultipartFile> images) {
    Store store = storeRepository.findById(storeId);
    for (int i = 0; i < images.size(); i++) {
        StoreImage storeImage = imageUploadService.processStoreImage(images.get(i), store, i);
        storeImageRepository.save(storeImage);
    }
}
```

### Template Patterns
```html
<!-- Store image gallery -->
<div class="image-gallery">
    <div th:each="image, iterStat : ${store.storeImages}" 
         th:class="'gallery-item' + (${iterStat.first} ? ' active' : '')">
        <img th:src="@{'/uploads/' + ${image.imagePath}}" 
             th:alt="${store.name + ' 이미지 ' + ${iterStat.count}}" />
    </div>
</div>
```

## Authorization Patterns

### Business Owner Access Control
```java
// Only business owners can register stores
@PreAuthorize("hasRole('BUSINESS_OWNER') or hasRole('ADMIN')")
@GetMapping("/stores/register")

// Store ownership verification
public boolean isStoreOwner(UUID storeId, User user) {
    Store store = storeRepository.findById(storeId);
    return store.getOwner().equals(user) || user.getRole() == Role.ADMIN;
}
```

### Template Authorization
```html
<!-- Show store management only to owners -->
<div sec:authorize="hasRole('BUSINESS_OWNER')" 
     th:if="${store.owner.uuid == #authentication.principal.user.uuid}">
    <a th:href="@{'/stores/' + ${store.uuid} + '/edit'}">가게 수정</a>
</div>
```

## Store Detail Page Patterns

### Two-Column Layout
```html
<!-- Main content + sidebar layout -->
<div class="store-detail-container">
    <div class="store-main-content">
        <!-- Store info, images, products -->
    </div>
    <div class="store-sidebar">
        <!-- SNS links, collaboration info -->
    </div>
</div>
```

### Product Integration
```java
// Store-Product relationship
@OneToMany(mappedBy = "store", fetch = FetchType.LAZY)
private List<Product> products;

// Avoid Cartesian Product - use separate queries
Store store = storeRepository.findById(storeId);
List<Product> products = productRepository.findByStoreOrderByCreatedAtDesc(store);
```

## Common Pitfalls
- ❌ **Direct Image Upload**: Use ImageUploadService, not direct file operations
- ❌ **Missing Authorization**: Always check store ownership before modifications
- ❌ **Cartesian Product**: Avoid multiple JOIN FETCH in single query
- ❌ **Manual UUID**: Never manually set UUID for @GeneratedValue entities
- ❌ **Null Safety**: Check store.getStoreImages() != null before iteration

## API Response Patterns
```java
// Store data with images
@GetMapping("/api/stores/{storeId}")
public ResponseEntity<ApiResponseData<StoreDetailResponse>> getStore(@PathVariable UUID storeId) {
    StoreDetailResponse response = storeService.getStoreDetail(storeId);
    return ResponseEntity.ok(ApiResponseData.success(response));
}
```

## Integration Points
- **User Domain**: Store ownership and business owner roles
- **Product Domain**: Products belong to stores
- **Collaboration Domain**: Store-based collaboration proposals
- **Image Upload**: Multi-image management system

## Testing Patterns
```java
@ActiveProfiles("test")
class StoreServiceTest {
    // Mock MultipartFile for image upload tests
    // Test store ownership validation
    // Verify three-phase registration flow
}
```

## Related Documentation
- [Main Claude Guidance](../../../CLAUDE.md)
- [Multi-Image Upload System](../../technical/image-upload.md)
- [Three-Phase Registration Solution](../../troubleshooting/THREE_PHASE_REGISTRATION_SOLUTION.md)
- [Product Domain](../product/CLAUDE.md)