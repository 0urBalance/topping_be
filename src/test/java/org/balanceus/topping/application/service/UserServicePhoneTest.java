package org.balanceus.topping.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServicePhoneTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setPhoneNumber("010-1234-5678");
        testUser.setEmail("test@example.com");
    }

    @Test
    void testExistsByPhoneNumber_WhenPhoneExists_ShouldReturnTrue() {
        // Given
        String phoneNumber = "010-1234-5678";
        when(userRepository.existsByPhoneNumber(phoneNumber)).thenReturn(true);

        // When
        boolean exists = userService.existsByPhoneNumber(phoneNumber);

        // Then
        assertTrue(exists);
    }

    @Test
    void testExistsByPhoneNumber_WhenPhoneDoesNotExist_ShouldReturnFalse() {
        // Given
        String phoneNumber = "010-9999-9999";
        when(userRepository.existsByPhoneNumber(phoneNumber)).thenReturn(false);

        // When
        boolean exists = userService.existsByPhoneNumber(phoneNumber);

        // Then
        assertFalse(exists);
    }

    @Test
    void testExistsByPhoneNumber_WithNullPhoneNumber_ShouldReturnFalse() {
        // Given
        String phoneNumber = null;
        when(userRepository.existsByPhoneNumber(phoneNumber)).thenReturn(false);

        // When
        boolean exists = userService.existsByPhoneNumber(phoneNumber);

        // Then
        assertFalse(exists);
    }
}