package org.balanceus.topping.infrastructure.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Component
@Slf4j
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final RequestCache requestCache = new HttpSessionRequestCache();

    public CustomAuthenticationSuccessHandler() {
        // Set default target URL as home page
        setDefaultTargetUrl("/");
        // Use saved request if available and valid
        setAlwaysUseDefaultTargetUrl(false);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {

        String username = authentication.getName();
        log.debug("Login successful for user: {}", username);

        // Check saved request and validate URL
        SavedRequest savedRequest = requestCache.getRequest(request, response);

        if (savedRequest != null) {
            String targetUrl = savedRequest.getRedirectUrl();

            if (isValidRedirectUrl(targetUrl)) {
                log.debug("Redirecting user {} to saved request URL: {}", username, targetUrl);
            } else {
                // Invalid URL - remove saved request and go to default
                log.debug("Invalid saved request URL: {}, redirecting to default", targetUrl);
                requestCache.removeRequest(request, response);
            }
        } else {
            log.debug("No saved request found, redirecting user {} to default URL: {}", username, getDefaultTargetUrl());
        }

        // Let the parent class handle the actual redirection logic
        super.onAuthenticationSuccess(request, response, authentication);
    }

    /**
     * Validates if the URL is a valid page redirect target.
     * Only allows actual page URLs, not API endpoints or invalid paths.
     */
    private boolean isValidRedirectUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        // Reject API endpoints
        if (url.contains("/api/")) {
            return false;
        }

        // Reject URLs with suspicious patterns
        if (url.contains("/wishlist/status") || url.contains("?continue")) {
            return false;
        }

        // Allow known page prefixes
        String[] validPrefixes = {
            "/mypage", "/products", "/stores", "/chat",
            "/collaborations", "/collabo", "/support", "/explore"
        };

        for (String prefix : validPrefixes) {
            if (url.contains(prefix)) {
                return true;
            }
        }

        // Allow root path
        if (url.endsWith("/") || url.matches(".*/[^/]*$")) {
            return true;
        }

        return false;
    }
}
