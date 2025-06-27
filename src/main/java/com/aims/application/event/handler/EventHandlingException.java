package com.aims.application.event.handler;

/**
 * Exception thrown when event handling fails
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 3
 */
public class EventHandlingException extends RuntimeException {
    
    public EventHandlingException(String message) {
        super(message);
    }
    
    public EventHandlingException(String message, Throwable cause) {
        super(message, cause);
    }
}
