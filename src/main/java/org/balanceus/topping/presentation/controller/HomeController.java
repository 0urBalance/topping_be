package org.balanceus.topping.presentation.controller;

import java.util.List;

import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {

	private final ProductRepository productRepository;

	@GetMapping("/")
	public String home(Model model) {
		List<Product> recentProducts = productRepository.findByIsActiveTrue();
		if (recentProducts.size() > 6) {
			recentProducts = recentProducts.subList(0, 6);
		}
		model.addAttribute("recentProducts", recentProducts);
		return "home";
	}
}