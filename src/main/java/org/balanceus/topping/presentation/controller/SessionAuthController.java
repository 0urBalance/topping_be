package org.balanceus.topping.presentation.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.balanceus.topping.application.service.SessionService;
import org.balanceus.topping.infrastructure.response.ApiResponseData;
import org.balanceus.topping.infrastructure.utils.AuthUtils;
import org.balanceus.topping.infrastructure.utils.SessionUtils;
import org.balanceus.topping.presentation.dto.LoginRequest;
import org.balanceus.topping.presentation.dto.SessionLoginResponse;
import org.balanceus.topping.presentation.dto.SessionUserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class SessionAuthController {

    private final SessionService sessionService;

    @PostMapping("/api/session/login")
    @ResponseBody
    public ResponseEntity<ApiResponseData<SessionLoginResponse>> sessionLogin(
            @RequestBody LoginRequest request, 
            HttpServletRequest httpRequest, 
            HttpServletResponse httpResponse) {
        
        log.debug("Session login attempt for user: {}", AuthUtils.maskEmail(request.getEmail()));
        
        SessionLoginResponse response = sessionService.authenticateAndCreateSession(request, httpRequest);
        return ResponseEntity.ok(ApiResponseData.success(response));
    }

    @PostMapping("/api/session/logout")
    @ResponseBody
    public ResponseEntity<ApiResponseData<String>> sessionLogout(
            HttpServletRequest request, 
            HttpServletResponse response) {
        
        log.debug("Session logout request from session: {}", 
                 SessionUtils.maskSessionId(SessionUtils.getSessionId(request)));
        
        sessionService.invalidateSession(request, response);
        return ResponseEntity.ok(ApiResponseData.success("로그아웃 완료"));
    }
    
    @PostMapping("/api/session/status")
    @ResponseBody
    public ResponseEntity<ApiResponseData<String>> sessionStatus(HttpServletRequest request) {
        log.debug("Session status check for session: {}", 
                 SessionUtils.maskSessionId(SessionUtils.getSessionId(request)));
        
        String status = sessionService.getSessionStatus(request);
        
        if (sessionService.isAuthenticated()) {
            return ResponseEntity.ok(ApiResponseData.success(status));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseData.failure(401, status));
        }
    }

    @GetMapping("/api/session/check")
    @ResponseBody
    public ResponseEntity<ApiResponseData<String>> sessionCheck(HttpServletRequest request) {
        log.debug("Session check for session: {}", 
                 SessionUtils.maskSessionId(SessionUtils.getSessionId(request)));
        
        if (sessionService.isAuthenticated()) {
            return ResponseEntity.ok(ApiResponseData.success("세션 유효"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseData.failure(401, "세션 만료"));
        }
    }

    @GetMapping("/api/session/user")
    @ResponseBody
    public ResponseEntity<ApiResponseData<SessionUserResponse>> getSessionUser(HttpServletRequest request) {
        log.debug("Session user request from session: {}", 
                 SessionUtils.maskSessionId(SessionUtils.getSessionId(request)));
        
        SessionUserResponse userResponse = sessionService.getCurrentUser();
        
        if (userResponse != null) {
            return ResponseEntity.ok(ApiResponseData.success(userResponse));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseData.failure(401, "인증되지 않음"));
        }
    }
}