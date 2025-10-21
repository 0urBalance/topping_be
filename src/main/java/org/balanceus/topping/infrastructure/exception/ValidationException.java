package org.balanceus.topping.infrastructure.exception;

import org.balanceus.topping.infrastructure.response.Code;

public class ValidationException extends BaseException {
    
    public ValidationException() {
        super(Code.VALIDATION_ERROR);
    }
    
    public ValidationException(String message) {
        super(Code.VALIDATION_ERROR, message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(Code.VALIDATION_ERROR, message, cause);
    }
}