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
		
		// Get user's collaboration applications (submitted via apply form)
		List<Collaboration> myApplications = collaborationRepository.findByApplicant(user);
		
		// Get ongoing collaborations (accepted applications by user)
		List<Collaboration> ongoingCollaborations = myApplications.stream()
				.filter(collab -> collab.getStatus() == Collaboration.CollaborationStatus.ACCEPTED)
				.toList();
		
		// Get completed collaborations (for future use)
		List<Collaboration> completedCollaborations = List.of(); // TODO: Implement when status is added
		
		// Get pending applications (waiting for response)
		List<Collaboration> pendingApplications = myApplications.stream()
				.filter(collab -> collab.getStatus() == Collaboration.CollaborationStatus.PENDING)
				.toList();
		
		// Get collaborations where user is the product owner (received applications)
		List<Collaboration> receivedApplications = collaborationRepository.findByProductCreator(user);
		
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
		int applicationCount = myApplications.size();
		int ongoingCollaborationCount = ongoingCollaborations.size();
		int pendingApplicationCount = pendingApplications.size();
		int receivedApplicationCount = receivedApplications.size();
		int productCount = myProducts.size();
		int chatRoomCount = activeChatRooms.size();

		// Calculate pending actions for alerts
		int pendingActionsCount = receivedApplications.stream()
				.mapToInt(collab -> collab.getStatus() == Collaboration.CollaborationStatus.PENDING ? 1 : 0)
				.sum();

		model.addAttribute("user", user);
		model.addAttribute("proposals", proposals);
		model.addAttribute("myApplications", myApplications);
		model.addAttribute("ongoingCollaborations", ongoingCollaborations);
		model.addAttribute("pendingApplications", pendingApplications);
		model.addAttribute("receivedApplications", receivedApplications);
		model.addAttribute("completedCollaborations", completedCollaborations);
		model.addAttribute("myProducts", myProducts);
		model.addAttribute("myCollaborationProducts", myCollaborationProducts);
		model.addAttribute("userStore", userStore);
		model.addAttribute("activeChatRooms", activeChatRooms);
		
		// Statistics
		model.addAttribute("proposalCount", proposalCount);
		model.addAttribute("applicationCount", applicationCount);
		model.addAttribute("ongoingCollaborationCount", ongoingCollaborationCount);
		model.addAttribute("pendingApplicationCount", pendingApplicationCount);
		model.addAttribute("receivedApplicationCount", receivedApplicationCount);
		model.addAttribute("productCount", productCount);
		model.addAttribute("chatRoomCount", chatRoomCount);
		model.addAttribute("pendingActionsCount", pendingActionsCount);

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
		
		// Check if user has business owner role
		if (!user.getRole().name().equals("ROLE_BUSINESS_OWNER") && !user.getRole().name().equals("ROLE_ADMIN")) {
			log.warn("User {} does not have business owner role - redirecting to mypage", user.getEmail());
			return "redirect:/mypage?error=access_denied";
		}
		
		// Get user's store if exists
		Store userStore = storeRepository.findByUser(user).orElse(null);
		
		if (userStore == null) {
			// Redirect to store registration if no store exists
			return "redirect:/stores/register";
		}
		
		model.addAttribute("store", userStore);
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
		
		// Get user's collaboration applications (submitted via apply form)
		List<Collaboration> myApplications = collaborationRepository.findByApplicant(user);
		
		// Get collaborations where user is involved
		List<CollaborationProposal> acceptedCollaborations = proposalRepository
				.findByStatus(CollaborationProposal.ProposalStatus.ACCEPTED);
		
		// Get user's collaboration products
		List<CollaborationProduct> myCollaborationProducts = collaborationProductRepository.findAll();
		
		// Get active chat rooms
		List<ChatRoom> activeChatRooms = chatRoomRepository.findByIsActiveTrue();

		model.addAttribute("proposals", proposals);
		model.addAttribute("myApplications", myApplications);
		model.addAttribute("acceptedCollaborations", acceptedCollaborations);
		model.addAttribute("myCollaborationProducts", myCollaborationProducts);
		model.addAttribute("activeChatRooms", activeChatRooms);
		return "mypage/collabos";
	}

	@GetMapping("/applications")
	public String myPageApplications(Model model, Principal principal) {
		log.debug("MyPage Applications accessed - Principal: {}", principal);
		
		if (principal == null) {
			log.warn("Principal is null - redirecting to login");
			return "redirect:/login";
		}

		User user = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));
		
		// Get user's collaboration applications (submitted via apply form)
		List<Collaboration> myApplications = collaborationRepository.findByApplicant(user);
		
		// Separate by status
		List<Collaboration> pendingApplications = myApplications.stream()
				.filter(collab -> collab.getStatus() == Collaboration.CollaborationStatus.PENDING)
				.toList();
		
		List<Collaboration> acceptedApplications = myApplications.stream()
				.filter(collab -> collab.getStatus() == Collaboration.CollaborationStatus.ACCEPTED)
				.toList();
		
		List<Collaboration> rejectedApplications = myApplications.stream()
				.filter(collab -> collab.getStatus() == Collaboration.CollaborationStatus.REJECTED)
				.toList();

		model.addAttribute("myApplications", myApplications);
		model.addAttribute("pendingApplications", pendingApplications);
		model.addAttribute("acceptedApplications", acceptedApplications);
		model.addAttribute("rejectedApplications", rejectedApplications);
		return "mypage/applications";
	}

	@GetMapping("/ongoing")
	public String myPageOngoing(Model model, Principal principal) {
		log.debug("MyPage Ongoing Collaborations accessed - Principal: {}", principal);
		
		if (principal == null) {
			log.warn("Principal is null - redirecting to login");
			return "redirect:/login";
		}

		User user = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));
		
		// Get user's accepted collaboration applications
		List<Collaboration> myApplications = collaborationRepository.findByApplicant(user);
		List<Collaboration> ongoingCollaborations = myApplications.stream()
				.filter(collab -> collab.getStatus() == Collaboration.CollaborationStatus.ACCEPTED)
				.toList();
		
		// Get collaborations where user is the product owner and accepted
		List<Collaboration> receivedApplications = collaborationRepository.findByProductCreator(user);
		List<Collaboration> acceptedReceivedApplications = receivedApplications.stream()
				.filter(collab -> collab.getStatus() == Collaboration.CollaborationStatus.ACCEPTED)
				.toList();
		
		// Get active chat rooms
		List<ChatRoom> activeChatRooms = chatRoomRepository.findByIsActiveTrue();

		model.addAttribute("ongoingCollaborations", ongoingCollaborations);
		model.addAttribute("acceptedReceivedApplications", acceptedReceivedApplications);
		model.addAttribute("activeChatRooms", activeChatRooms);
		return "mypage/ongoing";
	}

	@GetMapping("/received")
	public String myPageReceived(Model model, Principal principal) {
		log.debug("MyPage Received Applications accessed - Principal: {}", principal);
		
		if (principal == null) {
			log.warn("Principal is null - redirecting to login");
			return "redirect:/login";
		}

		User user = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));
		
		// Get collaborations where user is the product owner (received applications)
		List<Collaboration> receivedApplications = collaborationRepository.findByProductCreator(user);
		
		// Separate by status
		List<Collaboration> pendingReceived = receivedApplications.stream()
				.filter(collab -> collab.getStatus() == Collaboration.CollaborationStatus.PENDING)
				.toList();
		
		List<Collaboration> acceptedReceived = receivedApplications.stream()
				.filter(collab -> collab.getStatus() == Collaboration.CollaborationStatus.ACCEPTED)
				.toList();
		
		List<Collaboration> rejectedReceived = receivedApplications.stream()
				.filter(collab -> collab.getStatus() == Collaboration.CollaborationStatus.REJECTED)
				.toList();

		model.addAttribute("receivedApplications", receivedApplications);
		model.addAttribute("pendingReceived", pendingReceived);
		model.addAttribute("acceptedReceived", acceptedReceived);
		model.addAttribute("rejectedReceived", rejectedReceived);
		return "mypage/received";
	}

	@GetMapping("/upgrade")
	public String upgradeToBusinessOwner(Principal principal) {
		// TODO: Implement actual user role upgrade functionality
		// For now, redirect to mypage with info message
		return "redirect:/mypage?info=upgrade_required";
	}
}