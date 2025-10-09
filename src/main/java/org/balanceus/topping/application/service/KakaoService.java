package org.balanceus.topping.application.service;

import java.net.URI;
import java.util.Optional;

import org.balanceus.topping.domain.model.KakaoUserInfoDto;
import org.balanceus.topping.domain.model.SggCode;
import org.balanceus.topping.domain.model.User;
import org.balanceus.topping.domain.repository.SggCodeRepository;
import org.balanceus.topping.domain.repository.UserRepository;
import org.balanceus.topping.domain.model.Role;
import org.balanceus.topping.infrastructure.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Kakao 소셜 로그인 서비스
 * 비즈니스 로직을 담당하는 Application Layer
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class KakaoService {

	private final RestTemplate restTemplate;
	private final UserRepository userRepository;
	private final SggCodeRepository sggCodeRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${KAKAO.REST-API-KEY}")
	private String kakaoRestApiKey;
	
	@Value("${KAKAO.REDIRECT-URI}")
	private String kakaoRedirectUri;

	/**
	 * 카카오 로그인 처리 및 세션 인증
	 * @param code 카카오 인가 코드
	 * @return 로그인 성공 여부
	 */
	public boolean processKakaoLogin(String code) {
		try {
			// 1. 인가 코드로 액세스 토큰 획득
			String accessToken = getAccessToken(code);
			
			// 2. 액세스 토큰으로 사용자 정보 획득
			KakaoUserInfoDto kakaoUserInfo = getKakaoUserInfo(accessToken);
			
			// 3. 사용자 정보 검증
			if (!kakaoUserInfo.isValid() || !kakaoUserInfo.hasValidEmail()) {
				log.warn("유효하지 않은 카카오 사용자 정보: {}", kakaoUserInfo.getEmail());
				return false;
			}
			
			// 4. 사용자 조회 또는 생성
			User user = findOrCreateUser(kakaoUserInfo);
			
			// 5. Spring Security 세션 인증 처리
			authenticateUser(user);
			
			log.info("카카오 로그인 성공: {}", user.getEmail());
			return true;
			
		} catch (Exception e) {
			log.error("카카오 로그인 처리 중 오류 발생", e);
			return false;
		}
	}

	/**
	 * 인가 코드로 액세스 토큰 획득
	 */
	private String getAccessToken(String code) throws JsonProcessingException {
		URI uri = UriComponentsBuilder
			.fromUriString("https://kauth.kakao.com")
			.path("/oauth/token")
			.encode()
			.build()
			.toUri();

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("grant_type", "authorization_code");
		body.add("client_id", kakaoRestApiKey);
		body.add("redirect_uri", kakaoRedirectUri);
		body.add("code", code);

		RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
			.post(uri)
			.headers(headers)
			.body(body);

		ResponseEntity<String> response = restTemplate.exchange(requestEntity, String.class);
		
		JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
		String accessToken = jsonNode.get("access_token").asText();
		
		log.debug("카카오 액세스 토큰 획득 성공");
		return accessToken;
	}

	/**
	 * 액세스 토큰으로 카카오 사용자 정보 획득
	 */
	private KakaoUserInfoDto getKakaoUserInfo(String accessToken) throws JsonProcessingException {
		URI uri = UriComponentsBuilder
			.fromUriString("https://kapi.kakao.com")
			.path("/v2/user/me")
			.encode()
			.build()
			.toUri();

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + accessToken);
		headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

		RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
			.post(uri)
			.headers(headers)
			.body(new LinkedMultiValueMap<>());

		ResponseEntity<String> response = restTemplate.exchange(requestEntity, String.class);
		
		JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
		Long kakaoId = jsonNode.get("id").asLong();
		
		String nickname = null;
		if (jsonNode.has("properties") && jsonNode.get("properties").has("nickname")) {
			nickname = jsonNode.get("properties").get("nickname").asText();
		}
		
		String email = null;
		if (jsonNode.has("kakao_account") && jsonNode.get("kakao_account").has("email")) {
			email = jsonNode.get("kakao_account").get("email").asText();
		}

		log.info("카카오 사용자 정보 획득: ID={}, nickname={}, email={}", kakaoId, nickname, email);
		return new KakaoUserInfoDto(kakaoId, nickname, email);
	}

	/**
	 * 기존 사용자 조회 또는 신규 사용자 생성
	 */
	private User findOrCreateUser(KakaoUserInfoDto kakaoUserInfo) {
		Optional<User> existingUser = userRepository.findByEmail(kakaoUserInfo.getEmail());
		
		if (existingUser.isPresent()) {
			log.debug("기존 사용자 로그인: {}", kakaoUserInfo.getEmail());
			return existingUser.get();
		}

		// 신규 사용자 생성
		User newUser = createNewUserFromKakao(kakaoUserInfo);
		User savedUser = userRepository.save(newUser);
		
		log.info("카카오 신규 사용자 생성: {}", savedUser.getEmail());
		return savedUser;
	}

	/**
	 * 카카오 정보로 신규 사용자 생성
	 */
	@Transactional(readOnly = true)
	public User createNewUserFromKakao(KakaoUserInfoDto kakaoUserInfo) {
		User user = new User();
		user.setEmail(kakaoUserInfo.getEmail());
		user.setUsername(kakaoUserInfo.getDisplayName());
		user.setPassword(passwordEncoder.encode("KAKAO_USER_" + kakaoUserInfo.getKakaoId())); // 임시 패스워드
		user.setRole(Role.ROLE_USER); // 기본 역할
		user.setTermsAgreement(true); // 카카오 로그인 시 약관 동의로 간주
		
		// 기본 지역 설정 (서울특별시 강남구로 설정, 실제로는 사용자가 나중에 변경 가능)
		Optional<SggCode> defaultSggCode = sggCodeRepository.findById(11680);
		if (defaultSggCode.isPresent()) {
			user.setSggCode(defaultSggCode.get());
		} else {
			// 기본 지역이 없으면 첫 번째 지역으로 설정 - 페이징으로 제한
			sggCodeRepository.findAll(PageRequest.of(0, 1))
				.stream().findFirst().ifPresent(user::setSggCode);
		}
		
		return user;
	}

	/**
	 * Spring Security 세션 인증 처리
	 */
	private void authenticateUser(User user) {
		UserDetailsImpl userDetails = new UserDetailsImpl(user);
		UsernamePasswordAuthenticationToken authentication = 
			new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
		
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		log.debug("사용자 세션 인증 완료: {}", user.getEmail());
	}
}
