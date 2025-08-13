package org.balanceus.topping.presentation.controller;

import java.util.List;

import org.balanceus.topping.domain.model.CollaborationProposal;
import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.repository.CollaborationProposalRepository;
import org.balanceus.topping.domain.repository.ProductRepository;
import org.balanceus.topping.domain.repository.StoreRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/explore")
@RequiredArgsConstructor
public class ExploreController {

	private final ProductRepository productRepository;
	private final CollaborationProposalRepository proposalRepository;
	private final StoreRepository storeRepository;

	@GetMapping
	public String explore(Model model) {
		// Limit results for better performance
		Pageable pageable = PageRequest.of(0, 12);
		
		// Get stores data
		List<Store> stores = storeRepository.findAll(pageable);
		
		// Get popular/recent products
		List<Product> popularProducts = productRepository.findByProductTypeOrderByReviewCountDesc(
				Product.ProductType.SIGNATURE, pageable).getContent();
		List<Product> collaborationProducts = productRepository.findByProductTypeOrderByReviewCountDesc(
				Product.ProductType.COLLABORATION, pageable).getContent();
		
		// Original collaboration data
		List<Product> products = productRepository.findByIsActiveTrue();
		List<CollaborationProposal> proposals = proposalRepository.findByStatus(
				CollaborationProposal.CollaborationStatus.PENDING);
		// CollaborationProduct entity removed - using Product.COLLABORATION type instead
		Pageable pageable2 = PageRequest.of(0, 10);
		List<Product> liveProducts = productRepository.findByProductTypeOrderByReviewCountDesc(Product.ProductType.COLLABORATION, pageable2).getContent();

		// Add new data for store and product sections
		model.addAttribute("stores", stores);
		model.addAttribute("popularMenus", popularProducts);
		model.addAttribute("collaborationMenus", collaborationProducts);
		
		// Keep original data for backward compatibility
		model.addAttribute("products", products);
		model.addAttribute("proposals", proposals);
		model.addAttribute("liveProducts", liveProducts);

		return "explore";
	}
}