package org.balanceus.topping.infrastructure.exception;

import org.balanceus.topping.infrastructure.response.Code;

public class AuthenticationFailedException extends BaseException {
    
    public AuthenticationFailedException() {
        super(Code.SIGN002);
    }
    
    public AuthenticationFailedException(String message) {
        super(Code.SIGN002, message);
    }
    
    public AuthenticationFailedException(String message, Throwable cause) {
        super(Code.SIGN002, message, cause);
    }
}