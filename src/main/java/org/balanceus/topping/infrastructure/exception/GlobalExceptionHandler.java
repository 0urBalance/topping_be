package org.balanceus.topping.infrastructure.exception;

import static org.springframework.http.HttpStatus.*;

import org.balanceus.topping.infrastructure.response.ApiResponseData;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
	//스프링에서 감지하는 에러들

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
	public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body("런타임 오류 발생: " + e.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> handleException(Exception e) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body("예상치 못한 오류 발생: " + e.getMessage());
	}

	// 커스텀 에러처리 가능

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
}
