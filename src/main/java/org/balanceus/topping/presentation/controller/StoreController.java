package org.balanceus.topping.presentation.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.application.dto.StoreRegistrationRequest;
import org.balanceus.topping.application.service.MenuService;
import org.balanceus.topping.application.service.StoreService;
import org.balanceus.topping.domain.model.Menu;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.repository.StoreLikeRepository;
import org.balanceus.topping.domain.repository.WishlistRepository;
import org.balanceus.topping.infrastructure.response.ApiResponseData;
import org.balanceus.topping.infrastructure.security.UserDetailsImpl;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/stores")
@RequiredArgsConstructor
@Slf4j
public class StoreController {

    private final StoreService storeService;
    private final MenuService menuService;
    private final StoreLikeRepository storeLikeRepository;
    private final WishlistRepository wishlistRepository;

    @GetMapping("/register")
    public String showRegistrationForm(Model model, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        // Check if user has business owner role
        String userRole = userDetails.getUser().getRole().name();
        if (!userRole.equals("ROLE_BUSINESS_OWNER") && !userRole.equals("ROLE_ADMIN")) {
            log.warn("User {} does not have business owner role - access denied", userDetails.getUser().getEmail());
            return "redirect:/mypage?error=access_denied";
        }

        Optional<Store> existingStore = storeService.getStoreByUser(userDetails.getUser().getUuid());
        if (existingStore.isPresent()) {
            return "redirect:/stores/my-store";
        }

        model.addAttribute("storeRegistrationRequest", new StoreRegistrationRequest());
        return "store/register";
    }

    @PostMapping("/register")
    public String registerStore(
            @Valid @ModelAttribute StoreRegistrationRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        // Check if user has business owner role
        String userRole = userDetails.getUser().getRole().name();
        if (!userRole.equals("ROLE_BUSINESS_OWNER") && !userRole.equals("ROLE_ADMIN")) {
            log.warn("User {} does not have business owner role - access denied", userDetails.getUser().getEmail());
            return "redirect:/mypage?error=access_denied";
        }

        if (bindingResult.hasErrors()) {
            return "store/register";
        }

        try {
            storeService.registerStore(request, userDetails.getUser().getUuid());
            redirectAttributes.addFlashAttribute("successMessage", "Store registered successfully!");
            return "redirect:/stores/my-store";
        } catch (Exception e) {
            log.error("Store registration failed", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/stores/register";
        }
    }

    @PostMapping("/api/register")
    @ResponseBody
    public ApiResponseData<String> registerStoreApi(
            @Valid @ModelAttribute StoreRegistrationRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (userDetails == null) {
            return ApiResponseData.failure(401, "Authentication required");
        }

        if (bindingResult.hasErrors()) {
            return ApiResponseData.failure(400, "Validation failed: " + bindingResult.getFieldError().getDefaultMessage());
        }

        try {
            storeService.registerStore(request, userDetails.getUser().getUuid());
            return ApiResponseData.success("Store registered successfully");
        } catch (Exception e) {
            log.error("Store registration failed", e);
            return ApiResponseData.failure(500, e.getMessage());
        }
    }

    @GetMapping("/my-store")
    public String showMyStore(Model model, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        // Check if user has business owner role
        String userRole = userDetails.getUser().getRole().name();
        if (!userRole.equals("ROLE_BUSINESS_OWNER") && !userRole.equals("ROLE_ADMIN")) {
            log.warn("User {} does not have business owner role - access denied", userDetails.getUser().getEmail());
            return "redirect:/mypage?error=access_denied";
        }

        Optional<Store> store = storeService.getStoreByUser(userDetails.getUser().getUuid());
        if (store.isEmpty()) {
            return "redirect:/stores/register";
        }

        model.addAttribute("store", store.get());
        return "store/my-store";
    }

    @GetMapping("/edit")
    public String showEditForm(Model model, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        // Check if user has business owner role
        String userRole = userDetails.getUser().getRole().name();
        if (!userRole.equals("ROLE_BUSINESS_OWNER") && !userRole.equals("ROLE_ADMIN")) {
            log.warn("User {} does not have business owner role - access denied", userDetails.getUser().getEmail());
            return "redirect:/mypage?error=access_denied";
        }

        Optional<Store> store = storeService.getStoreByUser(userDetails.getUser().getUuid());
        if (store.isEmpty()) {
            return "redirect:/stores/register";
        }

        Store storeData = store.get();
        StoreRegistrationRequest request = new StoreRegistrationRequest();
        request.setName(storeData.getName());
        request.setAddress(storeData.getAddress());
        request.setContactNumber(storeData.getContactNumber());
        request.setBusinessHours(storeData.getBusinessHours());
        request.setCategory(storeData.getCategory());
        request.setMainImageUrl(storeData.getMainImageUrl());
        request.setSnsOrWebsiteLink(storeData.getSnsOrWebsiteLink());

        model.addAttribute("storeRegistrationRequest", request);
        model.addAttribute("store", storeData);
        return "store/edit";
    }

    @PostMapping("/edit")
    public String updateStore(
            @Valid @ModelAttribute StoreRegistrationRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        // Check if user has business owner role
        String userRole = userDetails.getUser().getRole().name();
        if (!userRole.equals("ROLE_BUSINESS_OWNER") && !userRole.equals("ROLE_ADMIN")) {
            log.warn("User {} does not have business owner role - access denied", userDetails.getUser().getEmail());
            return "redirect:/mypage?error=access_denied";
        }

        Optional<Store> storeOptional = storeService.getStoreByUser(userDetails.getUser().getUuid());
        if (storeOptional.isEmpty()) {
            return "redirect:/stores/register";
        }

        if (bindingResult.hasErrors()) {
            return "store/edit";
        }

        try {
            Store store = storeOptional.get();
            storeService.updateStore(store.getUuid(), request, userDetails.getUser().getUuid());
            redirectAttributes.addFlashAttribute("successMessage", "Store updated successfully!");
            return "redirect:/stores/my-store";
        } catch (Exception e) {
            log.error("Store update failed", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/stores/edit";
        }
    }

    @GetMapping("/{storeId}")
    public String storeDetail(@PathVariable("storeId") UUID storeId, Model model, 
                             @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        Optional<Store> storeOptional = storeService.getStoreById(storeId);
        if (storeOptional.isEmpty()) {
            return "redirect:/explore?error=store_not_found";
        }
        
        Store store = storeOptional.get();
        model.addAttribute("store", store);
        
        // Check if current user owns this store
        boolean isOwner = false;
        if (userDetails != null) {
            isOwner = store.getUser().getUuid().equals(userDetails.getUser().getUuid());
        }
        model.addAttribute("isOwner", isOwner);
        
        // Get menus by type
        List<Menu> collaborationMenus = menuService.getCollaborationMenus(store);
        List<Menu> signatureMenus = menuService.getSignatureMenus(store);
        
        model.addAttribute("collaborationMenus", collaborationMenus);
        model.addAttribute("signatureMenus", signatureMenus);
        
        // Get actual like and wishlist data
        long likeCount = storeLikeRepository.countByStore(store);
        long wishlistCount = wishlistRepository.countByStore(store);
        
        boolean isLiked = false;
        boolean isWishlisted = false;
        
        if (userDetails != null) {
            isLiked = storeLikeRepository.existsByUserAndStore(userDetails.getUser(), store);
            isWishlisted = wishlistRepository.existsByUserAndStore(userDetails.getUser(), store);
        }
        
        model.addAttribute("rating", 4.9); // Mock data - replace with actual review system
        model.addAttribute("reviewCount", 1114); // Mock data - replace with actual review system
        model.addAttribute("likeCount", likeCount);
        model.addAttribute("wishlistCount", wishlistCount);
        model.addAttribute("isLiked", isLiked);
        model.addAttribute("isWishlisted", isWishlisted);
        
        return "store/detail";
    }

}