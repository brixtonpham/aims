package com.aims.application.event;

/**
 * Exception thrown when event publishing fails
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 3
 */
public class EventPublishingException extends RuntimeException {
    
    public EventPublishingException(String message) {
        super(message);
    }
    
    public EventPublishingException(String message, Throwable cause) {
        super(message, cause);
    }
}
