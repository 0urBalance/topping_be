package org.balanceus.topping.presentation.controller;

import java.security.Principal;
import java.util.UUID;

import org.balanceus.topping.domain.model.Role;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/collabo")
@RequiredArgsConstructor
public class CollaboController {

	private final UserRepository userRepository;

	@GetMapping
	public String collabo() {
		return "redirect:/mypage";
	}

	// Redirect old paths to new structure
	@GetMapping("/create")
	public String createCollabo() {
		return "redirect:/products/create";
	}

	@GetMapping("/suggest")
	public String suggestCollabo() {
		return "redirect:/collaborations/suggest";
	}

	@GetMapping("/dashboard")
	public String dashboard() {
		return "redirect:/mypage/received";
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

		// Route based on user role: business owners suggest, regular users apply
		if (user.getRole() == Role.ROLE_BUSINESS_OWNER) {
			return "redirect:/collaborations/suggest?productId=" + id;
		} else {
			return "redirect:/collaborations/apply?productId=" + id;
		}
	}
}
