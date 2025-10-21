package org.balanceus.topping.infrastructure.utils;

import lombok.extern.slf4j.Slf4j;
import org.balanceus.topping.infrastructure.security.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
public class AuthUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
    );
    
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    private AuthUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * 현재 인증된 사용자 정보를 가져옵니다.
     */
    public static UserDetailsImpl getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getName().equals("anonymousUser")) {
            return null;
        }
        
        if (authentication.getPrincipal() instanceof UserDetailsImpl) {
            return (UserDetailsImpl) authentication.getPrincipal();
        }
        
        return null;
    }

    /**
     * 현재 사용자의 UUID를 가져옵니다.
     */
    public static UUID getCurrentUserUuid() {
        UserDetailsImpl userDetails = getCurrentUser();
        return userDetails != null ? userDetails.getUser().getUuid() : null;
    }

    /**
     * 현재 사용자의 이메일을 가져옵니다.
     */
    public static String getCurrentUserEmail() {
        UserDetailsImpl userDetails = getCurrentUser();
        return userDetails != null ? userDetails.getUser().getEmail() : null;
    }

    /**
     * 현재 사용자가 인증되었는지 확인합니다.
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && 
               !authentication.getName().equals("anonymousUser");
    }

    /**
     * 이메일 형식이 유효한지 검증합니다.
     */
    public static boolean isValidEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * 비밀번호 형식이 유효한지 검증합니다.
     * - 최소 8자 이상
     * - 대문자, 소문자, 숫자, 특수문자 각각 최소 1개 포함
     */
    public static boolean isValidPassword(String password) {
        if (!StringUtils.hasText(password)) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * 사용자명이 유효한지 검증합니다.
     * - 2자 이상 50자 이하
     * - 공백으로만 구성되지 않음
     */
    public static boolean isValidUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return false;
        }
        String trimmed = username.trim();
        return trimmed.length() >= 2 && trimmed.length() <= 50;
    }

    /**
     * 입력 문자열을 안전하게 정리합니다.
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        return input.trim();
    }

    /**
     * 로그용으로 이메일을 마스킹합니다.
     */
    public static String maskEmail(String email) {
        if (!StringUtils.hasText(email) || !email.contains("@")) {
            return "***";
        }
        
        String[] parts = email.split("@");
        if (parts.length != 2) {
            return "***";
        }
        
        String localPart = parts[0];
        String domainPart = parts[1];
        
        if (localPart.length() <= 2) {
            return "***@" + domainPart;
        }
        
        return localPart.charAt(0) + "***" + localPart.charAt(localPart.length() - 1) + "@" + domainPart;
    }
}