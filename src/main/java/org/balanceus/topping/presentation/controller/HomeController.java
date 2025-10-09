package org.balanceus.topping.presentation.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.balanceus.topping.application.service.StoreService;
import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.Product.ProductCategory;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.repository.ProductRepository;
import org.balanceus.topping.domain.repository.StoreRepository;
import org.balanceus.topping.infrastructure.response.ApiResponseData;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {

	private final ProductRepository productRepository;
	private final StoreRepository storeRepository;
	private final StoreService storeService;

	@GetMapping("/")
	public String home(@RequestParam(required = false) String category, 
					   @RequestParam(required = false) String search, 
					   Model model) {
		List<Product> recentProducts;
		
		// Handle search functionality
		if (search != null && !search.trim().isEmpty()) {
			recentProducts = productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrIngredientsContainingIgnoreCaseAndIsActiveTrue(
				search, search, search);
		}
		// Handle category filter
		else if (category != null && !category.trim().isEmpty()) {
			try {
				ProductCategory productCategory = ProductCategory.valueOf(category.toUpperCase());
				recentProducts = productRepository.findByCategoryAndIsActiveTrue(productCategory);
			} catch (IllegalArgumentException e) {
				// If invalid category, show all products
				recentProducts = productRepository.findByIsActiveTrue();
			}
		}
		// Default: show all active products
		else {
			recentProducts = productRepository.findByIsActiveTrue();
		}
		
		// Limit to 8 products for display
		if (recentProducts.size() > 8) {
			recentProducts = recentProducts.subList(0, 8);
		}
		
		long storeCount = storeRepository.count();
		
		// Add attributes for the UI
		model.addAttribute("categories", ProductCategory.values());
		model.addAttribute("selectedCategory", category);
		model.addAttribute("searchQuery", search);
		model.addAttribute("recentProducts", recentProducts);
		model.addAttribute("storeCount", storeCount);
		return "home";
	}

	@GetMapping("/upgrade")
	public String showUpgradePage() {
		// TODO: Implement actual user role upgrade functionality
		// For now, redirect to mypage with info message
		return "redirect:/mypage?info=upgrade_required";
	}
	
	@GetMapping("/api/search")
	@ResponseBody
	public ResponseEntity<ApiResponseData<Map<String, Object>>> search(
			@RequestParam(required = false) String query) {
		
		try {
			Map<String, Object> results = new HashMap<>();
			
			if (query != null && !query.trim().isEmpty()) {
				String searchTerm = query.trim();
				
				// Search products
				List<Product> products = productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrIngredientsContainingIgnoreCaseAndIsActiveTrue(
					searchTerm, searchTerm, searchTerm);
				
				// Limit products to 8 for performance
				if (products.size() > 8) {
					products = products.subList(0, 8);
				}
				
				// Search stores
				List<Store> stores = storeRepository.findByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
					searchTerm, searchTerm, searchTerm);
				
				// Limit stores to 5 for performance
				if (stores.size() > 5) {
					stores = stores.subList(0, 5);
				}
				
				results.put("products", products);
				results.put("stores", stores);
				results.put("query", searchTerm);
			} else {
				// Return empty results for empty search
				results.put("products", new ArrayList<>());
				results.put("stores", new ArrayList<>());
				results.put("query", "");
			}
			
			return ResponseEntity.ok(ApiResponseData.success(results));
			
		} catch (Exception e) {
			return ResponseEntity.internalServerError()
				.body(ApiResponseData.failure(500, "검색 중 오류가 발생했습니다."));
		}
	}
}