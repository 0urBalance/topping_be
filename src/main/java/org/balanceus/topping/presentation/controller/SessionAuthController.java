package org.balanceus.topping.presentation.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.balanceus.topping.infrastructure.response.ApiResponseData;
import org.balanceus.topping.infrastructure.security.UserDetailsImpl;
import org.balanceus.topping.presentation.dto.LoginRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class SessionAuthController {

    private final AuthenticationManager authenticationManager;

    @PostMapping("/api/session/login")
    @ResponseBody
    public ResponseEntity<ApiResponseData<String>> sessionLogin(
            @RequestBody LoginRequest request, 
            HttpServletRequest httpRequest, 
            HttpServletResponse httpResponse) {
        
        try {
            log.debug("Attempting session login for user: {}", request.getEmail());
            
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            log.debug("Authentication successful for user: {}", request.getEmail());

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Save authentication to session
            httpRequest.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
                SecurityContextHolder.getContext()
            );

            log.debug("Session created with ID: {}", httpRequest.getSession().getId());
            
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            log.debug("Login successful for user: {}", userDetails.getUser().getUsername());
            
            return ResponseEntity.ok(ApiResponseData.success("로그인 성공: " + userDetails.getUser().getUsername()));

        } catch (Exception e) {
            log.error("Login failed for user: {}", request.getEmail(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponseData.failure(400, "로그인 실패: " + e.getMessage()));
        }
    }

    @PostMapping("/api/session/logout")
    @ResponseBody
    public ResponseEntity<ApiResponseData<String>> sessionLogout(
            HttpServletRequest request, 
            HttpServletResponse response) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        
        return ResponseEntity.ok(ApiResponseData.success("로그아웃 완료"));
    }
    
    @PostMapping("/api/session/status")
    @ResponseBody
    public ResponseEntity<ApiResponseData<String>> sessionStatus(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String sessionId = request.getSession().getId();
        
        log.debug("Session status check - Session ID: {}, Authentication: {}", sessionId, auth);
        
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return ResponseEntity.ok(ApiResponseData.success("인증됨 - 사용자: " + auth.getName()));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseData.failure(401, "인증되지 않음"));
        }
    }
}