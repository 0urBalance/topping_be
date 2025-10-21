package org.balanceus.topping.application.service;

import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.UserRepository;
import org.balanceus.topping.infrastructure.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
    
    @Transactional(readOnly = true)
    public boolean existsByPhoneNumber(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(UUID uuid) {
        return userRepository.findById(uuid);
    }

    public void deleteById(UUID uuid) {
        userRepository.deleteById(uuid);
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + email));
    }

    @Transactional(readOnly = true)
    public User getUserById(UUID uuid) {
        return userRepository.findById(uuid)
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + uuid));
    }

    public User updatePassword(UUID userId, String currentPassword, String newPassword) {
        log.debug("Updating password for user: {}", userId);
        
        User user = getUserById(userId);
        
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    public User updateUserProfile(UUID userId, String username, String phoneNumber) {
        log.debug("Updating profile for user: {}", userId);
        
        User user = getUserById(userId);
        
        if (username != null && !username.trim().isEmpty()) {
            user.setUsername(username.trim());
        }
        
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            user.setPhoneNumber(phoneNumber.trim());
        }
        
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public boolean isValidPassword(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        return passwordEncoder.matches(password, userOpt.get().getPassword());
    }
}