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

	private static final String KAKAO_PLACEHOLDER_EMAIL_FORMAT = "kakao_%d@kakao-user.topping";

	@Value("${KAKAO.REST-API-KEY}")
	private String kakaoRestApiKey;
	
	@Value("${KAKAO.REDIRECT-URI}")
	private String kakaoRedirectUri;

	/**
	 * 카카오 로그인 처리 및 세션 인증
	 * @param code 카카오 인가 코드
	 * @param request HTTP 요청 객체 (세션 저장용)
	 * @return 로그인 처리 결과
	 */
	public KakaoLoginResult processKakaoLogin(String code, jakarta.servlet.http.HttpServletRequest request) {
		try {
			// 1. 인가 코드로 액세스 토큰 획득
			String accessToken = getAccessToken(code);
			
			// 2. 액세스 토큰으로 사용자 정보 획득
			KakaoUserInfoDto kakaoUserInfo = getKakaoUserInfo(accessToken);
			
			// 3. 사용자 정보 검증
			if (!kakaoUserInfo.isValid()) {
				log.warn("유효하지 않은 카카오 사용자 정보: kakaoId={}, email={}", kakaoUserInfo.getKakaoId(), kakaoUserInfo.getEmail());
				return KakaoLoginResult.failure("카카오 사용자 정보를 확인할 수 없습니다.");
			}
			
			// 4. 사용자 조회 또는 생성
			UserResolution userResolution = findOrCreateUser(kakaoUserInfo);
			User user = userResolution.user();
			
			// 5. Spring Security 세션 인증 처리
			authenticateUser(user, request);
			
			if (userResolution.placeholderEmailAssigned()) {
				log.info("카카오 이메일 미제공 계정 - 대체 이메일 생성: {}", user.getEmail());
				return KakaoLoginResult.success(
					true,
					userResolution.linkedExistingAccount(),
					"카카오 로그인은 완료되었지만 이메일 제공 권한이 없어 임시 이메일이 생성되었습니다. 마이페이지에서 실제 이메일을 등록해주세요.");
			}

			if (userResolution.linkedExistingAccount()) {
				log.info("기존 계정과 카카오 계정 연결 완료: {}", user.getEmail());
				return KakaoLoginResult.success(
					false,
					true,
					"기존 계정과 카카오 계정이 성공적으로 연결되었습니다.");
			}
			
			log.info("카카오 로그인 성공: {}", user.getEmail());
			return KakaoLoginResult.success(false, false, "카카오 로그인에 성공했습니다.");
			
		} catch (Exception e) {
			log.error("카카오 로그인 처리 중 오류 발생", e);
			return KakaoLoginResult.failure("카카오 로그인 처리 중 오류가 발생했습니다.");
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

		log.info("카카오 사용자 정보 획득 성공 - kakaoId: {}, nickname: {}, hasEmail: {}", 
			kakaoId, nickname, email != null);
		log.debug("카카오 사용자 상세 정보 - email: {}", email);
		return new KakaoUserInfoDto(kakaoId, nickname, email);
	}

	/**
	 * 기존 사용자 조회 또는 신규 사용자 생성
	 */
	private UserResolution findOrCreateUser(KakaoUserInfoDto kakaoUserInfo) {
		// 1. Kakao ID로 기존 사용자 조회
		Optional<User> existingByKakaoId = userRepository.findByKakaoId(kakaoUserInfo.getKakaoId());
		if (existingByKakaoId.isPresent()) {
			return new UserResolution(existingByKakaoId.get(), false, false);
		}

		Optional<User> existingUser = kakaoUserInfo.hasEmail()
			? userRepository.findByEmail(kakaoUserInfo.getEmail())
			: Optional.empty();
		
		if (existingUser.isPresent() && existingUser.get().getKakaoId() != null && existingUser.get().getKakaoId().equals(kakaoUserInfo.getKakaoId())) {
			log.debug("기존 사용자 로그인: {}", kakaoUserInfo.getEmail());
			return new UserResolution(existingUser.get(), false, false);
		}

		if (existingUser.isPresent()) {
			User user = existingUser.get();
			log.debug("기존 이메일 사용자와 카카오 계정을 연결합니다: {}", kakaoUserInfo.getEmail());
			user.setKakaoId(kakaoUserInfo.getKakaoId());
			User savedUser = userRepository.save(user);
			return new UserResolution(savedUser, false, true);
		}

		// 신규 사용자 생성
		User newUser = createNewUserFromKakao(kakaoUserInfo);
		User savedUser = userRepository.save(newUser);
		
		boolean placeholderEmail = !kakaoUserInfo.hasValidEmail();
		log.info("카카오 신규 사용자 생성 완료 - email: {}, placeholderEmail: {}", 
			savedUser.getEmail(), placeholderEmail);
		return new UserResolution(savedUser, placeholderEmail, false);
	}

	/**
	 * 카카오 정보로 신규 사용자 생성
	 */
	public User createNewUserFromKakao(KakaoUserInfoDto kakaoUserInfo) {
		User user = new User();
		if (kakaoUserInfo.hasValidEmail()) {
			user.setEmail(kakaoUserInfo.getEmail());
		} else {
			user.setEmail(generatePlaceholderEmail(kakaoUserInfo.getKakaoId()));
		}
		user.setUsername(kakaoUserInfo.getDisplayName());
		user.setKakaoId(kakaoUserInfo.getKakaoId());
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
	private void authenticateUser(User user, jakarta.servlet.http.HttpServletRequest request) {
		UserDetailsImpl userDetails = new UserDetailsImpl(user);
		UsernamePasswordAuthenticationToken authentication = 
			new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
		
		// SecurityContext에 인증 정보 설정
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		// 세션에 SecurityContext 저장 (SessionAuthController와 동일한 방식)
		request.getSession().setAttribute(
			org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
			SecurityContextHolder.getContext()
		);
		
		log.info("카카오 로그인 세션 인증 완료 - sessionId: {}, email: {}, kakaoId: {}", 
			request.getSession().getId(), user.getEmail(), user.getKakaoId());
	}

	private String generatePlaceholderEmail(Long kakaoId) {
		return String.format(KAKAO_PLACEHOLDER_EMAIL_FORMAT, kakaoId);
	}

	private record UserResolution(User user, boolean placeholderEmailAssigned, boolean linkedExistingAccount) {}

	public static class KakaoLoginResult {
		private final boolean success;
		private final boolean placeholderEmailAssigned;
		private final boolean linkedExistingAccount;
		private final String message;

		private KakaoLoginResult(boolean success, boolean placeholderEmailAssigned, boolean linkedExistingAccount, String message) {
			this.success = success;
			this.placeholderEmailAssigned = placeholderEmailAssigned;
			this.linkedExistingAccount = linkedExistingAccount;
			this.message = message;
		}

		public static KakaoLoginResult success(boolean placeholderEmailAssigned, boolean linkedExistingAccount, String message) {
			return new KakaoLoginResult(true, placeholderEmailAssigned, linkedExistingAccount, message);
		}

		public static KakaoLoginResult failure(String message) {
			return new KakaoLoginResult(false, false, false, message);
		}

		public boolean isSuccess() {
			return success;
		}

		public boolean isPlaceholderEmailAssigned() {
			return placeholderEmailAssigned;
		}

		public boolean isLinkedExistingAccount() {
			return linkedExistingAccount;
		}

		public String getMessage() {
			return message;
		}
	}
}
