package com.aims.application.command;

/**
 * Exception thrown when command validation fails
 */
public class CommandValidationException extends RuntimeException {
    
    public CommandValidationException(String message) {
        super(message);
    }
    
    public CommandValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
