package org.balanceus.topping.application.exception;

/**
 * Application-layer error semantics that are independent of transport concerns.
 */
public enum ApplicationErrorCode {
    NOT_FOUND,
    ALREADY_EXISTS,
    FORBIDDEN,
    VALIDATION_ERROR,
    UNEXPECTED_ERROR;
}
