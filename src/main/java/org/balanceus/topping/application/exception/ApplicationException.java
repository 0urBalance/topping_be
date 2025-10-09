package org.balanceus.topping.application.exception;

import java.util.Objects;

/**
 * Base runtime exception for application-layer use cases.
 * Keeps the use case semantics decoupled from presentation/transport handling.
 */
public class ApplicationException extends RuntimeException {

    private final ApplicationErrorCode errorCode;

    public ApplicationException(ApplicationErrorCode errorCode, String message) {
        super(message);
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode must not be null");
    }

    public ApplicationException(ApplicationErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode must not be null");
    }

    public ApplicationErrorCode getErrorCode() {
        return errorCode;
    }
}
