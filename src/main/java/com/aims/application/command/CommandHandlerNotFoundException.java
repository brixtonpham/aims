package com.aims.application.command;

/**
 * Exception thrown when no handler is found for a command
 */
public class CommandHandlerNotFoundException extends RuntimeException {
    
    public CommandHandlerNotFoundException(String message) {
        super(message);
    }
    
    public CommandHandlerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
