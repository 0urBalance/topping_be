package org.balanceus.topping.presentation.controller;

import java.security.Principal;

import org.balanceus.topping.application.service.UserService;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.UserRepository;
import org.balanceus.topping.presentation.dto.PasswordChangeForm;
import org.balanceus.topping.presentation.dto.UserEditForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserEditController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/edit")
    public String editForm(Model model, Principal principal) {
        log.debug("User edit form accessed - Principal: {}", principal);
        
        if (principal == null) {
            log.warn("Principal is null - redirecting to login");
            return "redirect:/auth/login";
        }

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Create form with current user data
        UserEditForm userEditForm = new UserEditForm();
        userEditForm.setUsername(user.getUsername());
        userEditForm.setPhoneNumber(user.getPhoneNumber());
        
        PasswordChangeForm passwordChangeForm = new PasswordChangeForm();
        
        model.addAttribute("user", user);
        model.addAttribute("userEditForm", userEditForm);
        model.addAttribute("passwordChangeForm", passwordChangeForm);
        
        return "users/edit";
    }

    @PostMapping("/edit")
    public String updateProfile(@Valid @ModelAttribute UserEditForm userEditForm,
                               BindingResult bindingResult,
                               Model model,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        log.debug("User profile update requested - Principal: {}", principal);
        
        if (principal == null) {
            return "redirect:/auth/login";
        }

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check for phone number uniqueness if it's being changed
        if (userEditForm.getPhoneNumber() != null && !userEditForm.getPhoneNumber().trim().isEmpty()) {
            String newPhoneNumber = userEditForm.getPhoneNumber().trim();
            if (!newPhoneNumber.equals(user.getPhoneNumber()) && 
                userService.existsByPhoneNumber(newPhoneNumber)) {
                bindingResult.rejectValue("phoneNumber", "duplicate", "이미 사용 중인 전화번호입니다.");
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("userEditForm", userEditForm);
            model.addAttribute("passwordChangeForm", new PasswordChangeForm());
            return "users/edit";
        }

        try {
            userService.updateUserProfile(user.getUuid(), 
                                        userEditForm.getUsername(), 
                                        userEditForm.getPhoneNumber());
            
            redirectAttributes.addFlashAttribute("message", "프로필이 성공적으로 업데이트되었습니다.");
            log.info("User profile updated successfully - User: {}", user.getEmail());
            
        } catch (Exception e) {
            log.error("Failed to update user profile", e);
            redirectAttributes.addFlashAttribute("error", "프로필 업데이트 중 오류가 발생했습니다.");
        }

        return "redirect:/mypage/profile";
    }

    @PostMapping("/edit/password")
    public String changePassword(@Valid @ModelAttribute PasswordChangeForm passwordChangeForm,
                                BindingResult bindingResult,
                                Model model,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        log.debug("Password change requested - Principal: {}", principal);
        
        if (principal == null) {
            return "redirect:/auth/login";
        }

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Additional validation for password confirmation
        if (!passwordChangeForm.isPasswordMatching()) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        if (bindingResult.hasErrors()) {
            UserEditForm userEditForm = new UserEditForm();
            userEditForm.setUsername(user.getUsername());
            userEditForm.setPhoneNumber(user.getPhoneNumber());
            
            model.addAttribute("user", user);
            model.addAttribute("userEditForm", userEditForm);
            model.addAttribute("passwordChangeForm", passwordChangeForm);
            return "users/edit";
        }

        try {
            userService.updatePassword(user.getUuid(), 
                                     passwordChangeForm.getCurrentPassword(), 
                                     passwordChangeForm.getNewPassword());
            
            redirectAttributes.addFlashAttribute("message", "비밀번호가 성공적으로 변경되었습니다.");
            log.info("Password changed successfully - User: {}", user.getEmail());
            
        } catch (IllegalArgumentException e) {
            log.warn("Password change failed - Invalid current password for user: {}", user.getEmail());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Failed to change password", e);
            redirectAttributes.addFlashAttribute("error", "비밀번호 변경 중 오류가 발생했습니다.");
        }

        return "redirect:/mypage/profile";
    }
}