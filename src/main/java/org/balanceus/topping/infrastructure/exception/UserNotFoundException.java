package org.balanceus.topping.infrastructure.exception;

import org.balanceus.topping.infrastructure.response.Code;

public class UserNotFoundException extends BaseException {
    
    public UserNotFoundException() {
        super(Code.SIGN001);
    }
    
    public UserNotFoundException(String message) {
        super(Code.SIGN001, message);
    }
    
    public UserNotFoundException(String message, Throwable cause) {
        super(Code.SIGN001, message, cause);
    }
}