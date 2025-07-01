package org.balanceus.topping.presentation.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.balanceus.topping.domain.model.CollaborationProduct;
import org.balanceus.topping.domain.model.CollaborationProposal;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.CollaborationProductRepository;
import org.balanceus.topping.domain.repository.CollaborationProposalRepository;
import org.balanceus.topping.domain.repository.UserRepository;
import org.balanceus.topping.infrastructure.response.ApiResponseData;
import org.balanceus.topping.infrastructure.response.Code;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/collaboration-products")
@RequiredArgsConstructor
public class CollaborationProductController {

	private final CollaborationProductRepository productRepository;
	private final CollaborationProposalRepository proposalRepository;
	private final UserRepository userRepository;

	@GetMapping("/create/{proposalId}")
	@PreAuthorize("hasRole('BUSINESS_OWNER')")
	public String createProductForm(@PathVariable UUID proposalId, Model model, Principal principal) {
		User user = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));

		CollaborationProposal proposal = proposalRepository.findById(proposalId)
				.orElseThrow(() -> new RuntimeException("Proposal not found"));

		if (!proposal.getProposer().equals(user) && 
			(proposal.getTargetBusinessOwner() == null || !proposal.getTargetBusinessOwner().equals(user))) {
			throw new RuntimeException("Access denied");
		}

		model.addAttribute("proposal", proposal);
		return "collaboration-products/create";
	}

	@PostMapping("/create")
	@PreAuthorize("hasRole('BUSINESS_OWNER')")
	@ResponseBody
	public ApiResponseData<CollaborationProduct> createProduct(
			@RequestParam UUID proposalId,
			@RequestParam String name,
			@RequestParam String description,
			@RequestParam BigDecimal price,
			@RequestParam(required = false) String imageUrl,
			@RequestParam Integer stockQuantity,
			@RequestParam(required = false) String specialOfferDescription,
			Principal principal) {

		User user = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));

		CollaborationProposal proposal = proposalRepository.findById(proposalId)
				.orElseThrow(() -> new RuntimeException("Proposal not found"));

		if (!proposal.getProposer().equals(user) && 
			(proposal.getTargetBusinessOwner() == null || !proposal.getTargetBusinessOwner().equals(user))) {
			throw new RuntimeException("Access denied");
		}

		CollaborationProduct product = new CollaborationProduct();
		product.setCollaborationProposal(proposal);
		product.setName(name);
		product.setDescription(description);
		product.setPrice(price);
		product.setImageUrl(imageUrl);
		product.setStockQuantity(stockQuantity);
		product.setSpecialOfferDescription(specialOfferDescription);
		product.setStatus(CollaborationProduct.ProductStatus.DRAFT);
		product.setAvailableFrom(LocalDateTime.now());

		CollaborationProduct saved = productRepository.save(product);
		return ApiResponseData.success(Code.SUCCESS, saved);
	}

	@GetMapping("/manage/{proposalId}")
	public String manageProducts(@PathVariable UUID proposalId, Model model, Principal principal) {
		User user = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));

		CollaborationProposal proposal = proposalRepository.findById(proposalId)
				.orElseThrow(() -> new RuntimeException("Proposal not found"));

		if (!proposal.getProposer().equals(user) && 
			(proposal.getTargetBusinessOwner() == null || !proposal.getTargetBusinessOwner().equals(user))) {
			throw new RuntimeException("Access denied");
		}

		List<CollaborationProduct> products = productRepository.findByCollaborationProposal(proposal);

		model.addAttribute("proposal", proposal);
		model.addAttribute("products", products);
		return "collaboration-products/manage";
	}

	@PostMapping("/{productId}/publish")
	@PreAuthorize("hasRole('BUSINESS_OWNER')")
	@ResponseBody
	public ApiResponseData<String> publishProduct(@PathVariable UUID productId, Principal principal) {
		CollaborationProduct product = productRepository.findById(productId)
				.orElseThrow(() -> new RuntimeException("Product not found"));

		User user = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));

		CollaborationProposal proposal = product.getCollaborationProposal();
		if (!proposal.getProposer().equals(user) && 
			(proposal.getTargetBusinessOwner() == null || !proposal.getTargetBusinessOwner().equals(user))) {
			throw new RuntimeException("Access denied");
		}

		product.setStatus(CollaborationProduct.ProductStatus.ACTIVE);
		product.setAvailableFrom(LocalDateTime.now());
		productRepository.save(product);

		proposal.setStatus(CollaborationProposal.ProposalStatus.LIVE);
		proposalRepository.save(proposal);

		return ApiResponseData.success(Code.SUCCESS, "제품이 성공적으로 출시되었습니다!");
	}

	@GetMapping("/live")
	public String liveProducts(Model model) {
		List<CollaborationProduct> liveProducts = 
				productRepository.findByStatusOrderByCreatedAtDesc(CollaborationProduct.ProductStatus.ACTIVE);

		model.addAttribute("products", liveProducts);
		return "collaboration-products/live";
	}

	@GetMapping("/{productId}")
	public String productDetail(@PathVariable UUID productId, Model model) {
		CollaborationProduct product = productRepository.findById(productId)
				.orElseThrow(() -> new RuntimeException("Product not found"));

		model.addAttribute("product", product);
		return "collaboration-products/detail";
	}
}