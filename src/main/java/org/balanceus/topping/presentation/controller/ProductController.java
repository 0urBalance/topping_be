package org.balanceus.topping.presentation.controller;

import java.util.List;
import java.util.UUID;
import java.security.Principal;

import org.balanceus.topping.application.dto.ProductRequestDto;
import org.balanceus.topping.application.dto.ProductAdjustmentDto;
import org.balanceus.topping.application.service.ImageUploadService;
import org.balanceus.topping.application.service.ProductService;
import org.balanceus.topping.application.service.StoreService;
import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.ProductWishlist;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.ProductWishlistRepository;
import org.balanceus.topping.domain.repository.UserRepository;
import org.balanceus.topping.infrastructure.response.ApiResponseData;
import org.balanceus.topping.infrastructure.security.UserDetailsImpl;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
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
	private final StoreService storeService;
	private final ImageUploadService imageUploadService;
	private final UserRepository userRepository;
	private final ProductWishlistRepository productWishlistRepository;

	@GetMapping
	public String listProducts(Model model) {
		List<Product> products = productService.getAllActiveProducts();
		model.addAttribute("products", products);
		return "products/list";
	}

	@GetMapping("/create")
	public String createProductForm(Model model, @AuthenticationPrincipal UserDetailsImpl userDetails) {
		if (userDetails == null) {
			return "redirect:/login";
		}
		
		// Check if user has business owner role
		String userRole = userDetails.getUser().getRole().name();
		if (!userRole.equals("ROLE_BUSINESS_OWNER") && !userRole.equals("ROLE_ADMIN")) {
			log.warn("User {} does not have business owner role - access denied", userDetails.getUser().getEmail());
			return "redirect:/mypage?error=access_denied";
		}
		
		// Get user's stores
		List<Store> userStores = storeService.getStoresByUser(userDetails.getUser().getUuid());
		if (userStores.isEmpty()) {
			return "redirect:/stores/register?error=no_store";
		}
		
		model.addAttribute("user", userDetails.getUser());
		model.addAttribute("userStores", userStores);
		model.addAttribute("productRequest", new ProductRequestDto());
		return "products/create";
	}

	@PostMapping("/create")
	public String createProduct(
			@Valid @ModelAttribute("productRequest") ProductRequestDto productRequest,
			BindingResult bindingResult,
			@RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
			Model model,
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			RedirectAttributes redirectAttributes) {
		
		// Validate authentication
		if (userDetails == null) {
			return "redirect:/login";
		}
		
		// Check if user has business owner role
		String userRole = userDetails.getUser().getRole().name();
		if (!userRole.equals("ROLE_BUSINESS_OWNER") && !userRole.equals("ROLE_ADMIN")) {
			log.warn("User {} does not have business owner role - access denied", userDetails.getUser().getEmail());
			return "redirect:/mypage?error=access_denied";
		}
		
		// Handle validation errors
		if (bindingResult.hasErrors()) {
			log.warn("Product creation validation failed for user: {}", userDetails.getUser().getEmail());
			// Re-populate data for form display
			List<Store> userStores = storeService.getStoresByUser(userDetails.getUser().getUuid());
			model.addAttribute("user", userDetails.getUser());
			model.addAttribute("userStores", userStores);
			return "products/create";
		}

		try {
			// Create product using service
			Product createdProduct = productService.createProduct(productRequest, userDetails.getUser().getUuid());
			
			// Handle image upload if provided
			if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
				String imagePath = imageUploadService.uploadProductImage(createdProduct, thumbnailFile);
				createdProduct.setThumbnailPath(imagePath);
				productService.updateProduct(createdProduct);
			}
			
			log.info("Product created successfully: {} by user: {}", createdProduct.getUuid(), userDetails.getUser().getEmail());
			redirectAttributes.addFlashAttribute("successMessage", "Product created successfully!");
			return "redirect:/products/" + createdProduct.getUuid();
			
		} catch (IllegalArgumentException e) {
			log.error("Product creation failed due to invalid data: {}", e.getMessage());
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			return "redirect:/products/create";
			
		} catch (Exception e) {
			log.error("Product creation failed for user: {} - {}", userDetails.getUser().getEmail(), e.getMessage());
			redirectAttributes.addFlashAttribute("errorMessage", "Product creation failed");
			return "redirect:/products/create";
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

	@GetMapping("/{id}/edit")
	public String editProductForm(@PathVariable UUID id, Model model, Principal principal) {
		log.debug("Product edit form accessed for product: {} by user: {}", id, principal != null ? principal.getName() : "anonymous");
		
		if (principal == null) {
			log.warn("Unauthenticated user attempted to access product edit form");
			return "redirect:/login";
		}
		
		try {
			// Get user for form display
			User user = userRepository.findByEmail(principal.getName())
					.orElseThrow(() -> new RuntimeException("User not found: " + principal.getName()));
					
			// Validate ownership first
			productService.validateProductOwnership(id, user.getUuid());
			
			// Get the product to edit
			Product product = productService.getProductById(id)
					.orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
			
			// Create ProductRequestDto with current values
			ProductRequestDto productRequest = new ProductRequestDto();
			productRequest.setName(product.getName());
			productRequest.setDescription(product.getDescription());
			productRequest.setPrice(product.getPrice());
			productRequest.setCategory(product.getCategory().name().toLowerCase());
			productRequest.setThumbnailPath(product.getThumbnailPath());
			productRequest.setStoreId(product.getStore().getUuid());
			
			model.addAttribute("product", product);
			model.addAttribute("productRequest", productRequest);
			model.addAttribute("user", user);
			
			return "products/edit";
			
		} catch (IllegalArgumentException e) {
			log.warn("Access denied or product not found: {}", e.getMessage());
			return "redirect:/products?error=access_denied";
		} catch (Exception e) {
			log.error("Error accessing product edit form: {}", e.getMessage());
			return "redirect:/products?error=server_error";
		}
	}

	@PostMapping("/{id}/edit")
	public String updateProduct(
			@PathVariable UUID id,
			@Valid @ModelAttribute("productRequest") ProductRequestDto productRequest,
			BindingResult bindingResult,
			Model model,
			Principal principal) {
		
		log.debug("Product update attempt for product: {} by user: {}", id, principal != null ? principal.getName() : "anonymous");
		
		// Validate authentication
		if (principal == null) {
			log.warn("Unauthenticated user attempted product update");
			return "redirect:/login";
		}
		
		try {
			// Get the product for form re-display if needed
			Product product = productService.getProductById(id)
					.orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
			
			// Handle validation errors
			if (bindingResult.hasErrors()) {
				log.warn("Product update validation failed for product: {} by user: {}", id, principal.getName());
				// Re-populate user and product for form display
				User user = userRepository.findByEmail(principal.getName())
						.orElseThrow(() -> new RuntimeException("User not found: " + principal.getName()));
				model.addAttribute("product", product);
				model.addAttribute("user", user);
				return "products/edit";
			}
			
			// Get the user UUID
			User user = userRepository.findByEmail(principal.getName())
					.orElseThrow(() -> new RuntimeException("User not found: " + principal.getName()));
					
			// Update product using service
			Product updatedProduct = productService.updateProduct(id, productRequest, user.getUuid());
			log.info("Product updated successfully: {} by user: {}", updatedProduct.getUuid(), principal.getName());
			return "redirect:/products/" + id + "?success=updated";
			
		} catch (IllegalArgumentException e) {
			log.error("Product update failed due to invalid data or access denied: {}", e.getMessage());
			return "redirect:/products?error=access_denied";
			
		} catch (Exception e) {
			log.error("Product update failed for product: {} by user: {} - {}", id, principal.getName(), e.getMessage());
			return "redirect:/products/" + id + "/edit?error=update_failed";
		}
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

	@PostMapping("/{id}/adjust")
	@ResponseBody
	public ApiResponseData<Product> adjustProduct(
			@PathVariable UUID id,
			@Valid @ModelAttribute ProductAdjustmentDto adjustmentDto,
			BindingResult bindingResult,
			Principal principal) {
		
		log.debug("Product adjustment attempt for product: {} by user: {}", id, principal != null ? principal.getName() : "anonymous");
		
		// Validate authentication
		if (principal == null) {
			log.warn("Unauthenticated user attempted product adjustment");
			return ApiResponseData.failure(401, "Authentication required");
		}
		
		// Handle validation errors
		if (bindingResult.hasErrors()) {
			log.warn("Product adjustment validation failed for product: {} by user: {}", id, principal.getName());
			return ApiResponseData.failure(400, "Validation errors: " + bindingResult.getAllErrors());
		}
		
		try {
			// Get the user UUID
			User user = userRepository.findByEmail(principal.getName())
					.orElseThrow(() -> new RuntimeException("User not found: " + principal.getName()));
			
			// Adjust product using service
			Product adjustedProduct = productService.adjustProduct(id, adjustmentDto, user.getUuid());
			log.info("Product adjusted successfully: {} by user: {}", adjustedProduct.getUuid(), principal.getName());
			
			return ApiResponseData.success(adjustedProduct);
			
		} catch (IllegalArgumentException e) {
			log.error("Product adjustment failed due to invalid data or access denied: {}", e.getMessage());
			return ApiResponseData.failure(400, e.getMessage());
			
		} catch (Exception e) {
			log.error("Product adjustment failed for product: {} by user: {} - {}", id, principal.getName(), e.getMessage());
			return ApiResponseData.failure(500, "Product adjustment failed");
		}
	}

	@PostMapping("/{id}/wishlist/toggle")
	@ResponseBody
	public ApiResponseData<String> toggleProductWishlist(
			@PathVariable UUID id,
			@AuthenticationPrincipal UserDetailsImpl userDetails) {
		
		log.debug("Product wishlist toggle for product: {} by user: {}", id, userDetails != null ? userDetails.getUser().getEmail() : "anonymous");
		
		// Validate authentication
		if (userDetails == null) {
			log.warn("Unauthenticated user attempted product wishlist toggle");
			return ApiResponseData.failure(401, "Authentication required");
		}
		
		try {
			// Get the product
			Product product = productService.getProductById(id)
					.orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
			
			User user = userDetails.getUser();
			
			// Check if already wishlisted
			boolean isWishlisted = productWishlistRepository.existsByUserAndProduct(user, product);
			
			if (isWishlisted) {
				// Remove from wishlist
				ProductWishlist existingWishlist = productWishlistRepository.findByUserAndProduct(user, product);
				if (existingWishlist != null) {
					productWishlistRepository.delete(existingWishlist);
					log.info("Product removed from wishlist: {} by user: {}", id, user.getEmail());
					return ApiResponseData.success("removed");
				}
			} else {
				// Add to wishlist
				ProductWishlist newWishlist = new ProductWishlist();
				newWishlist.setUser(user);
				newWishlist.setProduct(product);
				productWishlistRepository.save(newWishlist);
				log.info("Product added to wishlist: {} by user: {}", id, user.getEmail());
				return ApiResponseData.success("added");
			}
			
			return ApiResponseData.failure(500, "Failed to update wishlist");
			
		} catch (IllegalArgumentException e) {
			log.error("Product wishlist toggle failed due to invalid data: {}", e.getMessage());
			return ApiResponseData.failure(404, e.getMessage());
			
		} catch (Exception e) {
			log.error("Product wishlist toggle failed for product: {} by user: {} - {}", id, userDetails.getUser().getEmail(), e.getMessage());
			return ApiResponseData.failure(500, "Wishlist toggle failed");
		}
	}

	@GetMapping("/{id}/wishlist/status")
	@ResponseBody
	public ApiResponseData<Boolean> getProductWishlistStatus(
			@PathVariable UUID id,
			@AuthenticationPrincipal UserDetailsImpl userDetails) {
		
		// If not authenticated, return false
		if (userDetails == null) {
			return ApiResponseData.success(false);
		}
		
		try {
			// Get the product
			Product product = productService.getProductById(id)
					.orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
			
			User user = userDetails.getUser();
			boolean isWishlisted = productWishlistRepository.existsByUserAndProduct(user, product);
			
			return ApiResponseData.success(isWishlisted);
			
		} catch (Exception e) {
			log.error("Error checking product wishlist status for product: {} - {}", id, e.getMessage());
			return ApiResponseData.success(false);
		}
	}
}