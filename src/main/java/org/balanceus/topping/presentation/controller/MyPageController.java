package org.balanceus.topping.presentation.controller;

import java.security.Principal;
import java.util.List;

import org.balanceus.topping.domain.model.ChatRoom;
import org.balanceus.topping.domain.model.Collaboration;
import org.balanceus.topping.domain.model.CollaborationProduct;
import org.balanceus.topping.domain.model.CollaborationProposal;
import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.ChatRoomRepository;
import org.balanceus.topping.domain.repository.CollaborationProductRepository;
import org.balanceus.topping.domain.repository.CollaborationProposalRepository;
import org.balanceus.topping.domain.repository.CollaborationRepository;
import org.balanceus.topping.application.service.ProductService;
import org.balanceus.topping.domain.repository.StoreRepository;
import org.balanceus.topping.domain.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
@Slf4j
public class MyPageController {

	private final UserRepository userRepository;
	private final CollaborationProposalRepository proposalRepository;
	private final CollaborationRepository collaborationRepository;
	private final ProductService productService;
	private final CollaborationProductRepository collaborationProductRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final StoreRepository storeRepository;

	@GetMapping
	public String myPage(Model model, Principal principal) {
		log.debug("MyPage accessed - Principal: {}", principal);
		
		if (principal == null) {
			log.warn("Principal is null - redirecting to login");
			return "redirect:/login";
		}

		log.debug("User authenticated - Principal name: {}", principal.getName());
		User user = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));
		
		log.debug("User found: {}", user.getEmail());

		// Get user's proposals
		List<CollaborationProposal> proposals = proposalRepository.findByProposer(user);
		
		// Get collaborations where user is involved
		List<CollaborationProposal> acceptedCollaborations = proposalRepository
				.findByStatus(CollaborationProposal.ProposalStatus.ACCEPTED);
		
		// Get user's registered products
		List<Product> myProducts = productService.getProductsByCreator(principal.getName());
		
		// Get user's collaboration products
		List<CollaborationProduct> myCollaborationProducts = collaborationProductRepository.findAll();
		
		// Get active chat rooms
		List<ChatRoom> activeChatRooms = chatRoomRepository.findByIsActiveTrue();

		// Get user's store if exists
		Store userStore = storeRepository.findByUser(user).orElse(null);

		// Calculate statistics
		int proposalCount = proposals.size();
		int collaborationCount = acceptedCollaborations.size();
		int productCount = myProducts.size();
		int chatRoomCount = activeChatRooms.size();

		model.addAttribute("proposals", proposals);
		model.addAttribute("acceptedCollaborations", acceptedCollaborations);
		model.addAttribute("myProducts", myProducts);
		model.addAttribute("myCollaborationProducts", myCollaborationProducts);
		model.addAttribute("userStore", userStore);
		model.addAttribute("proposalCount", proposalCount);
		model.addAttribute("collaborationCount", collaborationCount);
		model.addAttribute("productCount", productCount);
		model.addAttribute("chatRoomCount", chatRoomCount);

		return "mypage";
	}

	@GetMapping("/store")
	public String myPageStore(Model model, Principal principal) {
		log.debug("MyPage Store accessed - Principal: {}", principal);
		
		if (principal == null) {
			log.warn("Principal is null - redirecting to login");
			return "redirect:/login";
		}

		User user = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));
		
		// Get user's store if exists
		Store userStore = storeRepository.findByUser(user).orElse(null);
		
		if (userStore == null) {
			// Redirect to store registration if no store exists
			return "redirect:/stores/register";
		}
		
		model.addAttribute("userStore", userStore);
		return "store/my-store";
	}

	@GetMapping("/product")
	public String myPageProduct(Model model, Principal principal) {
		log.debug("MyPage Product accessed - Principal: {}", principal);
		
		if (principal == null) {
			log.warn("Principal is null - redirecting to login");
			return "redirect:/login";
		}

		User user = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));
		
		// Get user's registered products
		List<Product> myProducts = productService.getProductsByCreator(principal.getName());
		
		model.addAttribute("myProducts", myProducts);
		return "mypage/product";
	}

	@GetMapping("/collabos")
	public String myPageCollabos(Model model, Principal principal) {
		log.debug("MyPage Collaborations accessed - Principal: {}", principal);
		
		if (principal == null) {
			log.warn("Principal is null - redirecting to login");
			return "redirect:/login";
		}

		User user = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));
		
		// Get user's proposals
		List<CollaborationProposal> proposals = proposalRepository.findByProposer(user);
		
		// Get collaborations where user is involved
		List<CollaborationProposal> acceptedCollaborations = proposalRepository
				.findByStatus(CollaborationProposal.ProposalStatus.ACCEPTED);
		
		// Get user's collaboration products
		List<CollaborationProduct> myCollaborationProducts = collaborationProductRepository.findAll();
		
		// Get active chat rooms
		List<ChatRoom> activeChatRooms = chatRoomRepository.findByIsActiveTrue();

		model.addAttribute("proposals", proposals);
		model.addAttribute("acceptedCollaborations", acceptedCollaborations);
		model.addAttribute("myCollaborationProducts", myCollaborationProducts);
		model.addAttribute("activeChatRooms", activeChatRooms);
		return "mypage/collabos";
	}
}