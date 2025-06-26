package com.aims.application.exceptions;

/**
 * Exception thrown when product application service operations fail.
 * Provides more specific error handling than generic exceptions.
 */
public class ProductApplicationException extends RuntimeException {
    
    public ProductApplicationException(String message) {
        super(message);
    }
    
    public ProductApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
