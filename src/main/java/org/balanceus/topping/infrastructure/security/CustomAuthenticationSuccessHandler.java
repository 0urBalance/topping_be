package org.balanceus.topping.infrastructure.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Component
@Slf4j
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    public CustomAuthenticationSuccessHandler() {
        // Set default target URL as home page
        setDefaultTargetUrl("/");
        // Always use default target URL if no saved request exists
        setAlwaysUseDefaultTargetUrl(false);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        
        String username = authentication.getName();
        log.debug("Login successful for user: {}", username);
        
        // Check if there's a saved request (user was redirected to login from a protected page)
        String savedRequestUrl = getSavedRequestUrl(request);
        
        if (savedRequestUrl != null) {
            log.debug("Redirecting user {} to saved request URL: {}", username, savedRequestUrl);
        } else {
            log.debug("No saved request found, redirecting user {} to default URL: {}", username, getDefaultTargetUrl());
        }
        
        // Let the parent class handle the actual redirection logic
        super.onAuthenticationSuccess(request, response, authentication);
    }
    
    private String getSavedRequestUrl(HttpServletRequest request) {
        // Get the saved request from session (if any)
        var savedRequest = request.getSession().getAttribute("SPRING_SECURITY_SAVED_REQUEST");
        if (savedRequest != null) {
            return savedRequest.toString();
        }
        return null;
    }
}