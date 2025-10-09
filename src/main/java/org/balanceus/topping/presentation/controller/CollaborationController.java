package org.balanceus.topping.presentation.controller;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.application.service.CollaborationApplicationException;
import org.balanceus.topping.application.service.CollaborationApplicationService;
import org.balanceus.topping.infrastructure.response.ApiResponseData;
import org.balanceus.topping.presentation.dto.collaboration.CollaborationApplyViewModel;
import org.balanceus.topping.presentation.dto.collaboration.CollaborationCardView;
import org.balanceus.topping.presentation.dto.collaboration.CollaborationProposalForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/collaborations")
@RequiredArgsConstructor
@Slf4j
public class CollaborationController {

    private final CollaborationApplicationService collaborationApplicationService;

    @GetMapping
    public String listCollaborations(Model model) {
        List<CollaborationCardView> collaborations = collaborationApplicationService.getCollaborationCards();
        model.addAttribute("collaborations", collaborations);
        return "collaborations/list";
    }

    @GetMapping("/apply")
    public String applyForm(@RequestParam(required = false) UUID productId,
                            @RequestParam(required = false) UUID storeId,
                            Model model,
                            Principal principal) {
        if (principal == null) {
            return "redirect:/login?error=authentication_required";
        }

        try {
            CollaborationApplyViewModel viewModel = collaborationApplicationService.prepareApplyForm(
                    Optional.ofNullable(productId),
                    Optional.ofNullable(storeId),
                    principal.getName());

            populateApplyModel(model, viewModel);
            return "collaborations/apply";
        } catch (CollaborationApplicationException e) {
            log.debug("Redirecting apply form due to: {}", e.getMessage());
            return resolveRedirect(e, "redirect:/collaborations/apply");
        }
    }

    @GetMapping("/apply/{productId}")
    public String applyFormLegacy(@PathVariable UUID productId, Model model, Principal principal) {
        return applyForm(productId, null, model, principal);
    }

    @PostMapping("/apply")
    public String applyCollaboration(@ModelAttribute CollaborationProposalForm form,
                                     Principal principal) {
        if (principal == null) {
            return "redirect:/login?error=authentication_required";
        }

        try {
            collaborationApplicationService.submitProposal(form, principal.getName());
            return "redirect:/mypage/applications?success=proposal_submitted";
        } catch (CollaborationApplicationException e) {
            log.debug("Proposal submission failed: {}", e.getMessage());
            return resolveRedirect(e, "redirect:/collaborations/apply");
        }
    }

    @PostMapping("/{id}/accept")
    public String acceptCollaboration(@PathVariable UUID id, Principal principal) {
        if (principal == null) {
            return "redirect:/login?error=authentication_required";
        }

        try {
            collaborationApplicationService.acceptCollaboration(id, principal.getName());
            return "redirect:/mypage/received?success=collaboration_accepted";
        } catch (CollaborationApplicationException e) {
            log.debug("Accept collaboration redirect: {}", e.getMessage());
            return resolveRedirect(e, "redirect:/mypage/received");
        }
    }

    @PostMapping("/{id}/reject")
    public String rejectCollaboration(@PathVariable UUID id, Principal principal) {
        if (principal == null) {
            return "redirect:/login?error=authentication_required";
        }

        try {
            collaborationApplicationService.rejectCollaboration(id, principal.getName());
            return "redirect:/mypage/received?success=collaboration_rejected";
        } catch (CollaborationApplicationException e) {
            log.debug("Reject collaboration redirect: {}", e.getMessage());
            return resolveRedirect(e, "redirect:/mypage/received");
        }
    }

    @GetMapping("/api")
    @ResponseBody
    public ApiResponseData<List<CollaborationCardView>> getCollaborationsApi() {
        return ApiResponseData.success(collaborationApplicationService.getCollaborationCards());
    }

    @GetMapping("/api/user/{userId}")
    @ResponseBody
    public ApiResponseData<List<CollaborationCardView>> getUserCollaborationsApi(@PathVariable UUID userId) {
        try {
            List<CollaborationCardView> collaborations = collaborationApplicationService.getCollaborationsForUser(userId);
            return ApiResponseData.success(collaborations);
        } catch (CollaborationApplicationException e) {
            log.debug("Failed to fetch user collaborations: {}", e.getMessage());
            if ("user_not_found".equals(e.getErrorCode())) {
                return ApiResponseData.failure(404, "User not found");
            }
            return ApiResponseData.failure(400, "Cannot load collaborations");
        }
    }

    private void populateApplyModel(Model model, CollaborationApplyViewModel viewModel) {
        model.addAttribute("applyView", viewModel);
        model.addAttribute("user", viewModel.getUser());
        model.addAttribute("isBusinessOwner", viewModel.isBusinessOwner());
        model.addAttribute("userStore", viewModel.getUserStore());
        model.addAttribute("userProducts", viewModel.getUserProducts());
        model.addAttribute("allStores", viewModel.getAllStores());
        model.addAttribute("allProducts", viewModel.getAllProducts());
        model.addAttribute("targetStore", viewModel.getTargetStore());
        model.addAttribute("targetProduct", viewModel.getTargetProduct());
        model.addAttribute("storeDataJson", viewModel.getStoreDataJson());
    }

    private String resolveRedirect(CollaborationApplicationException exception, String defaultUrl) {
        if (exception.getRedirectUrl() != null) {
            return exception.getRedirectUrl();
        }
        if (exception.getErrorCode() != null) {
            return defaultUrl + "?error=" + exception.getErrorCode();
        }
        return defaultUrl;
    }
}
