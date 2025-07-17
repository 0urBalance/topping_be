package org.balanceus.topping.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.balanceus.topping.domain.repository.UserRepository;
import org.balanceus.topping.presentation.dto.EmailCheckRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class EmailCheckController {
    
    private final UserRepository userRepository;
    
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
            boolean exists = userRepository.findByEmail(email).isPresent();
            
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
    
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }
}