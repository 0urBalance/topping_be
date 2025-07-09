package org.balanceus.topping.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.balanceus.topping.application.service.PasswordRecoveryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auth/password-recovery")
public class PasswordRecoveryController {
    
    private final PasswordRecoveryService passwordRecoveryService;
    
    @GetMapping("/find")
    public String showFindPasswordPage() {
        return "auth/password-recovery/find";
    }
    
    @PostMapping("/send-code")
    public String sendVerificationCode(@RequestParam String email, 
                                     RedirectAttributes redirectAttributes) {
        try {
            passwordRecoveryService.sendVerificationCode(email);
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("successMessage", 
                "인증코드가 이메일로 전송되었습니다. 이메일을 확인해주세요.");
            return "redirect:/auth/password-recovery/verify";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/auth/password-recovery/find";
        }
    }
    
    @GetMapping("/verify")
    public String showVerifyCodePage(Model model) {
        if (!model.containsAttribute("email")) {
            return "redirect:/auth/password-recovery/find";
        }
        return "auth/password-recovery/verify";
    }
    
    @PostMapping("/verify-code")
    public String verifyCode(@RequestParam String email,
                           @RequestParam String code,
                           RedirectAttributes redirectAttributes) {
        try {
            boolean isValid = passwordRecoveryService.verifyCode(email, code);
            if (isValid) {
                redirectAttributes.addFlashAttribute("email", email);
                redirectAttributes.addFlashAttribute("code", code);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "인증이 완료되었습니다. 새로운 비밀번호를 설정해주세요.");
                return "redirect:/auth/password-recovery/reset";
            } else {
                redirectAttributes.addFlashAttribute("email", email);
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "유효하지 않은 인증코드입니다. 다시 시도해주세요.");
                return "redirect:/auth/password-recovery/verify";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/auth/password-recovery/verify";
        }
    }
    
    @GetMapping("/reset")
    public String showResetPasswordPage(Model model) {
        if (!model.containsAttribute("email") || !model.containsAttribute("code")) {
            return "redirect:/auth/password-recovery/find";
        }
        return "auth/password-recovery/reset";
    }
    
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String email,
                              @RequestParam String code,
                              @RequestParam String newPassword,
                              @RequestParam String confirmPassword,
                              RedirectAttributes redirectAttributes) {
        try {
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("email", email);
                redirectAttributes.addFlashAttribute("code", code);
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "비밀번호가 일치하지 않습니다.");
                return "redirect:/auth/password-recovery/reset";
            }
            
            if (newPassword.length() < 8) {
                redirectAttributes.addFlashAttribute("email", email);
                redirectAttributes.addFlashAttribute("code", code);
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "비밀번호는 8자 이상이어야 합니다.");
                return "redirect:/auth/password-recovery/reset";
            }
            
            passwordRecoveryService.resetPassword(email, code, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", 
                "비밀번호가 성공적으로 변경되었습니다. 새로운 비밀번호로 로그인해주세요.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("code", code);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/auth/password-recovery/reset";
        }
    }
}