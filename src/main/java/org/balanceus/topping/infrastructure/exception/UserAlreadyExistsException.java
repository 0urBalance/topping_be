package org.balanceus.topping.infrastructure.exception;

import org.balanceus.topping.infrastructure.response.Code;

public class UserAlreadyExistsException extends BaseException {
    
    public UserAlreadyExistsException() {
        super(Code.SIGN003);
    }
    
    public UserAlreadyExistsException(String message) {
        super(Code.SIGN003, message);
    }
    
    public UserAlreadyExistsException(String message, Throwable cause) {
        super(Code.SIGN003, message, cause);
    }
}