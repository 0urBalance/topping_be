package org.balanceus.topping.presentation.controller;

import java.util.Optional;

import org.balanceus.topping.domain.model.SggCode;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.SggCodeRepository;
import org.balanceus.topping.domain.repository.UserRepository;
import org.balanceus.topping.infrastructure.response.ApiResponseData;
import org.balanceus.topping.infrastructure.security.UserDetailsImpl;
import org.balanceus.topping.presentation.dto.LoginRequest;
import org.balanceus.topping.presentation.dto.SignupRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final SggCodeRepository sggCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    // Template endpoints
    @GetMapping("/auth/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/auth/signup")
    public String signupPage() {
        return "auth/signup";
    }

    // API endpoints for session-based auth (handled by Spring Security)
    // Login is now handled by Spring Security's /login endpoint
    // This method can be used for additional login response if needed
    @PostMapping("/api/member/login-status")
    @ResponseBody
    public ResponseEntity<ApiResponseData<String>> loginStatus(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userDetails.getUser();
            return ResponseEntity.ok(ApiResponseData.success("로그인 성공: " + user.getUsername()));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseData.failure(401, "인증되지 않은 사용자"));
        }
    }

    @PostMapping("/api/member/signup")
    @ResponseBody
    public ResponseEntity<ApiResponseData<String>> signup(@RequestBody SignupRequest request) {
        try {
            // Validate terms agreement
            if (request.getTermsAgreement() == null || !request.getTermsAgreement()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponseData.failure(400, "이용약관 및 개인정보처리방침에 동의해주세요."));
            }

            // Validate sggCode
            if (request.getSggCode() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponseData.failure(400, "지역을 선택해주세요."));
            }

            // Check if sggCode exists
            Optional<SggCode> sggCodeOpt = sggCodeRepository.findById(request.getSggCode());
            if (sggCodeOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponseData.failure(400, "유효하지 않은 지역입니다."));
            }

            // Check if email already exists
            Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
            if (existingUser.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponseData.failure(409, "이미 등록된 이메일입니다."));
            }

            // Create new user
            SggCode sggCode = sggCodeOpt.get();
            
            User newUser = new User();
            newUser.setUsername(request.getUsername());
            newUser.setEmail(request.getEmail());
            newUser.setPassword(passwordEncoder.encode(request.getPassword()));
            newUser.setRole(request.getRole());
            newUser.setSggCode(sggCode);
            newUser.setTermsAgreement(request.getTermsAgreement());

            // Save user
            userRepository.save(newUser);

            return ResponseEntity.ok(ApiResponseData.success("회원가입이 완료되었습니다."));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseData.failure(500, "회원가입 중 오류가 발생했습니다."));
        }
    }

    // Logout is now handled by Spring Security's /logout endpoint
    // This method can be used for additional logout response if needed
    @PostMapping("/api/member/logout-status")
    @ResponseBody
    public ResponseEntity<ApiResponseData<String>> logoutStatus() {
        // Session logout is handled by Spring Security
        return ResponseEntity.ok(ApiResponseData.success("로그아웃되었습니다."));
    }
}