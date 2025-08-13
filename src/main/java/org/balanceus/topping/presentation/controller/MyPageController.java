package org.balanceus.topping.presentation.controller;

import java.security.Principal;
import java.util.List;

import org.balanceus.topping.domain.model.ChatRoom;
import org.balanceus.topping.domain.model.Collaboration;
import org.balanceus.topping.domain.model.CollaborationProposal;
import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.ChatRoomRepository;
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
		
		log.debug("User found: {} with UUID: {}", user.getEmail(), user.getUuid());

		// Get user's store if exists
		Store userStore = storeRepository.findByUser(user).orElse(null);
		log.debug("User store: {}", userStore != null ? userStore.getUuid() : "null");

		// Get user's proposals (both as user and store owner)
		List<CollaborationProposal> proposals = proposalRepository.findByProposerUser(user);
		log.debug("Found {} proposals by proposer user: {}", proposals.size(), user.getUuid());
		if (userStore != null) {
			List<CollaborationProposal> storeProposals = proposalRepository.findByProposerStore(userStore);
			log.debug("Found {} proposals by proposer store: {}", storeProposals.size(), userStore.getUuid());
			proposals.addAll(storeProposals);
		}
		log.debug("Total proposals for user: {}", proposals.size());
		
		// Get user's collaboration applications (submitted via apply form)
		List<Collaboration> myApplications = userStore != null ? 
			collaborationRepository.findByStoreParticipation(userStore) : List.of();
		
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
		
		// Get collaborations where user's store is involved (received applications)
		List<Collaboration> receivedApplications = userStore != null ? 
			collaborationRepository.findByStoreAndStatus(userStore, Collaboration.CollaborationStatus.PENDING) : List.of();
		
		// Get collaboration proposals where user's store is the target
		List<CollaborationProposal> receivedProposals = userStore != null ? 
			proposalRepository.findByTargetStore(userStore) : List.of();
		
		// Also include proposals with null target store (general proposals for business owners)
		if (userStore != null) {
			List<CollaborationProposal> generalProposals = proposalRepository.findByTargetStoreIsNull();
			log.debug("Found {} general proposals (null target store)", generalProposals.size());
			receivedProposals.addAll(generalProposals);
		}
		log.debug("Total received proposals: {}", receivedProposals.size());
		
		// Get user's registered products
		List<Product> myProducts = productService.getProductsByCreator(user.getUuid());
		
		// Get user's collaboration products (COLLABORATION type products)
		List<Product> myCollaborationProducts = myProducts.stream()
			.filter(p -> p.getProductType() == Product.ProductType.COLLABORATION)
			.toList();
		
		// Get active chat rooms
		List<ChatRoom> activeChatRooms = chatRoomRepository.findByIsActiveTrue();

		// Calculate statistics
		int proposalCount = proposals.size();
		int applicationCount = myApplications.size();
		int ongoingCollaborationCount = ongoingCollaborations.size();
		int pendingApplicationCount = pendingApplications.size();
		int receivedApplicationCount = receivedApplications.size() + receivedProposals.size(); // Include both types
		int productCount = myProducts.size();
		int chatRoomCount = activeChatRooms.size();

		// Calculate pending actions for alerts
		int pendingActionsCount = receivedApplications.stream()
				.mapToInt(collab -> collab.getStatus() == Collaboration.CollaborationStatus.PENDING ? 1 : 0)
				.sum() + 
				receivedProposals.stream()
				.mapToInt(prop -> prop.getStatus() == CollaborationProposal.CollaborationStatus.PENDING ? 1 : 0)
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
		List<Product> myProducts = productService.getProductsByCreator(user.getUuid());
		
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
		
		// Get user's store if exists
		Store userStore = storeRepository.findByUser(user).orElse(null);
		
		// Get user's proposals (both as user and store owner)
		List<CollaborationProposal> proposals = proposalRepository.findByProposerUser(user);
		if (userStore != null) {
			proposals.addAll(proposalRepository.findByProposerStore(userStore));
		}
		
		// Get user's collaboration applications (submitted via apply form)
		List<Collaboration> myApplications = userStore != null ? 
			collaborationRepository.findByStoreParticipation(userStore) : List.of();
		
		// Get collaborations where user is involved
		List<CollaborationProposal> acceptedCollaborations = proposalRepository
				.findByStatus(CollaborationProposal.CollaborationStatus.ACCEPTED);
		
		
		// Get active chat rooms
		List<ChatRoom> activeChatRooms = chatRoomRepository.findByIsActiveTrue();

		model.addAttribute("proposals", proposals);
		model.addAttribute("myApplications", myApplications);
		model.addAttribute("acceptedCollaborations", acceptedCollaborations);
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
		
		// Get user's store if exists
		Store userStore = storeRepository.findByUser(user).orElse(null);
		
		// Get user's collaboration applications (submitted via apply form)
		List<Collaboration> myApplications = userStore != null ? 
			collaborationRepository.findByStoreParticipation(userStore) : List.of();
		
		// Get user's collaboration proposals (submitted via new enhanced form)
		List<CollaborationProposal> myProposals = proposalRepository.findByProposerUser(user);
		if (userStore != null) {
			myProposals.addAll(proposalRepository.findByProposerStore(userStore));
		}
		
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
		
		// Separate proposals by status
		List<CollaborationProposal> pendingProposals = myProposals.stream()
				.filter(prop -> prop.getStatus() == CollaborationProposal.CollaborationStatus.PENDING)
				.toList();
		
		List<CollaborationProposal> acceptedProposals = myProposals.stream()
				.filter(prop -> prop.getStatus() == CollaborationProposal.CollaborationStatus.ACCEPTED)
				.toList();
		
		List<CollaborationProposal> rejectedProposals = myProposals.stream()
				.filter(prop -> prop.getStatus() == CollaborationProposal.CollaborationStatus.REJECTED)
				.toList();

		model.addAttribute("myApplications", myApplications);
		model.addAttribute("pendingApplications", pendingApplications);
		model.addAttribute("acceptedApplications", acceptedApplications);
		model.addAttribute("rejectedApplications", rejectedApplications);
		
		// Add proposal data
		model.addAttribute("myProposals", myProposals);
		model.addAttribute("pendingProposals", pendingProposals);
		model.addAttribute("acceptedProposals", acceptedProposals);
		model.addAttribute("rejectedProposals", rejectedProposals);
		
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
		
		// Get user's store if exists
		Store userStore = storeRepository.findByUser(user).orElse(null);
		
		// Get user's accepted collaboration applications
		List<Collaboration> myApplications = userStore != null ? 
			collaborationRepository.findByStoreParticipation(userStore) : List.of();
		List<Collaboration> ongoingCollaborations = myApplications.stream()
				.filter(collab -> collab.getStatus() == Collaboration.CollaborationStatus.ACCEPTED)
				.toList();
		
		// Get collaborations where user's store is involved and accepted
		List<Collaboration> acceptedReceivedApplications = userStore != null ?
			collaborationRepository.findByStoreAndStatus(userStore, Collaboration.CollaborationStatus.ACCEPTED) :
			List.of();
		
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
		// Get user's store and find collaborations involving their store
		Store userStore2 = storeRepository.findByUser(user).orElse(null);
		List<Collaboration> receivedApplications = userStore2 != null ? 
			collaborationRepository.findByStoreParticipation(userStore2) : List.of();
		
		// Get collaboration proposals where user is the target business owner
		List<CollaborationProposal> receivedProposals = userStore2 != null ? 
			proposalRepository.findByTargetStore(userStore2) : List.of();
		
		// Also include general proposals (null target store) for business owners  
		if (userStore2 != null) {
			List<CollaborationProposal> generalProposals = proposalRepository.findByTargetStoreIsNull();
			log.debug("Found {} general proposals for received page", generalProposals.size());
			receivedProposals.addAll(generalProposals);
		}
		log.debug("Total received proposals for received page: {}", receivedProposals.size());
		
		// Sort by creation date (newest first)
		receivedApplications.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
		receivedProposals.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
		
		// Separate collaborations by status
		List<Collaboration> pendingReceived = receivedApplications.stream()
				.filter(collab -> collab.getStatus() == Collaboration.CollaborationStatus.PENDING)
				.toList();
		
		List<Collaboration> acceptedReceived = receivedApplications.stream()
				.filter(collab -> collab.getStatus() == Collaboration.CollaborationStatus.ACCEPTED)
				.toList();
		
		List<Collaboration> rejectedReceived = receivedApplications.stream()
				.filter(collab -> collab.getStatus() == Collaboration.CollaborationStatus.REJECTED)
				.toList();
		
		// Separate proposals by status
		List<CollaborationProposal> pendingProposals = receivedProposals.stream()
				.filter(prop -> prop.getStatus() == CollaborationProposal.CollaborationStatus.PENDING)
				.toList();
		
		List<CollaborationProposal> acceptedProposals = receivedProposals.stream()
				.filter(prop -> prop.getStatus() == CollaborationProposal.CollaborationStatus.ACCEPTED)
				.toList();
		
		List<CollaborationProposal> rejectedProposals = receivedProposals.stream()
				.filter(prop -> prop.getStatus() == CollaborationProposal.CollaborationStatus.REJECTED)
				.toList();

		// Add all received items for display
		model.addAttribute("receivedApplications", receivedApplications);
		model.addAttribute("receivedProposals", receivedProposals);
		model.addAttribute("pendingReceived", pendingReceived);
		model.addAttribute("acceptedReceived", acceptedReceived);
		model.addAttribute("rejectedReceived", rejectedReceived);
		model.addAttribute("pendingProposals", pendingProposals);
		model.addAttribute("acceptedProposals", acceptedProposals);
		model.addAttribute("rejectedProposals", rejectedProposals);
		
		return "mypage/received";
	}

	@GetMapping("/upgrade")
	public String upgradeToBusinessOwner(Principal principal) {
		// TODO: Implement actual user role upgrade functionality
		// For now, redirect to mypage with info message
		return "redirect:/mypage?info=upgrade_required";
	}
}