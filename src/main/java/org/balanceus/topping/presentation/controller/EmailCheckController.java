package org.balanceus.topping.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.balanceus.topping.application.service.UserService;
import org.balanceus.topping.presentation.dto.EmailCheckRequest;
import org.balanceus.topping.presentation.dto.PhoneCheckRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class EmailCheckController {
    
    private final UserService userService;
    
    @PostMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestBody EmailCheckRequest request) {
        String email = request.getEmail();
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        
        if (email == null || email.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "이메일 주소를 입력해주세요.");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Sanitize email input
        email = email.trim().toLowerCase();
        
        // Validate email format
        if (!isValidEmail(email)) {
            response.put("success", false);
            response.put("message", "올바른 이메일 형식이 아닙니다.");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            boolean exists = userService.existsByEmail(email);
            
            data.put("exists", exists);
            data.put("email", email);
            
            response.put("success", true);
            response.put("data", data);
            
            if (exists) {
                response.put("message", "이미 사용중인 이메일입니다.");
            } else {
                response.put("message", "사용 가능한 이메일입니다.");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "이메일 확인 중 오류가 발생했습니다. 다시 시도해주세요.");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/check-phone")
    public ResponseEntity<Map<String, Object>> checkPhone(@RequestBody PhoneCheckRequest request) {
        String phoneNumber = request.getPhoneNumber();
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "전화번호를 입력해주세요.");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Sanitize phone input - remove all non-digits and re-format
        phoneNumber = phoneNumber.replaceAll("[^0-9]", "");
        phoneNumber = formatPhoneNumber(phoneNumber);
        
        // Validate phone format
        if (!isValidPhone(phoneNumber)) {
            response.put("success", false);
            response.put("message", "올바른 전화번호 형식이 아닙니다.");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            boolean exists = userService.existsByPhoneNumber(phoneNumber);
            
            data.put("exists", exists);
            data.put("phoneNumber", phoneNumber);
            
            response.put("success", true);
            response.put("data", data);
            
            if (exists) {
                response.put("message", "이미 사용중인 전화번호입니다.");
            } else {
                response.put("message", "사용 가능한 전화번호입니다.");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "전화번호 확인 중 오류가 발생했습니다. 다시 시도해주세요.");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }
    
    private String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber.length() >= 10 && phoneNumber.length() <= 11) {
            if (phoneNumber.length() == 10) {
                return phoneNumber.replaceFirst("(\\d{3})(\\d{3})(\\d{4})", "$1-$2-$3");
            } else {
                return phoneNumber.replaceFirst("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
            }
        }
        return phoneNumber;
    }
    
    private boolean isValidPhone(String phoneNumber) {
        // Korean phone number format: 010-1234-5678 (11 digits) or 010-123-4567 (10 digits)
        String phoneRegex = "^01[0-9]-\\d{3,4}-\\d{4}$";
        return phoneNumber.matches(phoneRegex);
    }
}