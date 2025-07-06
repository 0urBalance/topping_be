package org.balanceus.topping.presentation.controller;

import java.util.Optional;

import org.balanceus.topping.application.dto.StoreRegistrationRequest;
import org.balanceus.topping.application.service.StoreService;
import org.balanceus.topping.domain.model.Store;
import org.balanceus.topping.infrastructure.response.ApiResponseData;
import org.balanceus.topping.infrastructure.security.UserDetailsImpl;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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

    @GetMapping("/register")
    public String showRegistrationForm(Model model, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
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
}