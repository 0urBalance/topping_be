package org.balanceus.topping.presentation.controller;

import org.balanceus.topping.application.service.KakaoService;
import org.balanceus.topping.infrastructure.response.ApiResponseData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
	public String kakaoCallback(@RequestParam String code) {
		try {
			log.info("카카오 로그인 콜백 요청 - code: {}", code);
			
			// 카카오 로그인 처리 (세션 인증 포함)
			boolean loginSuccess = kakaoService.processKakaoLogin(code);
			
			if (loginSuccess) {
				log.info("카카오 로그인 성공 - 메인 페이지로 리다이렉트");
				return "redirect:/"; // 메인 페이지로 리다이렉트
			} else {
				log.warn("카카오 로그인 실패 - 로그인 페이지로 리다이렉트");
				return "redirect:/auth/login?error=kakao_login_failed"; // 로그인 페이지로 에러와 함께 리다이렉트
			}
			
		} catch (Exception e) {
			log.error("카카오 로그인 콜백 처리 중 오류 발생", e);
			return "redirect:/auth/login?error=kakao_login_error";
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
	public ResponseEntity<ApiResponseData<String>> kakaoLoginStatus(@RequestParam String code) {
		try {
			boolean loginSuccess = kakaoService.processKakaoLogin(code);
			
			if (loginSuccess) {
				return ResponseEntity.ok(ApiResponseData.success("카카오 로그인 성공"));
			} else {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(ApiResponseData.failure(401, "카카오 로그인 실패"));
			}
			
		} catch (Exception e) {
			log.error("카카오 로그인 상태 확인 중 오류 발생", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponseData.failure(500, "카카오 로그인 처리 중 오류가 발생했습니다."));
		}
	}
}