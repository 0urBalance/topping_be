package org.balanceus.topping.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.balanceus.topping.domain.model.SggCode;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.SggCodeRepository;
import org.balanceus.topping.domain.repository.UserRepository;
import org.balanceus.topping.infrastructure.security.Role;
import org.balanceus.topping.presentation.dto.SignupRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SggCodeRepository sggCodeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Create test SggCode data
        SggCode testSggCode = new SggCode();
        testSggCode.setSggCd5(11010);
        testSggCode.setSggCdNm("서울특별시 종로구");
        testSggCode.setSggCdNmRegion("서울특별시");
        testSggCode.setSggCdNmCity("종로구");
        sggCodeRepository.save(testSggCode);
    }

    @Test
    void signup_WithValidDataAndTermsAgreement_ShouldSucceed() throws Exception {
        SignupRequest request = new SignupRequest(
                "testuser",
                "test@example.com",
                "password123",
                Role.ROLE_USER,
                11010,
                true
        );

        mockMvc.perform(post("/api/member/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."));

        // Verify user was saved with terms agreement
        Optional<User> savedUser = userRepository.findByEmail("test@example.com");
        assertTrue(savedUser.isPresent());
        assertEquals("testuser", savedUser.get().getUsername());
        assertTrue(savedUser.get().getTermsAgreement());
    }

    @Test
    void signup_WithoutTermsAgreement_ShouldFail() throws Exception {
        SignupRequest request = new SignupRequest(
                "testuser",
                "test@example.com",
                "password123",
                Role.ROLE_USER,
                11010,
                false
        );

        mockMvc.perform(post("/api/member/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("이용약관 및 개인정보처리방침에 동의해주세요."));

        // Verify user was not saved
        Optional<User> savedUser = userRepository.findByEmail("test@example.com");
        assertTrue(savedUser.isEmpty());
    }

    @Test
    void signup_WithNullTermsAgreement_ShouldFail() throws Exception {
        SignupRequest request = new SignupRequest(
                "testuser",
                "test@example.com",
                "password123",
                Role.ROLE_USER,
                11010,
                null
        );

        mockMvc.perform(post("/api/member/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("이용약관 및 개인정보처리방침에 동의해주세요."));

        // Verify user was not saved
        Optional<User> savedUser = userRepository.findByEmail("test@example.com");
        assertTrue(savedUser.isEmpty());
    }

    @Test
    void signup_WithDuplicateEmail_ShouldFail() throws Exception {
        // Create existing user
        User existingUser = new User();
        existingUser.setUsername("existinguser");
        existingUser.setEmail("test@example.com");
        existingUser.setPassword("hashedpassword");
        existingUser.setRole(Role.ROLE_USER);
        existingUser.setTermsAgreement(true);
        userRepository.save(existingUser);

        SignupRequest request = new SignupRequest(
                "testuser",
                "test@example.com",
                "password123",
                Role.ROLE_USER,
                11010,
                true
        );

        mockMvc.perform(post("/api/member/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("이미 등록된 이메일입니다."));
    }

    @Test
    void signup_WithBusinessOwnerRoleAndTermsAgreement_ShouldSucceed() throws Exception {
        SignupRequest request = new SignupRequest(
                "businessowner",
                "business@example.com",
                "password123",
                Role.ROLE_BUSINESS_OWNER,
                11010,
                true
        );

        mockMvc.perform(post("/api/member/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."));

        // Verify user was saved with correct role and terms agreement
        Optional<User> savedUser = userRepository.findByEmail("business@example.com");
        assertTrue(savedUser.isPresent());
        assertEquals("businessowner", savedUser.get().getUsername());
        assertEquals(Role.ROLE_BUSINESS_OWNER, savedUser.get().getRole());
        assertTrue(savedUser.get().getTermsAgreement());
    }

    @Test
    @WithMockUser
    void loginStatus_WithAuthenticatedUser_ShouldReturnSuccess() throws Exception {
        mockMvc.perform(post("/api/member/login-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void loginStatus_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // Spring Security redirects unauthenticated requests to login page by default
        mockMvc.perform(post("/api/member/login-status"))
                .andExpect(status().isFound()) // 302 redirect
                .andExpect(redirectedUrl("http://localhost/auth/login"));
    }
}