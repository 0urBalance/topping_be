package org.balanceus.topping.application.service;

import lombok.Getter;

@Getter
public class CollaborationApplicationException extends RuntimeException {
    private final String errorCode;
    private final String redirectUrl;

    public CollaborationApplicationException(String errorCode, String redirectUrl, String message) {
        super(message);
        this.errorCode = errorCode;
        this.redirectUrl = redirectUrl;
    }

    public CollaborationApplicationException(String errorCode, String redirectUrl) {
        this(errorCode, redirectUrl, errorCode);
    }
}
