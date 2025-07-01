package org.balanceus.topping.presentation.controller;

import java.util.Optional;

import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.UserRepository;
import org.balanceus.topping.infrastructure.response.ApiResponseData;
import org.balanceus.topping.infrastructure.security.JwtService;
import org.balanceus.topping.infrastructure.security.UserDetailsImpl;
import org.balanceus.topping.presentation.dto.AuthResponse;
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
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
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

    // API endpoints
    @PostMapping("/api/member/login")
    @ResponseBody
    public ResponseEntity<ApiResponseData<AuthResponse>> login(@RequestBody LoginRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userDetails.getUser();

            // Generate JWT token
            String accessToken = jwtService.generateToken(userDetails);

            // Create response
            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken(accessToken)
                    .tokenType("Bearer")
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .expiresIn(jwtService.getExpirationTime())
                    .build();

            return ResponseEntity.ok(ApiResponseData.success(authResponse));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseData.failure(401, "로그인 정보 불일치"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseData.failure(500, "서버 오류가 발생했습니다."));
        }
    }

    @PostMapping("/api/member/signup")
    @ResponseBody
    public ResponseEntity<ApiResponseData<String>> signup(@RequestBody SignupRequest request) {
        try {
            // Check if email already exists
            Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
            if (existingUser.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponseData.failure(409, "이미 등록된 이메일입니다."));
            }

            // Create new user
            User newUser = new User();
            newUser.setUsername(request.getUsername());
            newUser.setEmail(request.getEmail());
            newUser.setPassword(passwordEncoder.encode(request.getPassword()));
            newUser.setRole(request.getRole());

            // Save user
            userRepository.save(newUser);

            return ResponseEntity.ok(ApiResponseData.success("회원가입이 완료되었습니다."));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseData.failure(500, "회원가입 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/api/member/logout")
    @ResponseBody
    public ResponseEntity<ApiResponseData<String>> logout() {
        // Since we're using stateless JWT, logout is handled on the client side
        // by removing the token from localStorage
        return ResponseEntity.ok(ApiResponseData.success("로그아웃되었습니다."));
    }
}