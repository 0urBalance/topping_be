package org.balanceus.topping.presentation.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.balanceus.topping.domain.model.ChatRoom;
import org.balanceus.topping.domain.model.Collaboration;
import org.balanceus.topping.domain.model.CollaborationProduct;
import org.balanceus.topping.domain.model.User;
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
		if (principal != null) {
			User user = userRepository.findByEmail(principal.getName())
					.orElse(null);
			
			if (user != null) {
				List<Collaboration> currentCollaborations = collaborationRepository.findByApplicant(user);
				List<ChatRoom> chatRooms = chatRoomRepository.findByIsActiveTrue();
				List<CollaborationProduct> collaborationProducts = collaborationProductRepository.findAll();

				model.addAttribute("currentCollaborations", currentCollaborations);
				model.addAttribute("chatRooms", chatRooms);
				model.addAttribute("collaborationProducts", collaborationProducts);
			}
		}

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
	public String applyCollabo(@PathVariable UUID id) {
		return "redirect:/collaborations/apply/" + id;
	}
}