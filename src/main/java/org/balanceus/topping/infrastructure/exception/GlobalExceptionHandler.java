package org.balanceus.topping.infrastructure.exception;

import static org.springframework.http.HttpStatus.*;

import org.balanceus.topping.application.exception.ApplicationErrorCode;
import org.balanceus.topping.application.exception.ApplicationException;
import org.balanceus.topping.infrastructure.response.ApiResponseData;
import org.balanceus.topping.infrastructure.response.Code;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.http.MediaType;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
	//스프링에서 감지하는 에러들

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<String> handleNoResource(NoResourceFoundException ex) {
		log.warn("Resource not found: {}", ex.getResourcePath());
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.contentType(MediaType.TEXT_PLAIN)
				.body("Not Found: " + ex.getResourcePath());
	}

	@ExceptionHandler(MultipartException.class)
	public ResponseEntity<String> handleMultipartException(MultipartException e) {
		log.error("Multipart parsing failed", e);
		log.error("Multipart exception details - Cause: {}, Message: {}", 
				e.getCause() != null ? e.getCause().getClass().getSimpleName() : "none", 
				e.getMessage());
		
		// Log additional details about the root cause
		Throwable rootCause = e;
		while (rootCause.getCause() != null) {
			rootCause = rootCause.getCause();
		}
		log.error("Root cause: {} - {}", rootCause.getClass().getSimpleName(), rootCause.getMessage());
		
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body("파일 업로드 처리 중 오류가 발생했습니다. 파일 크기가 10MB를 초과하지 않는지, 지원되는 파일 형식(JPG, PNG)인지 확인해주세요.");
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ApiResponseData<String>> handleRuntimeException(RuntimeException e) {
		log.error("Runtime exception occurred", e);
		String userMessage = getUserFriendlyMessage(e);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(ApiResponseData.failure(500, userMessage));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponseData<String>> handleException(Exception e) {
		log.error("Unexpected exception occurred", e);
		String userMessage = getUserFriendlyMessage(e);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(ApiResponseData.failure(500, userMessage));
	}

	private String getUserFriendlyMessage(Exception e) {
		String message = e.getMessage();
		if (message == null) {
			return "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
		}
		
		// SQL 관련 오류 처리
		if (message.toLowerCase().contains("sql") || 
			message.toLowerCase().contains("database") ||
			message.toLowerCase().contains("connection") ||
			message.toLowerCase().contains("constraint") ||
			message.toLowerCase().contains("foreign key") ||
			message.toLowerCase().contains("duplicate entry") ||
			message.toLowerCase().contains("table") ||
			message.toLowerCase().contains("column")) {
			return "데이터 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
		}
		
		// 네트워크 관련 오류 처리
		if (message.toLowerCase().contains("timeout") ||
			message.toLowerCase().contains("connection refused") ||
			message.toLowerCase().contains("network")) {
			return "네트워크 연결에 문제가 있습니다. 인터넷 연결을 확인한 후 다시 시도해주세요.";
		}
		
		// 인증/권한 관련 오류 처리
		if (message.toLowerCase().contains("unauthorized") ||
			message.toLowerCase().contains("access denied") ||
			message.toLowerCase().contains("permission") ||
			message.toLowerCase().contains("forbidden")) {
			return "접근 권한이 없습니다. 로그인 상태를 확인해주세요.";
		}
		
		// 파일 업로드 관련 오류 처리
		if (message.toLowerCase().contains("multipart") ||
			message.toLowerCase().contains("file size") ||
			message.toLowerCase().contains("upload")) {
			return "파일 업로드 중 오류가 발생했습니다. 파일 크기와 형식을 확인해주세요.";
		}
		
		// 검증 오류 처리
		if (message.toLowerCase().contains("validation") ||
			message.toLowerCase().contains("invalid") ||
			message.toLowerCase().contains("not found")) {
			return "입력하신 정보를 다시 확인해주세요.";
		}
		
		// 기본 메시지 (기술적 세부사항 숨김)
		return "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
	}

	// 커스텀 에러처리 가능

	@ExceptionHandler(AuthenticationFailedException.class)
	public ResponseEntity<ApiResponseData<String>> handleAuthenticationFailedException(AuthenticationFailedException e) {
		log.warn("Authentication failed: {}", e.getMessage());
		return ResponseEntity.status(e.getErrorCode().getStatus())
			.body(ApiResponseData.failure(e.getErrorCode().getCode(), e.getMessage()));
	}

	@ExceptionHandler(UserAlreadyExistsException.class)
	public ResponseEntity<ApiResponseData<String>> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
		log.warn("User already exists: {}", e.getMessage());
		return ResponseEntity.status(e.getErrorCode().getStatus())
			.body(ApiResponseData.failure(e.getErrorCode().getCode(), e.getMessage()));
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ApiResponseData<String>> handleUserNotFoundException(UserNotFoundException e) {
		log.warn("User not found: {}", e.getMessage());
		return ResponseEntity.status(e.getErrorCode().getStatus())
			.body(ApiResponseData.failure(e.getErrorCode().getCode(), e.getMessage()));
	}

	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<ApiResponseData<String>> handleValidationException(ValidationException e) {
		log.warn("Validation error: {}", e.getMessage());
		return ResponseEntity.status(e.getErrorCode().getStatus())
			.body(ApiResponseData.failure(e.getErrorCode().getCode(), e.getMessage()));
	}

	@ExceptionHandler(ApplicationException.class)
	public ResponseEntity<ApiResponseData<String>> handleApplicationException(ApplicationException e) {
		Code mappedCode = mapToResponseCode(e.getErrorCode());
		return ResponseEntity.status(mappedCode.getStatus())
			.body(ApiResponseData.failure(mappedCode.getCode(), e.getMessage()));
	}

	@ExceptionHandler(BaseException.class)
	public ResponseEntity<ApiResponseData<String>> handleException(BaseException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(
				ApiResponseData.failure(e.getErrorCode().getCode(), e.getErrorCode().getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponseData<Object>> handleMethodArgumentNotValidException(
		MethodArgumentNotValidException e) {
		StringBuilder sb = new StringBuilder();
		e.getBindingResult().getFieldErrors()
			.stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
			.forEach(message -> sb.append(message).append("\n"));

		if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
			sb.deleteCharAt(sb.length() - 1);  // 마지막 문자가 개행 문자라면 삭제
		}

		String errorMessages = sb.toString();
		return ResponseEntity.status(BAD_REQUEST).body(ApiResponseData.failure(0, errorMessages));
	}

	private Code mapToResponseCode(ApplicationErrorCode errorCode) {
		return switch (errorCode) {
			case NOT_FOUND -> Code.NOT_FOUND;
			case ALREADY_EXISTS -> Code.ALREADY_EXISTS;
			case FORBIDDEN -> Code.FORBIDDEN;
			case VALIDATION_ERROR -> Code.VALIDATION_ERROR;
			case UNEXPECTED_ERROR -> Code.INTERNAL_SERVER_ERROR;
		};
	}
}
