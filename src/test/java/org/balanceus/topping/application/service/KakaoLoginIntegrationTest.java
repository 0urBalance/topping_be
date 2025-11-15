package org.balanceus.topping.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.balanceus.topping.domain.model.KakaoUserInfoDto;
import org.balanceus.topping.domain.model.Role;
import org.balanceus.topping.domain.model.SggCode;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.SggCodeRepository;
import org.balanceus.topping.domain.repository.UserRepository;
import org.balanceus.topping.application.service.KakaoService.KakaoLoginResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Collections;

/**
 * 카카오 로그인 통합 테스트
 * 실제 카카오 API 호출 없이 전체 로그인 플로우를 테스트
 */
@ExtendWith(MockitoExtension.class)
class KakaoLoginIntegrationTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SggCodeRepository sggCodeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpSession httpSession;

    @InjectMocks
    private KakaoService kakaoService;

    private SggCode defaultSggCode;

    @BeforeEach
    void setUp() {
        // 테스트용 환경 변수 설정
        ReflectionTestUtils.setField(kakaoService, "kakaoRestApiKey", "test-api-key");
        ReflectionTestUtils.setField(kakaoService, "kakaoRedirectUri", "http://localhost:8080/api/user/kakao/callback");
        
        // 기본 지역 설정
        defaultSggCode = new SggCode();
        defaultSggCode.setSggCd5(11680);
        defaultSggCode.setSggCdNm("강남구");

        when(httpServletRequest.getSession()).thenReturn(httpSession);
        when(httpSession.getId()).thenReturn("test-session-id");
    }

    @Test
    void testSuccessfulKakaoLoginWithValidEmail() {
        // Given
        String code = "test-auth-code";
        String accessToken = "test-access-token";
        Long kakaoId = 12345L;
        String email = "test@example.com";
        String nickname = "테스트사용자";

        // Mock 카카오 API 응답
        mockKakaoTokenResponse(accessToken);
        mockKakaoUserInfoResponse(kakaoId, email, nickname);

        // Mock 데이터베이스 응답
        when(userRepository.findByKakaoId(kakaoId)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(sggCodeRepository.findById(11680)).thenReturn(Optional.of(defaultSggCode));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        
        User savedUser = createTestUser(kakaoId, email, nickname);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        KakaoLoginResult result = kakaoService.processKakaoLogin(code, httpServletRequest);

        // Then
        assertTrue(result.isSuccess(), "카카오 로그인이 성공해야 합니다.");
        assertFalse(result.isPlaceholderEmailAssigned(), "유효한 이메일이므로 임시 이메일이 할당되지 않아야 합니다.");
        assertFalse(result.isLinkedExistingAccount(), "신규 사용자이므로 기존 계정 연결이 아니어야 합니다.");
        assertEquals("카카오 로그인에 성공했습니다.", result.getMessage());

        // 사용자 저장 검증
        verify(userRepository).save(argThat(user -> 
            user.getKakaoId().equals(kakaoId) &&
            user.getEmail().equals(email) &&
            user.getUsername().equals(nickname) &&
            user.getRole().equals(Role.ROLE_USER) &&
            user.getTermsAgreement().equals(true)
        ));
    }

    @Test
    void testSuccessfulKakaoLoginWithoutEmail() {
        // Given
        String code = "test-auth-code";
        String accessToken = "test-access-token";
        Long kakaoId = 67890L;
        String nickname = "이메일미제공사용자";

        // Mock 카카오 API 응답 (이메일 없음)
        mockKakaoTokenResponse(accessToken);
        mockKakaoUserInfoResponse(kakaoId, null, nickname);

        // Mock 데이터베이스 응답
        when(userRepository.findByKakaoId(kakaoId)).thenReturn(Optional.empty());
        when(sggCodeRepository.findById(11680)).thenReturn(Optional.of(defaultSggCode));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        
        String placeholderEmail = String.format("kakao_%d@kakao-user.topping", kakaoId);
        User savedUser = createTestUser(kakaoId, placeholderEmail, nickname);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        KakaoLoginResult result = kakaoService.processKakaoLogin(code, httpServletRequest);

        // Then
        assertTrue(result.isSuccess(), "카카오 로그인이 성공해야 합니다.");
        assertTrue(result.isPlaceholderEmailAssigned(), "이메일이 없으므로 임시 이메일이 할당되어야 합니다.");
        assertFalse(result.isLinkedExistingAccount(), "신규 사용자이므로 기존 계정 연결이 아니어야 합니다.");
        assertTrue(result.getMessage().contains("임시 이메일이 생성되었습니다"));

        // 사용자 저장 검증 (임시 이메일 사용)
        verify(userRepository).save(argThat(user -> 
            user.getKakaoId().equals(kakaoId) &&
            user.getEmail().equals(placeholderEmail) &&
            user.getUsername().equals(nickname)
        ));
    }

    @Test
    void testLinkingExistingAccountWithKakaoId() {
        // Given
        String code = "test-auth-code";
        String accessToken = "test-access-token";
        Long kakaoId = 99999L;
        String email = "existing@example.com";
        String nickname = "기존사용자";

        // Mock 카카오 API 응답
        mockKakaoTokenResponse(accessToken);
        mockKakaoUserInfoResponse(kakaoId, email, nickname);

        // 기존 사용자 (카카오 ID 없음)
        User existingUser = createTestUser(null, email, "기존사용자이름");
        when(userRepository.findByKakaoId(kakaoId)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // When
        KakaoLoginResult result = kakaoService.processKakaoLogin(code, httpServletRequest);

        // Then
        assertTrue(result.isSuccess(), "카카오 로그인이 성공해야 합니다.");
        assertFalse(result.isPlaceholderEmailAssigned(), "유효한 이메일이므로 임시 이메일이 할당되지 않아야 합니다.");
        assertTrue(result.isLinkedExistingAccount(), "기존 계정과 카카오 계정이 연결되어야 합니다.");
        assertTrue(result.getMessage().contains("기존 계정과 카카오 계정이 성공적으로 연결되었습니다"));

        // 카카오 ID 설정 검증
        verify(userRepository).save(argThat(user -> 
            user.getKakaoId().equals(kakaoId) &&
            user.getEmail().equals(email)
        ));
    }

    @Test
    void testExistingKakaoUserLogin() {
        // Given
        String code = "test-auth-code";
        String accessToken = "test-access-token";
        Long kakaoId = 11111L;
        String email = "existing.kakao@example.com";
        String nickname = "기존카카오사용자";

        // Mock 카카오 API 응답
        mockKakaoTokenResponse(accessToken);
        mockKakaoUserInfoResponse(kakaoId, email, nickname);

        // 기존 카카오 사용자
        User existingKakaoUser = createTestUser(kakaoId, email, nickname);
        when(userRepository.findByKakaoId(kakaoId)).thenReturn(Optional.of(existingKakaoUser));

        // When
        KakaoLoginResult result = kakaoService.processKakaoLogin(code, httpServletRequest);

        // Then
        assertTrue(result.isSuccess(), "카카오 로그인이 성공해야 합니다.");
        assertFalse(result.isPlaceholderEmailAssigned(), "기존 사용자이므로 임시 이메일 할당이 없어야 합니다.");
        assertFalse(result.isLinkedExistingAccount(), "기존 카카오 사용자이므로 새로운 연결이 아니어야 합니다.");
        assertEquals("카카오 로그인에 성공했습니다.", result.getMessage());

        // 새로운 사용자 저장이 일어나지 않아야 함
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testKakaoUserInfoDtoValidation() {
        // 유효한 카카오 사용자 정보
        KakaoUserInfoDto validUser = new KakaoUserInfoDto(12345L, "testuser", "test@example.com");
        assertTrue(validUser.isValid());
        assertTrue(validUser.hasValidEmail());
        assertEquals("testuser", validUser.getDisplayName());

        // 이메일 없는 사용자
        KakaoUserInfoDto noEmailUser = new KakaoUserInfoDto(67890L, "anotheruser", null);
        assertTrue(noEmailUser.isValid());
        assertFalse(noEmailUser.hasValidEmail());
        assertEquals("anotheruser", noEmailUser.getDisplayName());

        // 잘못된 이메일 형식
        KakaoUserInfoDto invalidEmailUser = new KakaoUserInfoDto(12345L, "testuser", "invalid-email");
        assertTrue(invalidEmailUser.isValid());
        assertFalse(invalidEmailUser.hasValidEmail());
    }

    @Test
    void testNewUserFromKakaoCreation() {
        // Given
        KakaoUserInfoDto kakaoUserInfo = new KakaoUserInfoDto(12345L, "testuser", "test@example.com");
        when(sggCodeRepository.findById(11680)).thenReturn(Optional.of(defaultSggCode));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");

        // When
        User newUser = kakaoService.createNewUserFromKakao(kakaoUserInfo);

        // Then
        assertNotNull(newUser);
        assertEquals(kakaoUserInfo.getKakaoId(), newUser.getKakaoId());
        assertEquals(kakaoUserInfo.getEmail(), newUser.getEmail());
        assertEquals(kakaoUserInfo.getDisplayName(), newUser.getUsername());
        assertEquals(Role.ROLE_USER, newUser.getRole());
        assertTrue(newUser.getTermsAgreement());
        assertEquals(defaultSggCode, newUser.getSggCode());
    }

    @Test
    void testNewUserFromKakaoWithFallbackSggCode() {
        // Given
        KakaoUserInfoDto kakaoUserInfo = new KakaoUserInfoDto(12345L, "testuser", "test@example.com");
        SggCode fallbackSggCode = new SggCode();
        fallbackSggCode.setSggCd5(11110);
        fallbackSggCode.setSggCdNm("종로구");

        when(sggCodeRepository.findById(11680)).thenReturn(Optional.empty());
        when(sggCodeRepository.findAll(any(PageRequest.class)))
            .thenReturn(Collections.singletonList(fallbackSggCode));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");

        // When
        User newUser = kakaoService.createNewUserFromKakao(kakaoUserInfo);

        // Then
        assertNotNull(newUser);
        assertEquals(fallbackSggCode, newUser.getSggCode());
    }

    private void mockKakaoTokenResponse(String accessToken) {
        String tokenResponse = "{\"access_token\":\"" + accessToken + "\",\"token_type\":\"bearer\"}";
        when(restTemplate.exchange(any(), eq(String.class)))
            .thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));
    }

    private void mockKakaoUserInfoResponse(Long kakaoId, String email, String nickname) {
        StringBuilder userInfoJson = new StringBuilder();
        userInfoJson.append("{\"id\":").append(kakaoId);
        
        if (nickname != null) {
            userInfoJson.append(",\"properties\":{\"nickname\":\"").append(nickname).append("\"}");
        }
        
        if (email != null) {
            userInfoJson.append(",\"kakao_account\":{\"email\":\"").append(email).append("\"}");
        }
        
        userInfoJson.append("}");

        when(restTemplate.exchange(any(), eq(String.class)))
            .thenReturn(new ResponseEntity<>(userInfoJson.toString(), HttpStatus.OK))
            .thenReturn(new ResponseEntity<>(userInfoJson.toString(), HttpStatus.OK));
    }

    private User createTestUser(Long kakaoId, String email, String username) {
        User user = new User();
        user.setKakaoId(kakaoId);
        user.setEmail(email);
        user.setUsername(username);
        user.setRole(Role.ROLE_USER);
        user.setTermsAgreement(true);
        user.setSggCode(defaultSggCode);
        return user;
    }
}