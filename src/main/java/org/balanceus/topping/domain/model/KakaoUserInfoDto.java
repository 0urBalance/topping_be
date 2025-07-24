package org.balanceus.topping.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Kakao API로부터 받은 사용자 정보를 담는 DTO
 * Domain layer에 위치하여 비즈니스 로직에서 사용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoUserInfoDto {

	private Long kakaoId;
	private String nickname;
	private String email;

	/**
	 * Kakao 사용자 정보가 유효한지 검증
	 */
	public boolean isValid() {
		return kakaoId != null && email != null && !email.trim().isEmpty();
	}

	/**
	 * 이메일이 유효한 형식인지 검증
	 */
	public boolean hasValidEmail() {
		if (email == null || email.trim().isEmpty()) {
			return false;
		}
		return email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
	}

	/**
	 * 사용자명으로 사용할 값 반환 (nickname 우선, 없으면 email의 앞부분)
	 */
	public String getDisplayName() {
		if (nickname != null && !nickname.trim().isEmpty()) {
			return nickname;
		}
		if (email != null && email.contains("@")) {
			return email.substring(0, email.indexOf("@"));
		}
		return "사용자";
	}
}