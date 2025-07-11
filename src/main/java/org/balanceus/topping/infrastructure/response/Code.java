package org.balanceus.topping.infrastructure.response;

import java.util.Optional;
import java.util.function.Predicate;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Code {

	/**
	 * 성공 0번대
	 */
	SUCCESS(HttpStatus.OK, 200, "성공적으로 처리되었습니다."),
	CREATED(HttpStatus.CREATED, 201, "성공적으로 생성되었습니다."),
	ALREADY_EXISTS(HttpStatus.OK, 202, "이미 존재하는 리소스입니다."),

	/**
	 * 500번대
	 */
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 500, "예기치 못한 서버 오류가 발생했습니다."),
	INTERNAL_SERVER_MINIO_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 500, "Minio 서버 오류가 발생했습니다."),


	SIGN001(HttpStatus.BAD_REQUEST,2001,"일치하는 이메일 없음"),
	SIGN002(HttpStatus.BAD_REQUEST,2002,"비밀번호 불일치"),
	SIGN003(HttpStatus.BAD_REQUEST,2003,"이미 등록된 이메일"),
	SIGN004(HttpStatus.BAD_REQUEST,2004,"이미 등록된 사용자 이름"),
	SIGN005(HttpStatus.BAD_REQUEST,2005,"유효하지 않은 회원가입 정보"),
	SIGN006(HttpStatus.BAD_REQUEST,2006,"유효하지 않은 관리자 코드"),

	/**
	 * 400번대
	 */
	NOT_FOUND(HttpStatus.NOT_FOUND, 404, "요청한 리소스를 찾을 수 없습니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, 403, "접근 권한이 없습니다."),

	;

	private final HttpStatus status;
	private final Integer code;
	private final String message;

	public String getMessage(Throwable e) {
		return this.getMessage(this.getMessage() + " - " + e.getMessage());
	}

	public String getMessage(String message) {
		return Optional.ofNullable(message)
			.filter(Predicate.not(String::isBlank))
			.orElse(this.getMessage());
	}

	public String getDetailMessage(String message) {
		return this.getMessage() + " : " + message;
	}
}
