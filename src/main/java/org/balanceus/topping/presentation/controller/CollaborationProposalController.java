package org.balanceus.topping.presentation.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.balanceus.topping.domain.model.CollaborationProposal;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.CollaborationProposalRepository;
import org.balanceus.topping.domain.repository.UserRepository;
import org.balanceus.topping.infrastructure.response.ApiResponseData;
import org.balanceus.topping.infrastructure.response.Code;
import org.balanceus.topping.infrastructure.security.Role;
import org.balanceus.topping.infrastructure.service.NotificationService;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/proposals")
@RequiredArgsConstructor
public class CollaborationProposalController {

	private final CollaborationProposalRepository proposalRepository;
	private final UserRepository userRepository;
	private final NotificationService notificationService;

	@GetMapping("/suggest")
	public String suggestForm() {
		return "proposals/suggest";
	}

	@PostMapping("/suggest")
	@ResponseBody
	public ApiResponseData<CollaborationProposal> createProposal(
			@RequestParam String title,
			@RequestParam String description,
			@RequestParam String category,
			@RequestParam(required = false) String revenueSharePreference,
			Principal principal) {

		User proposer = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));

		CollaborationProposal proposal = new CollaborationProposal();
		proposal.setTitle(title);
		proposal.setDescription(description);
		proposal.setCategory(category);
		proposal.setProposer(proposer);
		proposal.setRevenueSharePreference(revenueSharePreference);
		proposal.setStatus(CollaborationProposal.ProposalStatus.PENDING);
		proposal.setTrendScore(0);

		CollaborationProposal saved = proposalRepository.save(proposal);
		
		// 사업자들에게 새 제안 알림 전송
		notificationService.notifyBusinessOwnersOfNewProposal(saved);
		
		return ApiResponseData.success(saved);
	}

	@GetMapping("/mypage")
	public String myPage(Model model, Principal principal) {
		User user = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));

		List<CollaborationProposal> myProposals = proposalRepository.findByProposer(user);
		model.addAttribute("proposals", myProposals);
		return "proposals/mypage";
	}

	@GetMapping("/dashboard")
	@PreAuthorize("hasRole('BUSINESS_OWNER')")
	public String businessDashboard(Model model, Principal principal) {
		User businessOwner = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));

		List<CollaborationProposal> pendingProposals = 
				proposalRepository.findByStatusOrderByTrendScoreDesc(CollaborationProposal.ProposalStatus.PENDING);
		
		List<CollaborationProposal> myTargetedProposals = 
				proposalRepository.findByTargetBusinessOwner(businessOwner);

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

		User businessOwner = userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new RuntimeException("User not found"));

		CollaborationProposal proposal = proposalRepository.findById(proposalId)
				.orElseThrow(() -> new RuntimeException("Proposal not found"));

		proposal.setStatus(CollaborationProposal.ProposalStatus.ACCEPTED);
		proposal.setTargetBusinessOwner(businessOwner);
		proposalRepository.save(proposal);

		// 제안자에게 수락 알림 전송
		notificationService.notifyProposalAccepted(proposal);

		return ApiResponseData.success("협업 제안이 수락되었습니다.");
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

		proposal.setStatus(CollaborationProposal.ProposalStatus.REJECTED);
		proposal.setTargetBusinessOwner(businessOwner);
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

		CollaborationProposal proposal = proposalRepository.findById(proposalId).orElse(null);
		if (proposal == null) {
			return "redirect:/mypage/received?error=proposal_not_found";
		}

		// Verify that the current user is the target business owner (or can accept)
		// For proposals, any business owner can accept initially
		if (!businessOwner.getRole().name().equals("ROLE_BUSINESS_OWNER") && 
		    !businessOwner.getRole().name().equals("ROLE_ADMIN")) {
			return "redirect:/mypage/received?error=unauthorized_action";
		}
		
		// Check if already processed
		if (proposal.getStatus() != CollaborationProposal.ProposalStatus.PENDING) {
			return "redirect:/mypage/received?error=already_processed";
		}

		proposal.setStatus(CollaborationProposal.ProposalStatus.ACCEPTED);
		proposal.setTargetBusinessOwner(businessOwner);
		proposalRepository.save(proposal);

		// 제안자에게 수락 알림 전송
		notificationService.notifyProposalAccepted(proposal);

		return "redirect:/mypage/received?success=proposal_accepted";
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
		if (proposal.getStatus() != CollaborationProposal.ProposalStatus.PENDING) {
			return "redirect:/mypage/received?error=already_processed";
		}

		proposal.setStatus(CollaborationProposal.ProposalStatus.REJECTED);
		proposal.setTargetBusinessOwner(businessOwner);
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
			proposals = proposalRepository.findByCategoryOrderByCreatedAtDesc(category);
		} else {
			proposals = proposalRepository.findByStatusOrderByTrendScoreDesc(CollaborationProposal.ProposalStatus.PENDING);
		}

		model.addAttribute("proposals", proposals);
		model.addAttribute("selectedCategory", category);
		return "proposals/browse";
	}
}