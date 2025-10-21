package org.balanceus.topping.presentation.controller;

import org.balanceus.topping.application.service.AuthService;
import org.balanceus.topping.infrastructure.response.ApiResponseData;
import org.balanceus.topping.infrastructure.security.UserDetailsImpl;
import org.balanceus.topping.infrastructure.utils.AuthUtils;
import org.balanceus.topping.presentation.dto.EmailCheckRequest;
import org.balanceus.topping.presentation.dto.EmailCheckResponse;
import org.balanceus.topping.presentation.dto.SignupRequest;
import org.balanceus.topping.presentation.dto.SignupResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @Value("${KAKAO.REST-API-KEY}")
    private String kakaoRestApiKey;
    
    @Value("${KAKAO.REDIRECT-URI}")
    private String kakaoRedirectUri;

    // Template endpoints
    @GetMapping("/auth/login")
    public String loginPage(Model model) {
        model.addAttribute("kakaoRestApiKey", kakaoRestApiKey);
        model.addAttribute("kakaoRedirectUri", kakaoRedirectUri);
        return "auth/login";
    }

    @GetMapping("/auth/signup")
    public String signupPage() {
        return "redirect:/auth/signup/step1";
    }

    // Note: Session-based authentication endpoints are handled by SessionAuthController
    // - /api/session/login for login
    // - /api/session/logout for logout
    // - /api/session/status for login status
    // - /api/session/user for user information

    @PostMapping("/api/member/signup")
    @ResponseBody
    public ResponseEntity<ApiResponseData<SignupResponse>> signup(@RequestBody SignupRequest request) {
        log.debug("User signup request for email: {}", AuthUtils.maskEmail(request.getEmail()));
        
        SignupResponse response = authService.registerUser(request);
        return ResponseEntity.ok(ApiResponseData.success(response));
    }

    @PostMapping("/api/member/check-email")
    @ResponseBody
    public ResponseEntity<ApiResponseData<EmailCheckResponse>> checkEmail(@RequestBody EmailCheckRequest request) {
        log.debug("Email availability check for: {}", AuthUtils.maskEmail(request.getEmail()));
        
        EmailCheckResponse response = authService.checkEmailAvailability(request);
        return ResponseEntity.ok(ApiResponseData.success(response));
    }

}