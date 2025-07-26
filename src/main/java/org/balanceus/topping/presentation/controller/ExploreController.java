package org.balanceus.topping.presentation.controller;

import java.util.List;

import org.balanceus.topping.domain.model.CollaborationProduct;
import org.balanceus.topping.domain.model.CollaborationProposal;
import org.balanceus.topping.domain.model.Menu;
import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.repository.CollaborationProductRepository;
import org.balanceus.topping.domain.repository.CollaborationProposalRepository;
import org.balanceus.topping.domain.repository.MenuRepository;
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
	private final CollaborationProductRepository collaborationProductRepository;
	private final StoreRepository storeRepository;
	private final MenuRepository menuRepository;

	@GetMapping
	public String explore(Model model) {
		// Limit results for better performance
		Pageable pageable = PageRequest.of(0, 12);
		
		// Get stores data
		List<Store> stores = storeRepository.findAll(pageable);
		
		// Get popular/recent menus
		List<Menu> popularMenus = menuRepository.findByMenuTypeOrderByReviewCountDesc(
				Menu.MenuType.SIGNATURE, pageable).getContent();
		List<Menu> collaborationMenus = menuRepository.findByMenuTypeOrderByReviewCountDesc(
				Menu.MenuType.COLLABORATION, pageable).getContent();
		
		// Original collaboration data
		List<Product> products = productRepository.findByIsActiveTrue();
		List<CollaborationProposal> proposals = proposalRepository.findByStatus(
				CollaborationProposal.ProposalStatus.PENDING);
		List<CollaborationProduct> liveProducts = collaborationProductRepository
				.findByStatus(CollaborationProduct.ProductStatus.ACTIVE);

		// Add new data for store and menu sections
		model.addAttribute("stores", stores);
		model.addAttribute("popularMenus", popularMenus);
		model.addAttribute("collaborationMenus", collaborationMenus);
		
		// Keep original data for backward compatibility
		model.addAttribute("products", products);
		model.addAttribute("proposals", proposals);
		model.addAttribute("liveProducts", liveProducts);

		return "explore";
	}
}