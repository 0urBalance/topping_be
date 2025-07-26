package org.balanceus.topping.application.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

import org.balanceus.topping.application.dto.ProductRequestDto;
import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.ProductRepository;
import org.balanceus.topping.domain.repository.StoreRepository;
import org.balanceus.topping.domain.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    /**
     * Create a new product with the authenticated user as creator
     */
    public Product createProduct(ProductRequestDto requestDto, UUID userUuid) {
        log.debug("Creating product for user: {}", userUuid);
        
        // Get authenticated user
        User creator = userRepository.findById(userUuid)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userUuid));
        
        // Get the store
        Store store = storeRepository.findById(requestDto.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("Store not found: " + requestDto.getStoreId()));
        
        // Validate store ownership
        if (!store.getUser().getUuid().equals(userUuid)) {
            throw new IllegalArgumentException("You can only create products for your own store");
        }
        
        // Validate input
        validateProductRequest(requestDto);
        
        // Create product
        Product product = new Product();
        product.setName(requestDto.getName().trim());
        product.setDescription(requestDto.getDescription() != null ? requestDto.getDescription().trim() : null);
        product.setPrice(requestDto.getPrice());
        product.setThumbnailPath(requestDto.getThumbnailPath());
        
        // Set enums based on category
        try {
            product.setProductType(Product.ProductType.valueOf(requestDto.getProductType()));
            product.setCategory(Product.ProductCategory.valueOf(requestDto.getProductCategory()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid category or product type");
        }
        
        product.setStore(store);
        product.setCreator(creator);
        product.setIsActive(true);
        product.setReviewCount(0);
        
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully: {} by user: {}", savedProduct.getUuid(), userUuid);
        
        return savedProduct;
    }

    /**
     * Get all active products
     */
    @Transactional(readOnly = true)
    public List<Product> getAllActiveProducts() {
        return productRepository.findByIsActiveTrue();
    }

    /**
     * Get product by ID
     */
    @Transactional(readOnly = true)
    public Optional<Product> getProductById(UUID id) {
        return productRepository.findById(id);
    }

    /**
     * Get products created by specific user
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsByCreator(UUID userUuid) {
        User creator = userRepository.findById(userUuid)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userUuid));
        return productRepository.findByCreator(creator);
    }

    /**
     * Get products by category
     */
    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    /**
     * Update an existing product
     */
    public Product updateProduct(Product product) {
        return productRepository.save(product);
    }
    
    /**
     * Update an existing product with DTO
     */
    public Product updateProduct(UUID id, ProductRequestDto requestDto, UUID userUuid) {
        log.debug("Updating product: {} for user: {}", id, userUuid);
        
        // Get the product to update
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        
        // Validate ownership
        validateProductOwnership(product, userUuid);
        
        // Validate input
        validateProductRequest(requestDto);
        
        // Update product fields
        product.setName(requestDto.getName().trim());
        product.setDescription(requestDto.getDescription() != null ? requestDto.getDescription().trim() : null);
        product.setPrice(requestDto.getPrice());
        product.setThumbnailPath(requestDto.getThumbnailPath());
        
        // Set enums based on category
        try {
            product.setProductType(Product.ProductType.valueOf(requestDto.getProductType()));
            product.setCategory(Product.ProductCategory.valueOf(requestDto.getProductCategory()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid category or product type");
        }
        
        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully: {} by user: {}", updatedProduct.getUuid(), userUuid);
        
        return updatedProduct;
    }

    /**
     * Validate that the user has permission to edit the product
     */
    private void validateProductOwnership(Product product, UUID userUuid) {
        if (!product.getCreator().getUuid().equals(userUuid)) {
            throw new IllegalArgumentException("Access denied: You can only edit your own products");
        }
    }

    /**
     * Validate that the user has permission to edit the product by ID
     */
    public void validateProductOwnership(UUID id, UUID userUuid) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        validateProductOwnership(product, userUuid);
    }

    /**
     * Validate product request data
     */
    private void validateProductRequest(ProductRequestDto requestDto) {
        if (requestDto.getName() == null || requestDto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        
        if (requestDto.getPrice() == null || requestDto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Product price must be greater than 0");
        }
        
        if (requestDto.getCategory() == null || requestDto.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("Product category is required");
        }
        
        if (requestDto.getStoreId() == null) {
            throw new IllegalArgumentException("Store ID is required");
        }
        
        // Validate category is one of the allowed values
        String[] allowedCategories = {"signature", "popular", "collaboration", "new"};
        boolean validCategory = false;
        for (String category : allowedCategories) {
            if (category.equals(requestDto.getCategory())) {
                validCategory = true;
                break;
            }
        }
        
        if (!validCategory) {
            throw new IllegalArgumentException("Invalid category: " + requestDto.getCategory());
        }
    }

    // Menu-related methods (migrated from MenuService)
    @Transactional(readOnly = true)
    public List<Product> getProductsByStore(Store store) {
        return productRepository.findByStore(store);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByStoreAndType(Store store, Product.ProductType productType) {
        return productRepository.findByStoreAndProductType(store, productType);
    }

    @Transactional(readOnly = true)
    public List<Product> getAvailableProductsByStore(Store store) {
        return productRepository.findByStoreAndIsAvailable(store, true);
    }

    @Transactional(readOnly = true)
    public List<Product> getCollaborationProducts(Store store) {
        return productRepository.findByStoreAndProductType(store, Product.ProductType.COLLABORATION);
    }

    @Transactional(readOnly = true)
    public List<Product> getSignatureProducts(Store store) {
        return productRepository.findByStoreAndProductType(store, Product.ProductType.SIGNATURE);
    }

    @Transactional(readOnly = true)
    public Page<Product> getProductsByTypeOrderByReviewCount(Product.ProductType productType, Pageable pageable) {
        return productRepository.findByProductTypeOrderByReviewCountDesc(productType, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> getAllProductsOrderByReviewCount(Pageable pageable) {
        return productRepository.findAllByOrderByReviewCountDesc(pageable);
    }

    public void deleteProduct(UUID productId) {
        productRepository.deleteById(productId);
    }
}