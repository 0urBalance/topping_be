package org.balanceus.topping.presentation.controller;

import java.util.List;
import java.util.UUID;

import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.ProductRepository;
import org.balanceus.topping.domain.repository.UserRepository;
import org.balanceus.topping.infrastructure.response.ApiResponseData;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import java.security.Principal;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductRepository productRepository;
	private final UserRepository userRepository;

	@GetMapping
	public String listProducts(Model model) {
		List<Product> products = productRepository.findByIsActiveTrue();
		model.addAttribute("products", products);
		return "products/list";
	}

	@GetMapping("/create")
	public String createProductForm(Model model, Principal principal) {
		// Spring Security ensures principal is not null
		model.addAttribute("product", new Product());
		return "products/create";
	}

	@PostMapping("/create")
	public String createProduct(
			@RequestParam String title,
			@RequestParam String description,
			@RequestParam String category,
			@RequestParam String imageUrl,
			Principal principal) {
		
		// Get creator from authenticated user instead of form parameter
		User creator = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));
		if (creator == null) {
			return "redirect:/products/create?error=user_not_found";
		}

		Product product = new Product();
		product.setTitle(title);
		product.setDescription(description);
		product.setCategory(category);
		product.setImageUrl(imageUrl);
		product.setCreator(creator);
		product.setIsActive(true);

		productRepository.save(product);
		return "redirect:/products";
	}

	@GetMapping("/{id}")
	public String viewProduct(@PathVariable UUID id, Model model) {
		Product product = productRepository.findById(id).orElse(null);
		if (product == null) {
			return "redirect:/products";
		}
		model.addAttribute("product", product);
		return "products/detail";
	}

	@GetMapping("/api")
	@ResponseBody
	public ApiResponseData<List<Product>> getProductsApi() {
		List<Product> products = productRepository.findByIsActiveTrue();
		return ApiResponseData.success(products);
	}

	@GetMapping("/api/{id}")
	@ResponseBody
	public ApiResponseData<Product> getProductApi(@PathVariable UUID id) {
		Product product = productRepository.findById(id).orElse(null);
		if (product == null) {
			return ApiResponseData.failure(404, "Product not found");
		}
		return ApiResponseData.success(product);
	}
}