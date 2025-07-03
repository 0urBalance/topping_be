package org.balanceus.topping.presentation.controller;

import java.util.List;
import java.util.UUID;

import org.balanceus.topping.domain.model.Collaboration;
import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.model.Collaboration.CollaborationStatus;
import org.balanceus.topping.domain.repository.CollaborationRepository;
import org.balanceus.topping.domain.repository.ProductRepository;
import org.balanceus.topping.domain.repository.UserRepository;
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

	@GetMapping
	public String listCollaborations(Model model) {
		List<Collaboration> collaborations = collaborationRepository.findAll();
		model.addAttribute("collaborations", collaborations);
		return "collaborations/list";
	}

	@GetMapping("/apply/{productId}")
	public String applyForm(@PathVariable UUID productId, Model model) {
		Product product = productRepository.findById(productId).orElse(null);
		if (product == null) {
			return "redirect:/products";
		}
		model.addAttribute("product", product);
		model.addAttribute("collaboration", new Collaboration());
		return "collaborations/apply";
	}

	@PostMapping("/apply")
	public String applyCollaboration(
			@RequestParam UUID productId,
			@RequestParam UUID applicantId,
			@RequestParam String message) {
		
		Product product = productRepository.findById(productId).orElse(null);
		User applicant = userRepository.findById(applicantId).orElse(null);
		
		if (product == null || applicant == null) {
			return "redirect:/products";
		}

		Collaboration collaboration = new Collaboration();
		collaboration.setProduct(product);
		collaboration.setApplicant(applicant);
		collaboration.setMessage(message);
		collaboration.setStatus(CollaborationStatus.PENDING);

		collaborationRepository.save(collaboration);
		return "redirect:/collaborations";
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