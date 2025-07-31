package org.balanceus.topping.presentation.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.security.Principal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.balanceus.topping.domain.model.Collaboration;
import org.balanceus.topping.domain.model.CollaborationProposal;
import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.model.Collaboration.CollaborationStatus;
import org.balanceus.topping.domain.model.CollaborationProposal.ProposalStatus;
import org.balanceus.topping.domain.repository.CollaborationRepository;
import org.balanceus.topping.domain.repository.CollaborationProposalRepository;
import org.balanceus.topping.domain.repository.ProductRepository;
import org.balanceus.topping.domain.repository.StoreRepository;
import org.balanceus.topping.domain.repository.UserRepository;
import org.balanceus.topping.application.service.ProductService;
import org.balanceus.topping.application.service.CollaborationService;
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

@Controller
@RequestMapping("/collaborations")
@RequiredArgsConstructor
public class CollaborationController {

	private final CollaborationRepository collaborationRepository;
	private final CollaborationProposalRepository collaborationProposalRepository;
	private final ProductRepository productRepository;
	private final StoreRepository storeRepository;
	private final UserRepository userRepository;
	private final ProductService productService;
	private final CollaborationService collaborationService;
	private final ObjectMapper objectMapper;

	@GetMapping
	public String listCollaborations(Model model) {
		List<Collaboration> collaborations = collaborationRepository.findAll();
		model.addAttribute("collaborations", collaborations);
		return "collaborations/list";
	}

	@GetMapping("/apply")
	public String applyForm(@RequestParam(required = false) UUID productId, 
	                       @RequestParam(required = false) UUID storeId, 
	                       Model model, Principal principal) {
		// Check authentication
		if (principal == null) {
			return "redirect:/login?error=authentication_required";
		}
		
		// Get the authenticated user
		User user = userRepository.findByEmail(principal.getName()).orElse(null);
		if (user == null) {
			return "redirect:/login?error=user_not_found";
		}
		
		// Add user to model for role-based rendering
		model.addAttribute("user", user);
		model.addAttribute("isBusinessOwner", user.getRole().name().equals("ROLE_BUSINESS_OWNER"));
		
		// Load data based on user role
		if (user.getRole().name().equals("ROLE_BUSINESS_OWNER")) {
			return handleBusinessOwnerForm(model, user);
		} else {
			return handleUserForm(productId, storeId, model, user);
		}
	}

	private String handleBusinessOwnerForm(Model model, User user) {
		// Get business owner's store
		Optional<Store> userStoreOpt = storeRepository.findByUser(user);
		if (userStoreOpt.isEmpty()) {
			return "redirect:/stores/register?error=no_store_registered";
		}
		
		Store userStore = userStoreOpt.get();
		List<Product> userProducts = productService.getProductsByCreator(user.getUuid());
		List<Store> allStores = storeRepository.findAll(org.springframework.data.domain.PageRequest.of(0, 100))
			.stream()
			.filter(store -> !store.getUuid().equals(userStore.getUuid()))
			.toList();
		
		// Generate JSON for store-product mapping
		String storeDataJson = generateStoreDataJson(allStores);
		
		model.addAttribute("userStore", userStore);
		model.addAttribute("userProducts", userProducts);
		model.addAttribute("allStores", allStores);
		model.addAttribute("allProducts", productRepository.findAll());
		model.addAttribute("storeDataJson", storeDataJson);
		
		return "collaborations/apply";
	}
	
	private String handleUserForm(UUID productId, UUID storeId, Model model, User user) {
		// Load all stores and products for selection
		List<Store> allStores = storeRepository.findAll(org.springframework.data.domain.PageRequest.of(0, 100));
		List<Product> allProducts = productRepository.findAll();
		List<Product> userProducts = productService.getProductsByCreator(user.getUuid());
		
		// Generate JSON for store-product mapping
		String storeDataJson = generateStoreDataJson(allStores);
		
		// If specific target provided, set it
		if (storeId != null) {
			Store targetStore = storeRepository.findById(storeId).orElse(null);
			if (targetStore != null) {
				model.addAttribute("targetStore", targetStore);
			}
		}
		
		if (productId != null) {
			Product targetProduct = productRepository.findById(productId).orElse(null);
			if (targetProduct != null) {
				model.addAttribute("targetProduct", targetProduct);
			}
		}
		
		model.addAttribute("allStores", allStores);
		model.addAttribute("allProducts", allProducts);
		model.addAttribute("userProducts", userProducts);
		model.addAttribute("storeDataJson", storeDataJson);
		
		return "collaborations/apply";
	}
	
	private String generateStoreDataJson(List<Store> stores) {
		try {
			Map<String, Object> storeData = stores.stream()
				.collect(Collectors.toMap(
					store -> store.getUuid().toString(),
					store -> {
						List<Product> products = productRepository.findByStore(store);
						return Map.of(
							"uuid", store.getUuid().toString(),
							"name", store.getName(),
							"category", store.getCategory(),
							"products", products.stream()
								.filter(Product::getIsAvailable)
								.map(product -> Map.of(
									"uuid", product.getUuid().toString(),
									"name", product.getName(),
									"category", product.getCategory() != null ? product.getCategory().toString() : "",
									"productType", product.getProductType() != null ? product.getProductType().toString() : "",
									"isAvailable", product.getIsAvailable(),
									"price", product.getPrice() != null ? product.getPrice() : 0
								))
								.collect(Collectors.toList())
						);
					}
				));
			
			return objectMapper.writeValueAsString(storeData);
		} catch (JsonProcessingException e) {
			// Log error and return empty object
			System.err.println("Error generating store data JSON: " + e.getMessage());
			return "{}";
		}
	}

	@GetMapping("/apply/{productId}")
	public String applyFormLegacy(@PathVariable UUID productId, Model model, Principal principal) {
		// Legacy support for product-based collaboration requests
		return applyForm(productId, null, model, principal);
	}

	@PostMapping("/apply")
	public String applyCollaboration(
			@RequestParam(required = false) UUID sourceStoreId,
			@RequestParam(required = false) UUID targetStoreId,
			@RequestParam(required = false) UUID sourceProductId,
			@RequestParam(required = false) UUID targetProductId,
			@RequestParam String collaborationTitle,
			@RequestParam String description,
			@RequestParam String startDate,
			@RequestParam String endDate,
			@RequestParam(required = false) String category,
			Principal principal) {
		
		// Check authentication
		if (principal == null) {
			return "redirect:/login?error=authentication_required";
		}
		
		// Get authenticated user
		User applicant = userRepository.findByEmail(principal.getName()).orElse(null);
		if (applicant == null) {
			return "redirect:/login?error=user_not_found";
		}
		
		// Validate required fields
		if (collaborationTitle == null || collaborationTitle.trim().isEmpty()) {
			return "redirect:/collaborations/apply?error=title_required";
		}
		
		if (description == null || description.trim().isEmpty()) {
			return "redirect:/collaborations/apply?error=description_required";
		}
		
		if (targetStoreId == null) {
			return "redirect:/collaborations/apply?error=target_store_required";
		}
		
		// Validate dates
		java.time.LocalDateTime startDateTime;
		java.time.LocalDateTime endDateTime;
		try {
			java.time.LocalDate start = java.time.LocalDate.parse(startDate);
			java.time.LocalDate end = java.time.LocalDate.parse(endDate);
			
			if (end.isBefore(start) || end.isEqual(start)) {
				return "redirect:/collaborations/apply?error=invalid_date_range";
			}
			
			// Convert to LocalDateTime (start of day)
			startDateTime = start.atStartOfDay();
			endDateTime = end.atStartOfDay();
			
		} catch (Exception e) {
			return "redirect:/collaborations/apply?error=invalid_date_format";
		}
		
		// Get target store and its owner
		Store targetStore = storeRepository.findById(targetStoreId).orElse(null);
		if (targetStore == null) {
			return "redirect:/collaborations/apply?error=target_store_not_found";
		}
		
		User targetBusinessOwner = targetStore.getUser();
		if (targetBusinessOwner == null) {
			return "redirect:/collaborations/apply?error=store_owner_not_found";
		}
		
		// Create CollaborationProposal entity
		CollaborationProposal proposal = new CollaborationProposal();
		proposal.setTitle(collaborationTitle);
		proposal.setDescription(description);
		proposal.setCategory(category);
		proposal.setProposer(applicant);
		proposal.setTargetBusinessOwner(targetBusinessOwner);
		proposal.setStartDate(startDateTime);
		proposal.setEndDate(endDateTime);
		proposal.setStatus(ProposalStatus.PENDING);
		proposal.setTrendScore(0); // Initialize trend score
		
		// Save the proposal
		collaborationProposalRepository.save(proposal);
		
		return "redirect:/mypage/applications?success=proposal_submitted";
	}

	private String handleProductCollaborationSubmission(UUID productId, UUID applicantProductId, String message, User applicant) {
		// Get target product
		Product product = productRepository.findById(productId).orElse(null);
		if (product == null) {
			return "redirect:/products?error=product_not_found";
		}
		
		// Prevent self-application
		if (product.getCreator().getUuid().equals(applicant.getUuid())) {
			return "redirect:/products/" + productId + "?error=cannot_apply_own_product";
		}
		
		// Get applicant's product if provided
		Product applicantProduct = null;
		if (applicantProductId != null) {
			applicantProduct = productRepository.findById(applicantProductId).orElse(null);
			// Validate that the product belongs to the applicant
			if (applicantProduct == null || !applicantProduct.getCreator().getUuid().equals(applicant.getUuid())) {
				return "redirect:/collaborations/apply/" + productId + "?error=invalid_applicant_product";
			}
		}
		
		// Validate message
		if (message == null || message.trim().isEmpty()) {
			return "redirect:/collaborations/apply/" + productId + "?error=message_required";
		}

		// Create collaboration
		Collaboration collaboration = new Collaboration();
		collaboration.setProduct(product);
		collaboration.setApplicant(applicant);
		collaboration.setApplicantProduct(applicantProduct);
		collaboration.setMessage(message.trim());
		collaboration.setStatus(CollaborationStatus.PENDING);

		collaborationRepository.save(collaboration);
		return "redirect:/collaborations?success=application_submitted";
	}

	@PostMapping("/{id}/accept")
	public String acceptCollaboration(@PathVariable UUID id, Principal principal) {
		if (principal == null) {
			return "redirect:/login?error=authentication_required";
		}
		
		User currentUser = userRepository.findByEmail(principal.getName()).orElse(null);
		if (currentUser == null) {
			return "redirect:/login?error=user_not_found";
		}
		
		Collaboration collaboration = collaborationRepository.findById(id).orElse(null);
		if (collaboration == null) {
			return "redirect:/mypage/received?error=collaboration_not_found";
		}
		
		// Verify that the current user is the product owner
		if (!collaboration.getProduct().getCreator().getUuid().equals(currentUser.getUuid())) {
			return "redirect:/mypage/received?error=unauthorized_action";
		}
		
		// Check if already processed
		if (collaboration.getStatus() != CollaborationStatus.PENDING) {
			return "redirect:/mypage/received?error=already_processed";
		}
		
		// Use service layer for collaboration acceptance with automatic chat room creation
		collaborationService.acceptCollaboration(id);
		
		return "redirect:/mypage/received?success=collaboration_accepted";
	}

	@PostMapping("/{id}/reject")
	public String rejectCollaboration(@PathVariable UUID id, Principal principal) {
		if (principal == null) {
			return "redirect:/login?error=authentication_required";
		}
		
		User currentUser = userRepository.findByEmail(principal.getName()).orElse(null);
		if (currentUser == null) {
			return "redirect:/login?error=user_not_found";
		}
		
		Collaboration collaboration = collaborationRepository.findById(id).orElse(null);
		if (collaboration == null) {
			return "redirect:/mypage/received?error=collaboration_not_found";
		}
		
		// Verify that the current user is the product owner
		if (!collaboration.getProduct().getCreator().getUuid().equals(currentUser.getUuid())) {
			return "redirect:/mypage/received?error=unauthorized_action";
		}
		
		// Check if already processed
		if (collaboration.getStatus() != CollaborationStatus.PENDING) {
			return "redirect:/mypage/received?error=already_processed";
		}
		
		// Use service layer for collaboration rejection
		collaborationService.rejectCollaboration(id);
		
		return "redirect:/mypage/received?success=collaboration_rejected";
	}

	@GetMapping("/api")
	@ResponseBody
	public ApiResponseData<List<Collaboration>> getCollaborationsApi() {
		List<Collaboration> collaborations = collaborationRepository.findAll();
		return ApiResponseData.success(collaborations);
	}

	@GetMapping("/api/user/{userId}")
	@ResponseBody
	public ApiResponseData<List<Collaboration>> getUserCollaborationsApi(@PathVariable UUID userId) {
		User user = userRepository.findById(userId).orElse(null);
		if (user == null) {
			return ApiResponseData.failure(404, "User not found");
		}
		List<Collaboration> collaborations = collaborationRepository.findByApplicant(user);
		return ApiResponseData.success(collaborations);
	}
}