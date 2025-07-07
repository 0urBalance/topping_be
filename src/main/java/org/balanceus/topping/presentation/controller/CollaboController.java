package org.balanceus.topping.presentation.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.balanceus.topping.domain.model.ChatRoom;
import org.balanceus.topping.domain.model.Collaboration;
import org.balanceus.topping.domain.model.CollaborationProduct;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.infrastructure.security.Role;
import org.balanceus.topping.domain.repository.ChatRoomRepository;
import org.balanceus.topping.domain.repository.CollaborationProductRepository;
import org.balanceus.topping.domain.repository.CollaborationRepository;
import org.balanceus.topping.domain.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/collabo")
@RequiredArgsConstructor
public class CollaboController {

	private final CollaborationRepository collaborationRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final CollaborationProductRepository collaborationProductRepository;
	private final UserRepository userRepository;

	@GetMapping
	public String collabo(Model model, Principal principal) {
		// Spring Security guarantees principal is not null due to .authenticated() configuration
		User user = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));
		
		List<Collaboration> currentCollaborations = collaborationRepository.findByApplicant(user);
		List<ChatRoom> chatRooms = chatRoomRepository.findByIsActiveTrue();
		List<CollaborationProduct> collaborationProducts = collaborationProductRepository.findAll();

		model.addAttribute("currentCollaborations", currentCollaborations);
		model.addAttribute("chatRooms", chatRooms);
		model.addAttribute("collaborationProducts", collaborationProducts);

		return "collabo";
	}

	// Redirect old paths to new structure
	@GetMapping("/create")
	public String createCollabo() {
		return "redirect:/products/create";
	}

	@GetMapping("/suggest")
	public String suggestCollabo() {
		return "redirect:/proposals/suggest";
	}

	@GetMapping("/dashboard")
	public String dashboard() {
		return "redirect:/proposals/dashboard";
	}

	@GetMapping("/apply/{id}")
	public String applyCollabo(@PathVariable UUID id, Principal principal) {
		// Check authentication
		if (principal == null) {
			return "redirect:/login?error=authentication_required";
		}
		
		// Get authenticated user to check role
		User user = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found: " + principal.getName()));
		
		// Route based on user role
		if (user.getRole() == Role.ROLE_BUSINESS_OWNER) {
			// Business owners are redirected to proposals/suggest for creating business proposals
			return "redirect:/collaborations/apply/" + id;
		} else {
			// General users (ROLE_USER) are redirected to collaborations/apply for direct collaboration
			return "redirect:/proposals/suggest";
		}
	}
}