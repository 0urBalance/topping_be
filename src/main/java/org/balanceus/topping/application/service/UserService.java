package org.balanceus.topping.application.service;

import java.util.Optional;
import java.util.UUID;

import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
    
    public boolean existsByPhoneNumber(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(UUID uuid) {
        return userRepository.findById(uuid);
    }

    public void deleteById(UUID uuid) {
        userRepository.deleteById(uuid);
    }

    public void updateUserProfile(UUID uuid, String username, String phoneNumber) {
        User user = userRepository.findById(uuid)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (username != null && !username.trim().isEmpty()) {
            user.setUsername(username.trim());
        }
        
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            user.setPhoneNumber(phoneNumber.trim());
        }
        
        userRepository.save(user);
    }

    public void updatePassword(UUID uuid, String currentPassword, String newPassword) {
        User user = userRepository.findById(uuid)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}