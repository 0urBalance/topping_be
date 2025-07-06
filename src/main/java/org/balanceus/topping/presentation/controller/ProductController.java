package org.balanceus.topping.presentation.controller;

import java.util.List;
import java.util.UUID;
import java.security.Principal;

import org.balanceus.topping.application.dto.ProductRequestDto;
import org.balanceus.topping.application.service.ProductService;
import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.UserRepository;
import org.balanceus.topping.infrastructure.response.ApiResponseData;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.validation.BindingResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

	private final ProductService productService;
	private final UserRepository userRepository;

	@GetMapping
	public String listProducts(Model model) {
		List<Product> products = productService.getAllActiveProducts();
		model.addAttribute("products", products);
		return "products/list";
	}

	@GetMapping("/create")
	public String createProductForm(Model model, Principal principal) {
		log.debug("Product creation form accessed by: {}", principal != null ? principal.getName() : "anonymous");
		
		if (principal == null) {
			log.warn("Unauthenticated user attempted to access product creation form");
			return "redirect:/login";
		}
		
		// Verify user exists in database
		User user = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found: " + principal.getName()));
		
		model.addAttribute("user", user);
		model.addAttribute("productRequest", new ProductRequestDto());
		return "products/create";
	}

	@PostMapping("/create")
	public String createProduct(
			@Valid @ModelAttribute("productRequest") ProductRequestDto productRequest,
			BindingResult bindingResult,
			Model model,
			Principal principal) {
		
		log.debug("Product creation attempt by: {}", principal != null ? principal.getName() : "anonymous");
		
		// Validate authentication
		if (principal == null) {
			log.warn("Unauthenticated user attempted product creation");
			return "redirect:/login";
		}
		
		// Handle validation errors
		if (bindingResult.hasErrors()) {
			log.warn("Product creation validation failed for user: {}", principal.getName());
			// Re-populate user for form display
			User user = userRepository.findByEmail(principal.getName())
					.orElseThrow(() -> new RuntimeException("User not found: " + principal.getName()));
			model.addAttribute("user", user);
			return "products/create";
		}

		try {
			// Create product using service with authenticated user
			Product createdProduct = productService.createProduct(productRequest, principal.getName());
			log.info("Product created successfully: {} by user: {}", createdProduct.getUuid(), principal.getName());
			return "redirect:/products?success=created";
			
		} catch (IllegalArgumentException e) {
			log.error("Product creation failed due to invalid data: {}", e.getMessage());
			return "redirect:/products/create?error=invalid_data";
			
		} catch (Exception e) {
			log.error("Product creation failed for user: {} - {}", principal.getName(), e.getMessage());
			return "redirect:/products/create?error=save_failed";
		}
	}

	@GetMapping("/{id}")
	public String viewProduct(@PathVariable UUID id, Model model) {
		Product product = productService.getProductById(id).orElse(null);
		if (product == null) {
			log.warn("Product not found: {}", id);
			return "redirect:/products";
		}
		model.addAttribute("product", product);
		return "products/detail";
	}

	@GetMapping("/api")
	@ResponseBody
	public ApiResponseData<List<Product>> getProductsApi() {
		List<Product> products = productService.getAllActiveProducts();
		return ApiResponseData.success(products);
	}

	@GetMapping("/api/{id}")
	@ResponseBody
	public ApiResponseData<Product> getProductApi(@PathVariable UUID id) {
		Product product = productService.getProductById(id).orElse(null);
		if (product == null) {
			return ApiResponseData.failure(404, "Product not found");
		}
		return ApiResponseData.success(product);
	}
}