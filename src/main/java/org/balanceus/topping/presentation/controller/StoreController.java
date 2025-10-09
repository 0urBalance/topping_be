package org.balanceus.topping.presentation.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.application.dto.StoreDetailView;
import org.balanceus.topping.application.dto.StoreEngagementResult;
import org.balanceus.topping.application.dto.StoreForm;
import org.balanceus.topping.application.dto.StoreRegistrationRequest;
import org.balanceus.topping.application.exception.ApplicationErrorCode;
import org.balanceus.topping.application.exception.ApplicationException;
import org.balanceus.topping.application.service.ImageUploadService;
import org.balanceus.topping.application.service.StoreEngagementService;
import org.balanceus.topping.application.service.StoreService;
import org.balanceus.topping.application.service.StoreViewService;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.domain.model.StoreImage;
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
    private final StoreViewService storeViewService;
    private final StoreEngagementService storeEngagementService;
    private final ImageUploadService imageUploadService;

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

        model.addAttribute("storeForm", new StoreForm());
        return "store/register";
    }

    @PostMapping("/register")
    public String registerStore(
            @Valid @ModelAttribute StoreForm storeForm,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {

        log.info("Store registration request received. User: {}", 
                userDetails != null ? userDetails.getUser().getEmail() : "null");

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
            model.addAttribute("storeForm", storeForm);
            return "store/register";
        }

        try {
            Store store = storeService.registerStore(storeForm, userDetails.getUser().getUuid());
            log.info("Store registered successfully. Store ID: {}", store.getUuid());

            redirectAttributes.addFlashAttribute("successMessage", "Store registered successfully!");
            return "redirect:/stores/setup-images";
        } catch (Exception e) {
            log.error("Store registration failed", e);
            model.addAttribute("storeForm", storeForm);
            model.addAttribute("errorMessage", "스토어 등록 중 오류가 발생했습니다. 입력 정보를 확인한 후 다시 시도해주세요.");
            return "store/register";
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
            StoreForm form = convertToStoreForm(request);
            storeService.registerStore(form, userDetails.getUser().getUuid());
            return ApiResponseData.success("Store registered successfully");
        } catch (Exception e) {
            log.error("Store registration failed", e);
            return ApiResponseData.failure(500, "스토어 등록 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
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
        StoreForm storeForm = convertToStoreForm(storeData);

        model.addAttribute("storeForm", storeForm);
        model.addAttribute("store", storeData);
        return "store/edit";
    }

    @PostMapping("/edit")
    public String updateStore(
            @Valid @ModelAttribute StoreForm storeForm,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {

        log.debug("Store edit request received. User: {}", 
                userDetails != null ? userDetails.getUser().getEmail() : "null");

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
            // Re-populate store data for display of existing images
            Optional<Store> currentStore = storeService.getStoreByUser(userDetails.getUser().getUuid());
            if (currentStore.isPresent()) {
                model.addAttribute("store", currentStore.get());
            }
            model.addAttribute("storeForm", storeForm);
            return "store/edit";
        }

        try {
            Store store = storeOptional.get();
            Store updatedStore = storeService.updateStore(store.getUuid(), storeForm, userDetails.getUser().getUuid());
            log.info("Store updated successfully. Store ID: {}", updatedStore.getUuid());

            redirectAttributes.addFlashAttribute("successMessage", "Store updated successfully!");
            return "redirect:/stores/my-store";
        } catch (Exception e) {
            log.error("Store update failed", e);
            redirectAttributes.addFlashAttribute("errorMessage", "스토어 정보 수정 중 오류가 발생했습니다. 입력 정보를 확인한 후 다시 시도해주세요.");
            return "redirect:/stores/edit";
        }
    }

    @GetMapping("/{storeId}")
    public String storeDetail(@PathVariable("storeId") UUID storeId, Model model,
                             @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UUID requesterUuid = userDetails != null ? userDetails.getUser().getUuid() : null;
        try {
            StoreDetailView detail = storeViewService.getStoreDetail(storeId, requesterUuid);
            model.addAttribute("store", detail.getStore());
            model.addAttribute("isOwner", detail.isOwner());
            model.addAttribute("popularMenus", detail.getPopularMenus());
            model.addAttribute("signatureMenus", detail.getSignatureMenus());
            model.addAttribute("likeCount", detail.getLikeCount());
            model.addAttribute("wishlistCount", detail.getWishlistCount());
            model.addAttribute("collabProductCount", detail.getCollaborationProductCount());
            model.addAttribute("collaboratingStores", detail.getCollaboratingStores());
            model.addAttribute("isLiked", detail.isLiked());
            model.addAttribute("isWishlisted", detail.isWishlisted());
            model.addAttribute("reviewCount", detail.getReviewCount());
            model.addAttribute("rating", detail.getRating());
            return "store/detail";
        } catch (ApplicationException e) {
            if (e.getErrorCode() == ApplicationErrorCode.NOT_FOUND) {
                return "redirect:/explore?error=store_not_found";
            }
            throw e;
        }
    }

    @GetMapping("/setup-images")
    public String showImageSetup(Model model, @AuthenticationPrincipal UserDetailsImpl userDetails) {
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
            log.warn("User {} tried to access image setup but has no store", userDetails.getUser().getEmail());
            return "redirect:/stores/register";
        }

        model.addAttribute("store", store.get());
        return "store/setup-images";
    }

    @GetMapping("/api/{storeUuid}/collaborating-stores")
    @ResponseBody
    public ApiResponseData<List<Map<String, Object>>> getCollaboratingStores(@PathVariable UUID storeUuid) {
        try {
            StoreDetailView detail = storeViewService.getStoreDetail(storeUuid, null);
            List<Map<String, Object>> storeData = detail.getCollaboratingStores()
                .stream()
                .map(this::toCollaboratingStoreMap)
                .toList();

            return ApiResponseData.success(storeData);
        } catch (ApplicationException e) {
            if (e.getErrorCode() == ApplicationErrorCode.NOT_FOUND) {
                return ApiResponseData.failure(404, "가게를 찾을 수 없습니다.");
            }
            log.error("Failed to get collaborating stores for store {}", storeUuid, e);
            return ApiResponseData.failure(500, "콜라보 가게 정보를 불러오는데 실패했습니다.");
        } catch (Exception e) {
            log.error("Failed to get collaborating stores for store {}", storeUuid, e);
            return ApiResponseData.failure(500, "콜라보 가게 정보를 불러오는데 실패했습니다.");
        }
    }

    @PostMapping("/setup-images/complete")
    public String completeImageSetup(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                   RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        redirectAttributes.addFlashAttribute("successMessage", "스토어 설정이 완료되었습니다!");
        return "redirect:/stores/my-store";
    }

    @PostMapping("/setup-images/skip")
    public String skipImageSetup(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        redirectAttributes.addFlashAttribute("infoMessage", "이미지는 나중에 추가할 수 있습니다.");
        return "redirect:/stores/my-store";
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
            return ApiResponseData.failure(500, "이미지 업로드 중 오류가 발생했습니다. 파일 크기와 형식을 확인한 후 다시 시도해주세요.");
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
            return ApiResponseData.failure(500, "이미지 삭제 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
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
            StoreEngagementResult result = storeEngagementService.toggleStoreLike(
                storeId, userDetails.getUser().getUuid());

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("isLiked", result.active());
            responseData.put("likeCount", result.count());

            return ApiResponseData.success(responseData);
            
        } catch (Exception e) {
            log.error("Failed to toggle store like for user: {}, store: {}", 
                     userDetails.getUser().getEmail(), storeId, e);
            return ApiResponseData.failure(500, "Failed to update like status");
        }
    }

    @PostMapping("/api/{storeId}/wishlist/toggle")
    @ResponseBody
    public ApiResponseData<Map<String, Object>> toggleStoreWishlist(
            @PathVariable("storeId") UUID storeId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (userDetails == null) {
            return ApiResponseData.failure(401, "Authentication required");
        }

        try {
            StoreEngagementResult result = storeEngagementService.toggleStoreWishlist(
                storeId, userDetails.getUser().getUuid());

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("isWishlisted", result.active());
            responseData.put("wishlistCount", result.count());

            return ApiResponseData.success(responseData);
            
        } catch (Exception e) {
            log.error("Failed to toggle store wishlist for user: {}, store: {}", 
                     userDetails.getUser().getEmail(), storeId, e);
            return ApiResponseData.failure(500, "Failed to update wishlist status");
        }
    }

    @GetMapping("/api/{storeId}/wishlist/status")
    @ResponseBody
    public ApiResponseData<Boolean> getStoreWishlistStatus(
            @PathVariable("storeId") UUID storeId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (userDetails == null) {
            return ApiResponseData.failure(401, "Authentication required");
        }

        try {
            boolean isWishlisted = storeEngagementService.isStoreWishlisted(
                storeId, userDetails.getUser().getUuid());

            return ApiResponseData.success(isWishlisted);
            
        } catch (Exception e) {
            log.error("Failed to get store wishlist status for user: {}, store: {}", 
                     userDetails.getUser().getEmail(), storeId, e);
            return ApiResponseData.failure(500, "Failed to get wishlist status");
        }
    }

    private Map<String, Object> toCollaboratingStoreMap(Store collaboratingStore) {
        Map<String, Object> storeMap = new HashMap<>();
        storeMap.put("uuid", collaboratingStore.getUuid().toString());
        storeMap.put("name", collaboratingStore.getName());
        storeMap.put("category", collaboratingStore.getCategory() != null
            ? collaboratingStore.getCategory().getDisplayName()
            : null);
        storeMap.put("address", collaboratingStore.getAddress());
        storeMap.put("mainImagePath", collaboratingStore.getMainImage() != null
            ? collaboratingStore.getMainImage().getImagePath()
            : collaboratingStore.getMainImageUrl());
        return storeMap;
    }

    private StoreForm convertToStoreForm(StoreRegistrationRequest request) {
        StoreForm form = new StoreForm();
        form.setName(request.getName());
        form.setCategory(request.getCategory());
        form.setAddress(request.getAddress());
        form.setContactNumber(request.getContactNumber());
        form.setBusinessHours(request.getBusinessHours());
        form.setMainImageUrl(request.getMainImageUrl());
        form.setSnsOrWebsiteLink(request.getSnsOrWebsiteLink());
        return form;
    }

    // Helper method to convert Store to StoreForm for editing
    private StoreForm convertToStoreForm(Store store) {
        StoreForm form = new StoreForm();
        form.setName(store.getName());
        form.setCategoryFromEnum(store.getCategory());
        form.setAddress(store.getAddress());
        form.setDescription(store.getDescription());
        form.setContactNumber(store.getContactNumber());
        form.setBusinessHours(store.getBusinessHours());
        form.setMainImageUrl(store.getMainImageUrl());
        form.setSnsOrWebsiteLink(store.getSnsOrWebsiteLink());
        form.setIsCollaborationOpen(store.getIsCollaborationOpen());
        
        // Convert tags list to comma-separated string
        form.setTagsFromList(store.getTags());
        
        return form;
    }

}
