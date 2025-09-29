package org.balanceus.topping.presentation.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.balanceus.topping.application.service.ImageUploadService;
import org.balanceus.topping.domain.model.CollaborationProposal;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.ProposalSource;
import org.balanceus.topping.domain.repository.StoreRepository;
import org.balanceus.topping.domain.repository.ProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.balanceus.topping.domain.repository.CollaborationProposalRepository;
import org.balanceus.topping.domain.repository.UserRepository;
import org.balanceus.topping.infrastructure.response.ApiResponseData;
import org.balanceus.topping.infrastructure.response.Code;
import org.balanceus.topping.infrastructure.security.Role;
import org.balanceus.topping.infrastructure.service.NotificationService;
import org.balanceus.topping.application.service.ChatService;
import org.balanceus.topping.application.service.CollaborationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/proposals")
@RequiredArgsConstructor
public class CollaborationProposalController {

	private final CollaborationProposalRepository proposalRepository;
	private final UserRepository userRepository;
	private final StoreRepository storeRepository;
	private final ProductRepository productRepository;
	private final NotificationService notificationService;
	private final ChatService chatService;
	private final CollaborationService collaborationService;
	private final ImageUploadService imageUploadService;

	@GetMapping("/suggest")
	public String suggestForm(Model model, Principal principal) {
		// Get all stores for selection, excluding user's own store if they have one
		List<Store> allStores = storeRepository.findAll(Pageable.unpaged());
		List<Product> userProducts = List.of();
		
		// If user is logged in, exclude their own store from the list and get their products
		if (principal != null) {
			User user = userRepository.findByEmail(principal.getName()).orElse(null);
			if (user != null) {
				Store userStore = storeRepository.findByUser(user).orElse(null);
				if (userStore != null) {
					allStores = allStores.stream()
							.filter(store -> !store.getUuid().equals(userStore.getUuid()))
							.toList();
					userProducts = productRepository.findByStore(userStore);
				}
			}
		}
		
		model.addAttribute("stores", allStores);
		model.addAttribute("userProducts", userProducts);
		return "proposals/suggest";
	}
	
	@GetMapping("/api/stores/{storeId}/products")
	@ResponseBody
	public ApiResponseData<List<Product>> getStoreProducts(@PathVariable UUID storeId) {
		Store store = storeRepository.findById(storeId)
				.orElseThrow(() -> new RuntimeException("Store not found"));
		
		List<Product> products = productRepository.findByStore(store);
		return ApiResponseData.success(products);
	}

	@PostMapping("/suggest")
	@ResponseBody
	public ApiResponseData<CollaborationProposal> createProposal(
			@RequestParam String title,
			@RequestParam String description,
			@RequestParam String category,
			@RequestParam(required = false) String revenueSharePreference,
			@RequestParam String targetStoreId,
			@RequestParam(required = false) String targetProductId,
			@RequestParam(required = false) String proposerProductId,
			Principal principal) {

		// Convert string parameters to UUIDs, handling empty strings as null
		UUID targetStoreUuid = parseUUID(targetStoreId, "Target store ID is required");
		UUID targetProductUuid = parseUUID(targetProductId, null);
		UUID proposerProductUuid = parseUUID(proposerProductId, null);

		User proposer = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));

		// Validation: Check that target store exists
		Store targetStore = storeRepository.findById(targetStoreUuid)
				.orElseThrow(() -> new RuntimeException("Target store not found"));
		
		// Get proposer's store
		Store proposerStore = storeRepository.findByUser(proposer).orElse(null);
		
		// Validation: Prevent self-collaboration
		if (proposerStore != null && proposerStore.getUuid().equals(targetStoreUuid)) {
			throw new RuntimeException("Cannot propose collaboration with your own store");
		}
		
		// Validation: Check target product if provided
		Product targetProduct = null;
		if (targetProductUuid != null) {
			targetProduct = productRepository.findById(targetProductUuid)
					.orElseThrow(() -> new RuntimeException("Target product not found"));
			
			// Verify that the target product belongs to the target store
			if (!targetProduct.getStore().getUuid().equals(targetStoreUuid)) {
				throw new RuntimeException("Target product does not belong to the selected target store");
			}
		}
		
		// Validation: Check proposer product if provided
		Product proposerProduct = null;
		if (proposerProductUuid != null) {
			proposerProduct = productRepository.findById(proposerProductUuid)
					.orElseThrow(() -> new RuntimeException("Proposer product not found"));
			
			// Verify that the proposer product belongs to the proposer's store
			if (proposerStore == null || !proposerProduct.getStore().getUuid().equals(proposerStore.getUuid())) {
				throw new RuntimeException("Proposer product does not belong to your store");
			}
		}

		CollaborationProposal proposal = new CollaborationProposal();
		proposal.setTitle(title);
		proposal.setDescription(description);
		proposal.setProposerUser(proposer);
		proposal.setSource(ProposalSource.CUSTOMER);
		proposal.setStatus(CollaborationProposal.CollaborationStatus.PENDING);
		
		// Set proposer's store and product
		if (proposerStore != null) {
			proposal.setProposerStore(proposerStore);
		}
		if (proposerProduct != null) {
			proposal.setProposerProduct(proposerProduct);
			System.out.println("✅ Set proposer product: " + proposerProduct.getUuid() + " (" + proposerProduct.getName() + ")");
		} else {
			System.out.println("⚠️ No proposer product provided in /proposals/suggest");
		}
		
		// Set target store and product
		proposal.setTargetStore(targetStore);
		if (targetProduct != null) {
			proposal.setTargetProduct(targetProduct);
			System.out.println("✅ Set target product: " + targetProduct.getUuid() + " (" + targetProduct.getName() + ")");
		} else {
			System.out.println("⚠️ No target product provided in /proposals/suggest");
		}

		CollaborationProposal saved = proposalRepository.save(proposal);
		
		// 🎯 NEW: Create chat room immediately for the proposal
		try {
			chatService.createChatRoomForCollaborationProposal(saved.getUuid());
			// Log successful chat room creation
			System.out.println("✅ Chat room created for proposal: " + saved.getUuid());
		} catch (Exception e) {
			// Log error but don't fail the proposal submission
			System.err.println("❌ Failed to create chat room for proposal " + saved.getUuid() + ": " + e.getMessage());
		}
		
		// 사업자들에게 새 제안 알림 전송
		notificationService.notifyBusinessOwnersOfNewProposal(saved);
		
		return ApiResponseData.success(saved);
	}
	
	private UUID parseUUID(String uuidString, String errorMessage) {
		if (uuidString == null || uuidString.trim().isEmpty()) {
			if (errorMessage != null) {
				throw new RuntimeException(errorMessage);
			}
			return null;
		}
		try {
			return UUID.fromString(uuidString.trim());
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Invalid UUID format: " + uuidString);
		}
	}

	@GetMapping("/mypage")
	public String myPage(Model model, Principal principal) {
		User user = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));

		List<CollaborationProposal> myProposals = proposalRepository.findByProposerUser(user);
		model.addAttribute("proposals", myProposals);
		return "proposals/mypage";
	}

	@GetMapping("/dashboard")
	@PreAuthorize("hasRole('BUSINESS_OWNER')")
	public String businessDashboard(Model model, Principal principal) {
		User businessOwner = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));

		List<CollaborationProposal> pendingProposals = 
				proposalRepository.findByStatusOrderByCreatedAtDesc(CollaborationProposal.CollaborationStatus.PENDING);
		
		// Target business owner proposals handled through targetStore relationship
		List<CollaborationProposal> myTargetedProposals = List.of(); // TODO: Implement with targetStore query

		model.addAttribute("pendingProposals", pendingProposals);
		model.addAttribute("myTargetedProposals", myTargetedProposals);
		return "proposals/business-dashboard";
	}

	@PostMapping("/{proposalId}/accept")
	@PreAuthorize("hasRole('BUSINESS_OWNER')")
	@ResponseBody
	public ApiResponseData<String> acceptProposal(
			@PathVariable UUID proposalId,
			Principal principal) {

		try {
			// Use CollaborationService to properly approve the proposal
			// This will create the Collaboration entity and set collaboration_uuid
			collaborationService.approve(proposalId);
			
			return ApiResponseData.success("협업 제안이 수락되었습니다.");
		} catch (Exception e) {
			return ApiResponseData.failure(Code.INTERNAL_SERVER_ERROR.getCode(), "제안 수락 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
		}
	}

	@PostMapping("/{proposalId}/reject")
	@PreAuthorize("hasRole('BUSINESS_OWNER')")
	@ResponseBody
	public ApiResponseData<String> rejectProposal(
			@PathVariable UUID proposalId,
			Principal principal) {

		User businessOwner = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));

		CollaborationProposal proposal = proposalRepository.findById(proposalId)
				.orElseThrow(() -> new RuntimeException("Proposal not found"));

		proposal.setStatus(CollaborationProposal.CollaborationStatus.REJECTED);
		// Target business owner handled through store relationship
		proposalRepository.save(proposal);

		// 제안자에게 거절 알림 전송
		notificationService.notifyProposalRejected(proposal);

		return ApiResponseData.success("협업 제안이 거절되었습니다.");
	}

	@PostMapping("/{proposalId}/accept/form")
	@PreAuthorize("hasRole('BUSINESS_OWNER')")
	public String acceptProposalForm(
			@PathVariable UUID proposalId,
			Principal principal) {

		if (principal == null) {
			return "redirect:/login?error=authentication_required";
		}
		
		User businessOwner = userRepository.findByEmail(principal.getName()).orElse(null);
		if (businessOwner == null) {
			return "redirect:/login?error=user_not_found";
		}

		try {
			// Use CollaborationService to properly approve the proposal
			// This will create the Collaboration entity and set collaboration_uuid
			collaborationService.approve(proposalId);
			
			return "redirect:/mypage/received?success=proposal_accepted";
		} catch (Exception e) {
			// Create a safe error message for URL parameter
			String safeMessage;
			try {
				String rawMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
				// Clean message: remove line breaks, quotes, brackets, and other problematic characters
				String cleanMessage = rawMessage
					.replaceAll("[\\r\\n\\[\\]\"'<>]", " ")
					.replaceAll("\\s+", " ")
					.trim();
				
				// Limit message length to avoid very long URLs
				if (cleanMessage.length() > 50) {
					cleanMessage = cleanMessage.substring(0, 50) + "...";
				}
				
				// URL encode the message to handle any remaining special characters
				safeMessage = URLEncoder.encode(cleanMessage, StandardCharsets.UTF_8);
			} catch (Exception encodingException) {
				// Ultimate fallback if even encoding fails
				safeMessage = "encoding_error";
			}
			
			return "redirect:/mypage/received?error=approval_failed&message=" + safeMessage;
		}
	}

	@PostMapping("/{proposalId}/reject/form")
	@PreAuthorize("hasRole('BUSINESS_OWNER')")
	public String rejectProposalForm(
			@PathVariable UUID proposalId,
			Principal principal) {

		if (principal == null) {
			return "redirect:/login?error=authentication_required";
		}
		
		User businessOwner = userRepository.findByEmail(principal.getName()).orElse(null);
		if (businessOwner == null) {
			return "redirect:/login?error=user_not_found";
		}

		CollaborationProposal proposal = proposalRepository.findById(proposalId).orElse(null);
		if (proposal == null) {
			return "redirect:/mypage/received?error=proposal_not_found";
		}

		// Verify that the current user is the target business owner (or can reject)
		if (!businessOwner.getRole().name().equals("ROLE_BUSINESS_OWNER") && 
		    !businessOwner.getRole().name().equals("ROLE_ADMIN")) {
			return "redirect:/mypage/received?error=unauthorized_action";
		}
		
		// Check if already processed
		if (proposal.getStatus() != CollaborationProposal.CollaborationStatus.PENDING) {
			return "redirect:/mypage/received?error=already_processed";
		}

		proposal.setStatus(CollaborationProposal.CollaborationStatus.REJECTED);
		// Target business owner handled through store relationship
		proposalRepository.save(proposal);

		// 제안자에게 거절 알림 전송
		notificationService.notifyProposalRejected(proposal);

		return "redirect:/mypage/received?success=proposal_rejected";
	}

	@GetMapping("/browse")
	public String browseProposals(
			@RequestParam(required = false) String category,
			Model model) {

		List<CollaborationProposal> proposals;
		if (category != null && !category.isEmpty()) {
			// Category removed from new structure - showing all pending proposals
			proposals = proposalRepository.findByStatusOrderByCreatedAtDesc(CollaborationProposal.CollaborationStatus.PENDING);
		} else {
			proposals = proposalRepository.findByStatusOrderByCreatedAtDesc(CollaborationProposal.CollaborationStatus.PENDING);
		}

		model.addAttribute("proposals", proposals);
		model.addAttribute("selectedCategory", category);
		return "proposals/browse";
	}

	// New API endpoints for proposal updates and image management

	@PutMapping("/{proposalId}")
	@ResponseBody
	public ApiResponseData<CollaborationProposal> updateProposal(
			@PathVariable UUID proposalId,
			@RequestBody UpdateProposalRequest request,
			Principal principal) {
		
		User user = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));
		
		CollaborationProposal proposal = proposalRepository.findById(proposalId)
				.orElseThrow(() -> new RuntimeException("Proposal not found"));
		
		// Check authorization - only proposer or target store owner can update
		User proposerUser = proposal.getProposerUser() != null ? proposal.getProposerUser() : 
			(proposal.getProposerStore() != null ? proposal.getProposerStore().getUser() : null);
		User targetUser = proposal.getTargetStore() != null ? proposal.getTargetStore().getUser() : null;
		
		boolean isAuthorized = (proposerUser != null && proposerUser.getUuid().equals(user.getUuid())) ||
			(targetUser != null && targetUser.getUuid().equals(user.getUuid()));
		
		if (!isAuthorized) {
			throw new RuntimeException("Unauthorized to update this proposal");
		}
		
		// Update proposal fields
		// Industry field removed from new structure
		// Products field removed from new structure - handled through proposerProduct/targetProduct
		// ProfitShare, Duration, Location fields removed from new structure
		
		CollaborationProposal updatedProposal = proposalRepository.save(proposal);
		return ApiResponseData.success(updatedProposal);
	}

	@PostMapping("/{proposalId}/images")
	@ResponseBody
	public ApiResponseData<List<String>> uploadProposalImages(
			@PathVariable UUID proposalId,
			@RequestParam("images") MultipartFile[] files,
			Principal principal) {
		
		User user = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));
		
		CollaborationProposal proposal = proposalRepository.findById(proposalId)
				.orElseThrow(() -> new RuntimeException("Proposal not found"));
		
		// Check authorization
		User proposerUser = proposal.getProposerUser() != null ? proposal.getProposerUser() : 
			(proposal.getProposerStore() != null ? proposal.getProposerStore().getUser() : null);
		User targetUser = proposal.getTargetStore() != null ? proposal.getTargetStore().getUser() : null;
		
		boolean isAuthorized = (proposerUser != null && proposerUser.getUuid().equals(user.getUuid())) ||
			(targetUser != null && targetUser.getUuid().equals(user.getUuid()));
		
		if (!isAuthorized) {
			throw new RuntimeException("Unauthorized to upload images for this proposal");
		}
		
		// TODO: Replace with Product image upload
		List<String> imagePaths = List.of(); // imageUploadService.uploadCollaborationProposalImages(proposal, files);
		return ApiResponseData.success(imagePaths);
	}

	@GetMapping("/{proposalId}/images")
	@ResponseBody
	public ApiResponseData<List<Object>> getProposalImages(
			@PathVariable UUID proposalId,
			Principal principal) {
		
		User user = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));
		
		CollaborationProposal proposal = proposalRepository.findById(proposalId)
				.orElseThrow(() -> new RuntimeException("Proposal not found"));
		
		// Check authorization
		User proposerUser = proposal.getProposerUser() != null ? proposal.getProposerUser() : 
			(proposal.getProposerStore() != null ? proposal.getProposerStore().getUser() : null);
		User targetUser = proposal.getTargetStore() != null ? proposal.getTargetStore().getUser() : null;
		
		boolean isAuthorized = (proposerUser != null && proposerUser.getUuid().equals(user.getUuid())) ||
			(targetUser != null && targetUser.getUuid().equals(user.getUuid()));
		
		if (!isAuthorized) {
			throw new RuntimeException("Unauthorized to view images for this proposal");
		}
		
		// TODO: Replace with Product image retrieval
		List<Object> images = List.of(); // imageUploadService.getCollaborationProposalImages(proposal);
		return ApiResponseData.success(images);
	}

	// DTO for update request
	public static class UpdateProposalRequest {
		private String industry;
		private String products;
		private String profitShare;
		private String duration;
		private String location;
		
		// Getters and setters
		public String getIndustry() { return industry; }
		public void setIndustry(String industry) { this.industry = industry; }
		
		public String getProducts() { return products; }
		public void setProducts(String products) { this.products = products; }
		
		public String getProfitShare() { return profitShare; }
		public void setProfitShare(String profitShare) { this.profitShare = profitShare; }
		
		public String getDuration() { return duration; }
		public void setDuration(String duration) { this.duration = duration; }
		
		public String getLocation() { return location; }
		public void setLocation(String location) { this.location = location; }
	}
}