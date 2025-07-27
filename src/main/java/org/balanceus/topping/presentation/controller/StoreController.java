package org.balanceus.topping.presentation.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.application.dto.StoreRegistrationRequest;
import org.balanceus.topping.application.service.ImageUploadService;
import org.balanceus.topping.application.service.ProductService;
import org.balanceus.topping.application.service.StoreService;
import org.balanceus.topping.domain.model.Collaboration;
import org.balanceus.topping.domain.model.Product;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.StoreImage;
import org.balanceus.topping.domain.model.StoreLike;
import org.balanceus.topping.domain.repository.CollaborationRepository;
import org.balanceus.topping.domain.repository.ProductRepository;
import org.balanceus.topping.domain.repository.ReviewRepository;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
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
    private final ProductService productService;
    private final ImageUploadService imageUploadService;
    private final ProductRepository productRepository;
    private final CollaborationRepository collaborationRepository;
    private final ReviewRepository reviewRepository;
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
        
        Optional<Store> storeOptional;
        try {
            storeOptional = storeService.getStoreByIdWithProductsAndTags(storeId);
        } catch (Exception e) {
            log.warn("Failed to fetch store with menus and tags, falling back to simple query", e);
            storeOptional = storeService.getStoreById(storeId);
        }
        
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
        
        // Use Store entity helper methods for products - with null safety
        List<Product> popularProducts = store.getPopularProducts();
        List<Product> signatureProducts = store.getSignatureProducts();
        
        
        // Ensure lists are never null for template
        model.addAttribute("popularMenus", popularProducts != null ? popularProducts : new ArrayList<>());
        model.addAttribute("signatureMenus", signatureProducts != null ? signatureProducts : new ArrayList<>());
        
        // Get actual like and wishlist data
        long likeCount = storeLikeRepository.countByStore(store);
        long wishlistCount = wishlistRepository.countByStore(store);
        
        // Get collaboration product count
        long collabProductCount = productRepository.countByStoreAndCollaborationIsNotNull(store);
        
        // Get collaborating stores (stores that have accepted collaborations with this store's products)
        List<Collaboration> acceptedCollaborations = collaborationRepository.findByProductStoreAndStatus(store, Collaboration.CollaborationStatus.ACCEPTED);
        List<Store> collaboratingStores = acceptedCollaborations.stream()
            .map(collaboration -> collaboration.getApplicantProduct() != null ? collaboration.getApplicantProduct().getStore() : null)
            .filter(collaboratingStore -> collaboratingStore != null && !collaboratingStore.getUuid().equals(store.getUuid()))
            .distinct()
            .toList();
        
        boolean isLiked = false;
        boolean isWishlisted = false;
        
        if (userDetails != null) {
            isLiked = storeLikeRepository.existsByUserAndStore(userDetails.getUser(), store);
            isWishlisted = wishlistRepository.existsByUserAndStore(userDetails.getUser(), store);
        }
        
        // Get actual review data
        long reviewCount = reviewRepository.countByStoreAndIsActiveTrue(store);
        Double averageRating = reviewRepository.findAverageRatingByStoreAndIsActiveTrue(store);
        double rating = averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0;
        
        model.addAttribute("rating", rating);
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("likeCount", likeCount);
        model.addAttribute("wishlistCount", wishlistCount);
        model.addAttribute("collabProductCount", collabProductCount);
        model.addAttribute("collaboratingStores", collaboratingStores);
        model.addAttribute("isLiked", isLiked);
        model.addAttribute("isWishlisted", isWishlisted);
        
        return "store/detail";
    }

    @PostMapping("/upload-images")
    @ResponseBody
    public ApiResponseData<List<String>> uploadStoreImages(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "imageType", defaultValue = "GALLERY") String imageType,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (userDetails == null) {
            return ApiResponseData.failure(401, "Authentication required");
        }

        // Check if user has business owner role
        String userRole = userDetails.getUser().getRole().name();
        if (!userRole.equals("ROLE_BUSINESS_OWNER") && !userRole.equals("ROLE_ADMIN")) {
            return ApiResponseData.failure(403, "Access denied");
        }

        Optional<Store> storeOptional = storeService.getStoreByUser(userDetails.getUser().getUuid());
        if (storeOptional.isEmpty()) {
            return ApiResponseData.failure(404, "Store not found");
        }

        try {
            StoreImage.ImageType type = StoreImage.ImageType.valueOf(imageType.toUpperCase());
            List<String> uploadedPaths = imageUploadService.uploadStoreImages(
                    storeOptional.get(), files, type);
            
            return ApiResponseData.success(uploadedPaths);
        } catch (Exception e) {
            log.error("Store image upload failed", e);
            return ApiResponseData.failure(500, e.getMessage());
        }
    }

    @PostMapping("/delete-image/{imageId}")
    @ResponseBody
    public ApiResponseData<String> deleteStoreImage(
            @PathVariable("imageId") UUID imageId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (userDetails == null) {
            return ApiResponseData.failure(401, "Authentication required");
        }

        // Check if user has business owner role
        String userRole = userDetails.getUser().getRole().name();
        if (!userRole.equals("ROLE_BUSINESS_OWNER") && !userRole.equals("ROLE_ADMIN")) {
            return ApiResponseData.failure(403, "Access denied");
        }

        try {
            imageUploadService.deleteStoreImage(imageId);
            return ApiResponseData.success("Image deleted successfully");
        } catch (Exception e) {
            log.error("Store image deletion failed", e);
            return ApiResponseData.failure(500, e.getMessage());
        }
    }

    @PostMapping("/api/{storeId}/like")
    @ResponseBody
    public ApiResponseData<Map<String, Object>> toggleStoreLike(
            @PathVariable("storeId") UUID storeId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (userDetails == null) {
            return ApiResponseData.failure(401, "Authentication required");
        }

        try {
            Optional<Store> storeOptional = storeService.getStoreById(storeId);
            if (storeOptional.isEmpty()) {
                return ApiResponseData.failure(404, "Store not found");
            }

            Store store = storeOptional.get();
            
            // Check if user already liked this store
            Optional<StoreLike> existingLike = storeLikeRepository.findByUserAndStore(userDetails.getUser(), store);
            
            boolean isLiked;
            if (existingLike.isPresent()) {
                // Unlike: remove existing like
                storeLikeRepository.delete(existingLike.get());
                isLiked = false;
                log.debug("User {} unliked store {}", userDetails.getUser().getEmail(), store.getName());
            } else {
                // Like: create new like
                StoreLike newLike = new StoreLike();
                newLike.setUser(userDetails.getUser());
                newLike.setStore(store);
                storeLikeRepository.save(newLike);
                isLiked = true;
                log.debug("User {} liked store {}", userDetails.getUser().getEmail(), store.getName());
            }
            
            // Get updated like count
            long likeCount = storeLikeRepository.countByStore(store);
            
            // Return response data
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("isLiked", isLiked);
            responseData.put("likeCount", likeCount);
            
            return ApiResponseData.success(responseData);
            
        } catch (Exception e) {
            log.error("Failed to toggle store like for user: {}, store: {}", 
                     userDetails.getUser().getEmail(), storeId, e);
            return ApiResponseData.failure(500, "Failed to update like status");
        }
    }

}