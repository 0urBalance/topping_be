package org.balanceus.topping.infrastructure.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.util.StringUtils;

@Slf4j
public class SessionUtils {

    private SessionUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * 세션에 인증 정보를 저장합니다.
     */
    public static void saveAuthenticationToSession(HttpServletRequest request, Authentication authentication) {
        if (request == null || authentication == null) {
            log.warn("Cannot save authentication to session: request or authentication is null");
            return;
        }
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        HttpSession session = request.getSession(true);
        session.setAttribute(
            HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
            SecurityContextHolder.getContext()
        );
        
        log.debug("Authentication saved to session: {}", session.getId());
    }

    /**
     * 세션에서 인증 정보를 제거합니다.
     */
    public static void removeAuthenticationFromSession(HttpServletRequest request) {
        if (request == null) {
            return;
        }
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
            log.debug("Authentication removed from session: {}", session.getId());
        }
    }

    /**
     * 세션이 유효한지 확인합니다.
     */
    public static boolean isSessionValid(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        
        try {
            // 세션 ID 접근을 통해 유효성 확인
            session.getId();
            return true;
        } catch (IllegalStateException e) {
            log.debug("Session is invalid: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 세션에서 인증 정보를 가져옵니다.
     */
    public static SecurityContext getSecurityContextFromSession(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        
        Object securityContext = session.getAttribute(
            HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY
        );
        
        if (securityContext instanceof SecurityContext) {
            return (SecurityContext) securityContext;
        }
        
        return null;
    }

    /**
     * 세션 ID를 안전하게 가져옵니다.
     */
    public static String getSessionId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        
        try {
            return session.getId();
        } catch (IllegalStateException e) {
            log.debug("Cannot get session ID: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 새로운 세션을 생성합니다.
     */
    public static String createNewSession(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        
        // 기존 세션이 있다면 무효화
        HttpSession existingSession = request.getSession(false);
        if (existingSession != null) {
            try {
                existingSession.invalidate();
                log.debug("Existing session invalidated");
            } catch (IllegalStateException e) {
                log.debug("Session was already invalidated: {}", e.getMessage());
            }
        }
        
        // 새 세션 생성
        HttpSession newSession = request.getSession(true);
        String sessionId = newSession.getId();
        log.debug("New session created: {}", sessionId);
        
        return sessionId;
    }

    /**
     * 세션에 임시 데이터를 저장합니다.
     */
    public static void setSessionAttribute(HttpServletRequest request, String key, Object value) {
        if (request == null || !StringUtils.hasText(key)) {
            return;
        }
        
        HttpSession session = request.getSession(true);
        session.setAttribute(key, value);
        log.debug("Session attribute set: {} in session {}", key, session.getId());
    }

    /**
     * 세션에서 임시 데이터를 가져옵니다.
     */
    public static Object getSessionAttribute(HttpServletRequest request, String key) {
        if (request == null || !StringUtils.hasText(key)) {
            return null;
        }
        
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        
        return session.getAttribute(key);
    }

    /**
     * 세션에서 임시 데이터를 제거합니다.
     */
    public static void removeSessionAttribute(HttpServletRequest request, String key) {
        if (request == null || !StringUtils.hasText(key)) {
            return;
        }
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(key);
            log.debug("Session attribute removed: {} from session {}", key, session.getId());
        }
    }

    /**
     * 로그용으로 세션 ID를 마스킹합니다.
     */
    public static String maskSessionId(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return "***";
        }
        
        if (sessionId.length() <= 8) {
            return "***";
        }
        
        return sessionId.substring(0, 4) + "***" + sessionId.substring(sessionId.length() - 4);
    }
}