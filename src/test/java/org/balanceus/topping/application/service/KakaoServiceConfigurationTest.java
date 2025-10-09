package org.balanceus.topping.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.balanceus.topping.domain.repository.SggCodeRepository;
import org.balanceus.topping.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

/**
 * Kakao Service 설정 테스트
 * 환경변수 및 구성 요소들이 올바르게 주입되는지 확인
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
	"KAKAO.REST-API-KEY=test-api-key",
	"KAKAO.REDIRECT-URI=http://localhost:8080/api/user/kakao/callback"
})
public class KakaoServiceConfigurationTest {

	@Autowired
	private KakaoService kakaoService;

	@Test
	public void testKakaoServiceInjection() {
		assertNotNull(kakaoService, "KakaoService should be properly injected");
	}

	@Test
	public void testConfigurationValues() {
		// 실제 환경변수 값들이 서비스에 제대로 주입되는지는 별도의 통합 테스트에서 확인
		// 여기서는 서비스 빈이 정상적으로 생성되는지만 확인
		assertNotNull(kakaoService, "KakaoService configuration should work properly");
	}

	/**
	 * KakaoUserInfoDto 검증 테스트
	 */
	@Test
	public void testKakaoUserInfoValidation() {
		// 유효한 사용자 정보 테스트
		org.balanceus.topping.domain.model.KakaoUserInfoDto validUser = 
			new org.balanceus.topping.domain.model.KakaoUserInfoDto(12345L, "testuser", "test@example.com");
		
		assertEquals(true, validUser.isValid(), "Valid user should pass validation");
		assertEquals(true, validUser.hasValidEmail(), "Valid email should pass validation");
		assertEquals("testuser", validUser.getDisplayName(), "Display name should be nickname");
		
		// 이메일만 있는 사용자 테스트 (닉네임 없음)
		org.balanceus.topping.domain.model.KakaoUserInfoDto emailOnlyUser = 
			new org.balanceus.topping.domain.model.KakaoUserInfoDto(12345L, null, "test@example.com");
		
		assertEquals(true, emailOnlyUser.isValid(), "User with email only should be valid");
		assertEquals("test", emailOnlyUser.getDisplayName(), "Display name should be email prefix when no nickname");
		
		org.balanceus.topping.domain.model.KakaoUserInfoDto noEmailUser =
			new org.balanceus.topping.domain.model.KakaoUserInfoDto(67890L, "anotheruser", null);

		assertEquals(true, noEmailUser.isValid(), "User without email consent should still be valid");
		assertEquals(false, noEmailUser.hasValidEmail(), "Missing email should not pass email validation");
		assertEquals("anotheruser", noEmailUser.getDisplayName(), "Display name should fall back to nickname");
		
		// 잘못된 이메일 테스트
		org.balanceus.topping.domain.model.KakaoUserInfoDto invalidEmailUser = 
			new org.balanceus.topping.domain.model.KakaoUserInfoDto(12345L, "testuser", "invalid-email");
		
		assertEquals(false, invalidEmailUser.hasValidEmail(), "Invalid email should fail validation");
	}
}
