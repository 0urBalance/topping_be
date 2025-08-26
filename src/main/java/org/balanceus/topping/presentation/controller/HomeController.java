package org.balanceus.topping.presentation.controller;

import java.util.List;

import org.balanceus.topping.application.service.StoreService;
import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.Product.ProductCategory;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.repository.ProductRepository;
import org.balanceus.topping.domain.repository.StoreRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {

	private final ProductRepository productRepository;
	private final StoreRepository storeRepository;
	private final StoreService storeService;

	@GetMapping("/")
	public String home(@RequestParam(required = false) String category, Model model) {
		// Get products based on category filter
		List<Product> recentProducts;
		if (category != null && !category.trim().isEmpty()) {
			try {
				ProductCategory productCategory = ProductCategory.valueOf(category.toUpperCase());
				recentProducts = productRepository.findByCategoryAndIsActiveTrue(productCategory);
			} catch (IllegalArgumentException e) {
				// If invalid category, show all products
				recentProducts = productRepository.findByIsActiveTrue();
			}
		} else {
			recentProducts = productRepository.findByIsActiveTrue();
		}
		
		// Limit to 8 products for display
		if (recentProducts.size() > 8) {
			recentProducts = recentProducts.subList(0, 8);
		}
		
		long storeCount = storeRepository.count();
		
		// Add category information for the filter UI
		model.addAttribute("categories", ProductCategory.values());
		model.addAttribute("selectedCategory", category);
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
}