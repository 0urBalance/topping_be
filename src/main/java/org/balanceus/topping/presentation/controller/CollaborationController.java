package org.balanceus.topping.presentation.controller;

import java.util.List;
import java.util.UUID;
import java.security.Principal;

import org.balanceus.topping.domain.model.Collaboration;
import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.model.Collaboration.CollaborationStatus;
import org.balanceus.topping.domain.repository.CollaborationRepository;
import org.balanceus.topping.domain.repository.ProductRepository;
import org.balanceus.topping.domain.repository.UserRepository;
import org.balanceus.topping.application.service.ProductService;
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
	private final ProductRepository productRepository;
	private final UserRepository userRepository;
	private final ProductService productService;

	@GetMapping
	public String listCollaborations(Model model) {
		List<Collaboration> collaborations = collaborationRepository.findAll();
		model.addAttribute("collaborations", collaborations);
		return "collaborations/list";
	}

	@GetMapping("/apply/{productId}")
	public String applyForm(@PathVariable UUID productId, Model model, Principal principal) {
		// Check authentication
		if (principal == null) {
			return "redirect:/login?error=authentication_required";
		}
		
		// Get the target product
		Product product = productRepository.findById(productId).orElse(null);
		if (product == null) {
			return "redirect:/products?error=product_not_found";
		}
		
		// Get the authenticated user
		User user = userRepository.findByEmail(principal.getName()).orElse(null);
		if (user == null) {
			return "redirect:/login?error=user_not_found";
		}
		
		// Prevent self-application
		if (product.getCreator().getUuid().equals(user.getUuid())) {
			return "redirect:/products/" + productId + "?error=cannot_apply_own_product";
		}
		
		// Get user's products for selection
		List<Product> userProducts = productService.getProductsByCreator(user.getUuid());
		
		model.addAttribute("product", product);
		model.addAttribute("user", user);
		model.addAttribute("userProducts", userProducts);
		model.addAttribute("collaboration", new Collaboration());
		return "collaborations/apply";
	}

	@PostMapping("/apply")
	public String applyCollaboration(
			@RequestParam UUID productId,
			@RequestParam(required = false) UUID applicantProductId,
			@RequestParam String message,
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
	public String acceptCollaboration(@PathVariable UUID id) {
		Collaboration collaboration = collaborationRepository.findById(id).orElse(null);
		if (collaboration != null) {
			collaboration.setStatus(CollaborationStatus.ACCEPTED);
			collaborationRepository.save(collaboration);
		}
		return "redirect:/collaborations";
	}

	@PostMapping("/{id}/reject")
	public String rejectCollaboration(@PathVariable UUID id) {
		Collaboration collaboration = collaborationRepository.findById(id).orElse(null);
		if (collaboration != null) {
			collaboration.setStatus(CollaborationStatus.REJECTED);
			collaborationRepository.save(collaboration);
		}
		return "redirect:/collaborations";
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