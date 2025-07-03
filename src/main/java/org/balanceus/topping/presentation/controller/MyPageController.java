package org.balanceus.topping.presentation.controller;

import java.security.Principal;
import java.util.List;

import org.balanceus.topping.domain.model.ChatRoom;
import org.balanceus.topping.domain.model.Collaboration;
import org.balanceus.topping.domain.model.CollaborationProduct;
import org.balanceus.topping.domain.model.CollaborationProposal;
import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.ChatRoomRepository;
import org.balanceus.topping.domain.repository.CollaborationProductRepository;
import org.balanceus.topping.domain.repository.CollaborationProposalRepository;
import org.balanceus.topping.domain.repository.CollaborationRepository;
import org.balanceus.topping.domain.repository.ProductRepository;
import org.balanceus.topping.domain.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageController {

	private final UserRepository userRepository;
	private final CollaborationProposalRepository proposalRepository;
	private final CollaborationRepository collaborationRepository;
	private final ProductRepository productRepository;
	private final CollaborationProductRepository collaborationProductRepository;
	private final ChatRoomRepository chatRoomRepository;

	@GetMapping
	public String myPage(Model model, Principal principal) {
		if (principal == null) {
			return "redirect:/login";
		}

		User user = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));

		// Get user's proposals
		List<CollaborationProposal> proposals = proposalRepository.findByProposer(user);
		
		// Get collaborations where user is involved
		List<CollaborationProposal> acceptedCollaborations = proposalRepository
				.findByStatus(CollaborationProposal.ProposalStatus.ACCEPTED);
		
		// Get user's registered products
		List<Product> myProducts = productRepository.findByCreator(user);
		
		// Get user's collaboration products
		List<CollaborationProduct> myCollaborationProducts = collaborationProductRepository.findAll();
		
		// Get active chat rooms
		List<ChatRoom> activeChatRooms = chatRoomRepository.findByIsActiveTrue();

		// Calculate statistics
		int proposalCount = proposals.size();
		int collaborationCount = acceptedCollaborations.size();
		int productCount = myProducts.size();
		int chatRoomCount = activeChatRooms.size();

		model.addAttribute("proposals", proposals);
		model.addAttribute("acceptedCollaborations", acceptedCollaborations);
		model.addAttribute("myProducts", myProducts);
		model.addAttribute("myCollaborationProducts", myCollaborationProducts);
		model.addAttribute("proposalCount", proposalCount);
		model.addAttribute("collaborationCount", collaborationCount);
		model.addAttribute("productCount", productCount);
		model.addAttribute("chatRoomCount", chatRoomCount);

		return "mypage";
	}
}