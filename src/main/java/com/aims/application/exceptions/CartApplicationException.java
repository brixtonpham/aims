package com.aims.application.exceptions;

/**
 * Application exception for Cart operations
 */
public class CartApplicationException extends RuntimeException {
    
    public CartApplicationException(String message) {
        super(message);
    }
    
    public CartApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
