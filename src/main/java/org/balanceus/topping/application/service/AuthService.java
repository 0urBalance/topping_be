package org.balanceus.topping.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.balanceus.topping.domain.model.SggCode;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.SggCodeRepository;
import org.balanceus.topping.domain.repository.UserRepository;
import org.balanceus.topping.infrastructure.exception.UserAlreadyExistsException;
import org.balanceus.topping.infrastructure.exception.ValidationException;
import org.balanceus.topping.presentation.dto.EmailCheckRequest;
import org.balanceus.topping.presentation.dto.EmailCheckResponse;
import org.balanceus.topping.presentation.dto.SignupRequest;
import org.balanceus.topping.presentation.dto.SignupResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final SggCodeRepository sggCodeRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public EmailCheckResponse checkEmailAvailability(EmailCheckRequest request) {
        log.debug("Checking email availability for: {}", request.getEmail());
        
        boolean exists = userRepository.findByEmail(request.getEmail()).isPresent();
        String message = exists ? 
            "이미 등록된 이메일입니다." : 
            "사용 가능한 이메일입니다.";
            
        return new EmailCheckResponse(exists, message);
    }

    public SignupResponse registerUser(SignupRequest request) {
        log.debug("Starting user registration for email: {}", request.getEmail());
        
        validateSignupRequest(request);
        validateEmailNotExists(request.getEmail());
        validateSggCodeExists(request.getSggCode());
        
        User newUser = createUserFromRequest(request);
        User savedUser = userRepository.save(newUser);
        
        log.info("User successfully registered with email: {}", savedUser.getEmail());
        
        return new SignupResponse(
            "회원가입이 완료되었습니다.",
            savedUser.getUuid().toString(),
            savedUser.getUsername(),
            savedUser.getEmail()
        );
    }

    private void validateSignupRequest(SignupRequest request) {
        if (request.getTermsAgreement() == null || !request.getTermsAgreement()) {
            throw new ValidationException("이용약관 및 개인정보처리방침에 동의해주세요.");
        }
        
        if (request.getSggCode() == null) {
            throw new ValidationException("지역을 선택해주세요.");
        }
        
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new ValidationException("이메일을 입력해주세요.");
        }
        
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new ValidationException("비밀번호를 입력해주세요.");
        }
        
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new ValidationException("사용자 이름을 입력해주세요.");
        }
    }

    private void validateEmailNotExists(String email) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new UserAlreadyExistsException("이미 등록된 이메일입니다.");
        }
    }

    private void validateSggCodeExists(Integer sggCode) {
        Optional<SggCode> sggCodeOpt = sggCodeRepository.findById(sggCode);
        if (sggCodeOpt.isEmpty()) {
            throw new ValidationException("유효하지 않은 지역입니다.");
        }
    }

    private User createUserFromRequest(SignupRequest request) {
        SggCode sggCode = sggCodeRepository.findById(request.getSggCode())
            .orElseThrow(() -> new ValidationException("유효하지 않은 지역입니다."));
        
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(request.getRole());
        newUser.setSggCode(sggCode);
        newUser.setTermsAgreement(request.getTermsAgreement());
        
        return newUser;
    }
}