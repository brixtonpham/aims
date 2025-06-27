package com.aims.domain.order.service.event;

import java.time.LocalDateTime;

/**
 * Base class for all domain events
 * Follows Domain-Driven Design event pattern
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 2 - Task 2.1
 */
public abstract class DomainEvent {
    
    private final String eventId;
    private final LocalDateTime occurredOn;
    private final String eventType;
    
    protected DomainEvent(String eventType) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.eventType = eventType;
    }
    
    public String getEventId() {
        return eventId;
    }
    
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    @Override
    public String toString() {
        return String.format("%s{eventId='%s', occurredOn=%s}", 
            getClass().getSimpleName(), eventId, occurredOn);
    }
}
