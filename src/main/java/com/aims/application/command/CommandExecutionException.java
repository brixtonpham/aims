package com.aims.application.command;

/**
 * Exception thrown when command execution fails
 */
public class CommandExecutionException extends RuntimeException {
    
    public CommandExecutionException(String message) {
        super(message);
    }
    
    public CommandExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
