package org.balanceus.topping.presentation.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.balanceus.topping.infrastructure.response.ApiResponseData;
import org.balanceus.topping.infrastructure.security.UserDetailsImpl;
import org.balanceus.topping.presentation.dto.LoginRequest;
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

@Controller
@RequiredArgsConstructor
public class SessionAuthController {

    private final AuthenticationManager authenticationManager;

    @PostMapping("/api/session/login")
    @ResponseBody
    public ResponseEntity<ApiResponseData<String>> sessionLogin(
            @RequestBody LoginRequest request, 
            HttpServletRequest httpRequest, 
            HttpServletResponse httpResponse) {
        
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Save authentication to session
            httpRequest.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
                SecurityContextHolder.getContext()
            );

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return ResponseEntity.ok(ApiResponseData.success("로그인 성공: " + userDetails.getUser().getUsername()));

        } catch (Exception e) {
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
}