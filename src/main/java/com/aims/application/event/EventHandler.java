package com.aims.application.event;

import com.aims.domain.order.service.event.DomainEvent;

/**
 * Interface for handling domain events
 * 
 * @param <T> The type of domain event to handle
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 3
 */
public interface EventHandler<T extends DomainEvent> {
    
    /**
     * Handle the given domain event
     * 
     * @param event The domain event to handle
     */
    void handle(T event);
    
    /**
     * Get the type of event this handler can process
     * 
     * @return The event class type
     */
    Class<T> getEventType();
    
    /**
     * Get the handler priority for ordering multiple handlers
     * Lower values indicate higher priority
     * 
     * @return The priority value (default: 100)
     */
    default int getPriority() {
        return 100;
    }
}
