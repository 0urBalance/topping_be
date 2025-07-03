package org.balanceus.topping.presentation.controller;

import java.util.List;

import org.balanceus.topping.domain.model.CollaborationProduct;
import org.balanceus.topping.domain.model.CollaborationProposal;
import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.repository.CollaborationProductRepository;
import org.balanceus.topping.domain.repository.CollaborationProposalRepository;
import org.balanceus.topping.domain.repository.ProductRepository;
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

	@GetMapping
	public String explore(Model model) {
		List<Product> products = productRepository.findByIsActiveTrue();
		List<CollaborationProposal> proposals = proposalRepository.findByStatus(
				CollaborationProposal.ProposalStatus.PENDING);
		List<CollaborationProduct> liveProducts = collaborationProductRepository
				.findByStatus(CollaborationProduct.ProductStatus.ACTIVE);

		model.addAttribute("products", products);
		model.addAttribute("proposals", proposals);
		model.addAttribute("liveProducts", liveProducts);

		return "explore";
	}
}