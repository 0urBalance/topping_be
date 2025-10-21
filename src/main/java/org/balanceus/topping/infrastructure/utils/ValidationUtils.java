package org.balanceus.topping.infrastructure.utils;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^01[0-9]-?[0-9]{4}-?[0-9]{4}$");
    private static final Pattern KOREAN_NAME_PATTERN = Pattern.compile("^[가-힣]{2,5}$");
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");
    
    private ValidationUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * 문자열이 null이 아니고 비어있지 않은지 확인합니다.
     */
    public static boolean hasText(String str) {
        return StringUtils.hasText(str);
    }

    /**
     * 문자열이 최소 길이와 최대 길이 사이에 있는지 확인합니다.
     */
    public static boolean isLengthBetween(String str, int minLength, int maxLength) {
        if (!hasText(str)) {
            return false;
        }
        int length = str.trim().length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * 휴대폰 번호 형식이 유효한지 확인합니다.
     * 예: 010-1234-5678, 01012345678
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (!hasText(phoneNumber)) {
            return false;
        }
        return PHONE_PATTERN.matcher(phoneNumber.trim()).matches();
    }

    /**
     * 한국 이름 형식이 유효한지 확인합니다.
     * 2-5자 한글만 허용
     */
    public static boolean isValidKoreanName(String name) {
        if (!hasText(name)) {
            return false;
        }
        return KOREAN_NAME_PATTERN.matcher(name.trim()).matches();
    }

    /**
     * 영문자와 숫자로만 구성되어 있는지 확인합니다.
     */
    public static boolean isAlphaNumeric(String str) {
        if (!hasText(str)) {
            return false;
        }
        return ALPHANUMERIC_PATTERN.matcher(str.trim()).matches();
    }

    /**
     * 비밀번호 확인이 일치하는지 확인합니다.
     */
    public static boolean isPasswordConfirmationValid(String password, String confirmPassword) {
        if (!hasText(password) || !hasText(confirmPassword)) {
            return false;
        }
        return password.equals(confirmPassword);
    }

    /**
     * Boolean 값이 true인지 확인합니다 (동의 체크용).
     */
    public static boolean isAgreed(Boolean agreement) {
        return agreement != null && agreement;
    }

    /**
     * URL 형식이 유효한지 간단히 확인합니다.
     */
    public static boolean isValidUrl(String url) {
        if (!hasText(url)) {
            return false;
        }
        return url.trim().toLowerCase().startsWith("http://") || 
               url.trim().toLowerCase().startsWith("https://");
    }

    /**
     * 숫자 범위가 유효한지 확인합니다.
     */
    public static boolean isNumberInRange(Integer number, int min, int max) {
        if (number == null) {
            return false;
        }
        return number >= min && number <= max;
    }

    /**
     * 긍정적인 숫자인지 확인합니다.
     */
    public static boolean isPositiveNumber(Integer number) {
        return number != null && number > 0;
    }

    /**
     * 문자열이 특정 값들 중 하나인지 확인합니다.
     */
    public static boolean isOneOf(String value, String... validValues) {
        if (!hasText(value) || validValues == null || validValues.length == 0) {
            return false;
        }
        
        String trimmedValue = value.trim();
        for (String validValue : validValues) {
            if (trimmedValue.equals(validValue)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 최소 값 이상인지 확인합니다.
     */
    public static boolean isMinLength(String str, int minLength) {
        if (!hasText(str)) {
            return false;
        }
        return str.trim().length() >= minLength;
    }

    /**
     * 최대 값 이하인지 확인합니다.
     */
    public static boolean isMaxLength(String str, int maxLength) {
        if (!hasText(str)) {
            return true; // null/empty는 최대 길이 제한을 통과
        }
        return str.trim().length() <= maxLength;
    }

    /**
     * 안전한 문자열로 정리합니다 (XSS 방지 기본).
     */
    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        
        return input.trim()
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;")
                   .replace("/", "&#x2F;");
    }

    /**
     * 휴대폰 번호를 표준 형식으로 변환합니다.
     * 예: 01012345678 -> 010-1234-5678
     */
    public static String formatPhoneNumber(String phoneNumber) {
        if (!hasText(phoneNumber)) {
            return phoneNumber;
        }
        
        String cleaned = phoneNumber.replaceAll("[^0-9]", "");
        if (cleaned.length() == 11 && cleaned.startsWith("010")) {
            return cleaned.substring(0, 3) + "-" + 
                   cleaned.substring(3, 7) + "-" + 
                   cleaned.substring(7);
        }
        
        return phoneNumber;
    }
}