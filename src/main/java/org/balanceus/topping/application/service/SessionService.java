package org.balanceus.topping.application.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.balanceus.topping.infrastructure.exception.AuthenticationFailedException;
import org.balanceus.topping.infrastructure.security.UserDetailsImpl;
import org.balanceus.topping.presentation.dto.LoginRequest;
import org.balanceus.topping.presentation.dto.SessionLoginResponse;
import org.balanceus.topping.presentation.dto.SessionUserResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final AuthenticationManager authenticationManager;

    public SessionLoginResponse authenticateAndCreateSession(LoginRequest request, HttpServletRequest httpRequest) {
        log.debug("Attempting session login for user: {}", request.getEmail());
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            log.debug("Authentication successful for user: {}", request.getEmail());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            httpRequest.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
                SecurityContextHolder.getContext()
            );

            log.debug("Session created with ID: {}", httpRequest.getSession().getId());
            
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            log.info("Login successful for user: {}", userDetails.getUser().getUsername());
            
            String redirectUrl = determineRedirectUrl(httpRequest);
            
            SessionUserResponse userResponse = createSessionUserResponse(userDetails);
            
            return new SessionLoginResponse(
                "로그인 성공: " + userDetails.getUser().getUsername(),
                redirectUrl,
                userResponse
            );

        } catch (Exception e) {
            log.error("Login failed for user: {}", request.getEmail(), e);
            throw new AuthenticationFailedException(
                "로그인에 실패했습니다. 이메일과 비밀번호를 확인한 후 다시 시도해주세요."
            );
        }
    }

    public void invalidateSession(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
            log.debug("Session invalidated for user: {}", auth.getName());
        }
    }

    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser");
    }

    public SessionUserResponse getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (!isAuthenticated()) {
            return null;
        }
        
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return createSessionUserResponse(userDetails);
    }

    public String getSessionStatus(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String sessionId = request.getSession().getId();
        
        log.debug("Session status check - Session ID: {}, Authentication: {}", sessionId, auth);
        
        if (isAuthenticated()) {
            return "인증됨 - 사용자: " + auth.getName();
        } else {
            return "인증되지 않음";
        }
    }

    private String determineRedirectUrl(HttpServletRequest request) {
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        SavedRequest savedRequest = requestCache.getRequest(request, null);
        
        if (savedRequest != null) {
            String redirectUrl = savedRequest.getRedirectUrl();
            log.debug("Found saved request, redirect URL: {}", redirectUrl);
            return redirectUrl;
        }
        
        return "/";
    }

    private SessionUserResponse createSessionUserResponse(UserDetailsImpl userDetails) {
        return new SessionUserResponse(
            userDetails.getUser().getUuid(),
            userDetails.getUser().getUsername(),
            userDetails.getUser().getEmail(),
            userDetails.getUser().getRole().name()
        );
    }
}