package org.balanceus.topping.application.service;

import lombok.RequiredArgsConstructor;
import org.balanceus.topping.domain.model.PasswordRecovery;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.PasswordRecoveryRepository;
import org.balanceus.topping.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PasswordRecoveryService {
    
    private final PasswordRecoveryRepository passwordRecoveryRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    
    private static final int CODE_LENGTH = 6;
    private static final int EXPIRY_MINUTES = 15;
    
    public void sendVerificationCode(String email) {
        // Check if user exists
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Don't expose whether email exists to prevent user enumeration
            // Just log the attempt and return silently
            return;
        }
        
        // Generate verification code
        String verificationCode = generateVerificationCode();
        
        // Delete any existing recovery requests for this email
        passwordRecoveryRepository.deleteByEmail(email);
        
        // Create new recovery request
        PasswordRecovery recovery = PasswordRecovery.builder()
            .email(email)
            .verificationCode(verificationCode)
            .isUsed(false)
            .expiresAt(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES))
            .build();
        
        passwordRecoveryRepository.save(recovery);
        
        // Send email
        emailService.sendVerificationCode(email, verificationCode);
    }
    
    public boolean verifyCode(String email, String code) {
        Optional<PasswordRecovery> recoveryOpt = passwordRecoveryRepository
            .findByEmailAndVerificationCodeAndIsUsedFalse(email, code);
        
        if (recoveryOpt.isEmpty()) {
            return false;
        }
        
        PasswordRecovery recovery = recoveryOpt.get();
        return recovery.isValid();
    }
    
    public void resetPassword(String email, String code, String newPassword) {
        Optional<PasswordRecovery> recoveryOpt = passwordRecoveryRepository
            .findByEmailAndVerificationCodeAndIsUsedFalse(email, code);
        
        if (recoveryOpt.isEmpty()) {
            throw new IllegalArgumentException("유효하지 않은 인증코드입니다.");
        }
        
        PasswordRecovery recovery = recoveryOpt.get();
        if (!recovery.isValid()) {
            throw new IllegalArgumentException("인증코드가 만료되었거나 이미 사용되었습니다.");
        }
        
        // Find user and update password
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Mark recovery as used
        recovery.setUsed(true);
        passwordRecoveryRepository.save(recovery);
    }
    
    private String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();
        
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        
        return code.toString();
    }
}