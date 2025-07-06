package org.balanceus.topping.application.service;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

import org.balanceus.topping.application.dto.ProductRequestDto;
import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.ProductRepository;
import org.balanceus.topping.domain.repository.UserRepository;
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
    private final UserRepository userRepository;

    /**
     * Create a new product with the authenticated user as creator
     */
    public Product createProduct(ProductRequestDto requestDto, String userEmail) {
        log.debug("Creating product for user: {}", userEmail);
        
        // Get authenticated user
        User creator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));
        
        // Validate input
        validateProductRequest(requestDto);
        
        // Create product
        Product product = new Product();
        product.setTitle(requestDto.getTitle().trim());
        product.setDescription(requestDto.getDescription().trim());
        product.setCategory(requestDto.getCategory());
        product.setImageUrl(requestDto.getImageUrl() != null && !requestDto.getImageUrl().trim().isEmpty() 
                ? requestDto.getImageUrl().trim() : null);
        product.setCreator(creator);
        product.setIsActive(true);
        
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully: {} by user: {}", savedProduct.getUuid(), userEmail);
        
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
    public List<Product> getProductsByCreator(String userEmail) {
        User creator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));
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
     * Validate product request data
     */
    private void validateProductRequest(ProductRequestDto requestDto) {
        if (requestDto.getTitle() == null || requestDto.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Product title is required");
        }
        
        if (requestDto.getDescription() == null || requestDto.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Product description is required");
        }
        
        if (requestDto.getCategory() == null || requestDto.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("Product category is required");
        }
        
        // Validate category is one of the allowed values
        String[] allowedCategories = {"웹 개발", "모바일 앱", "게임 개발", "디자인", "마케팅", "기타"};
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
}