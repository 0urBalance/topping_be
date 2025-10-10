package org.balanceus.topping.presentation.controller;

import org.balanceus.topping.application.service.KakaoService;
import org.balanceus.topping.infrastructure.response.ApiResponseData;
import org.balanceus.topping.application.service.KakaoService.KakaoLoginResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 사용자 관련 컨트롤러
 * Presentation Layer - 외부 요청 처리 및 응답
 */
@Slf4j
@Controller
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

	private final KakaoService kakaoService;

	/**
	 * 카카오 로그인 콜백 처리
	 * 카카오 인증 후 리다이렉트되는 엔드포인트
	 * 
	 * @param code 카카오에서 전달받은 인가 코드
	 * @return 로그인 성공 시 메인 페이지로 리다이렉트, 실패 시 로그인 페이지로 리다이렉트
	 */
	@GetMapping("/kakao/callback")
	public String kakaoCallback(@RequestParam String code, RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest request) {
		try {
			log.info("카카오 로그인 콜백 요청 - code: {}", code);
			
			// 카카오 로그인 처리 (세션 인증 포함)
			KakaoLoginResult loginResult = kakaoService.processKakaoLogin(code, request);
			
			if (loginResult.isSuccess()) {
				if (loginResult.isPlaceholderEmailAssigned() || loginResult.isLinkedExistingAccount()) {
					redirectAttributes.addFlashAttribute("kakaoLoginMessage", loginResult.getMessage());
				}
				if (loginResult.isPlaceholderEmailAssigned()) {
					redirectAttributes.addFlashAttribute("kakaoEmailNotice", true);
				}
				log.info("카카오 로그인 성공 - 메인 페이지로 리다이렉트");
				return "redirect:/"; // 메인 페이지로 리다이렉트
			} else {
				redirectAttributes.addFlashAttribute("kakaoLoginError", loginResult.getMessage());
				log.warn("카카오 로그인 실패 - 로그인 페이지로 리다이렉트");
				return "redirect:/auth/login?error=kakao_login_failed"; // 로그인 페이지로 에러와 함께 리다이렉트
			}
			
		} catch (Exception e) {
			log.error("카카오 로그인 콜백 처리 중 오류 발생", e);
			return "redirect:/auth/login?error=kakao_login_error";
		}
	}

	/**
	 * 현재 사용자 정보 조회 API
	 * 
	 * @param userDetails 인증된 사용자 정보
	 * @return 사용자 기본 정보
	 */
	@GetMapping("/info")
	@ResponseBody
	public ResponseEntity<ApiResponseData<UserInfoDto>> getUserInfo(
			@org.springframework.security.core.annotation.AuthenticationPrincipal 
			org.balanceus.topping.infrastructure.security.UserDetailsImpl userDetails) {
		try {
			if (userDetails == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(ApiResponseData.failure(401, "인증이 필요합니다."));
			}
			
			UserInfoDto userInfo = new UserInfoDto();
			userInfo.setUsername(userDetails.getUser().getUsername());
			userInfo.setEmail(userDetails.getUser().getEmail());
			userInfo.setRole(userDetails.getUser().getRole().name());
			
			return ResponseEntity.ok(ApiResponseData.success(userInfo));
			
		} catch (Exception e) {
			log.error("사용자 정보 조회 중 오류 발생", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseData.failure(500, "사용자 정보 조회 중 오류가 발생했습니다."));
		}
	}

	/**
	 * 카카오 로그인 상태 확인 API (개발/테스트용)
	 * 
	 * @param code 카카오 인가 코드
	 * @return 로그인 처리 결과
	 */
	@GetMapping("/kakao/login-status")
	@ResponseBody
	public ResponseEntity<ApiResponseData<String>> kakaoLoginStatus(@RequestParam String code, jakarta.servlet.http.HttpServletRequest request) {
		try {
			KakaoLoginResult loginResult = kakaoService.processKakaoLogin(code, request);
			
			if (loginResult.isSuccess()) {
				return ResponseEntity.ok(ApiResponseData.success(loginResult.getMessage()));
			} else {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(ApiResponseData.failure(401, loginResult.getMessage()));
			}
			
		} catch (Exception e) {
			log.error("카카오 로그인 상태 확인 중 오류 발생", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseData.failure(500, "카카오 로그인 처리 중 오류가 발생했습니다."));
		}
	}

	/**
	 * 사용자 정보 DTO
	 */
	public static class UserInfoDto {
		private String username;
		private String email;
		private String role;

		public String getUsername() { return username; }
		public void setUsername(String username) { this.username = username; }
		
		public String getEmail() { return email; }
		public void setEmail(String email) { this.email = email; }
		
		public String getRole() { return role; }
		public void setRole(String role) { this.role = role; }
	}
}
