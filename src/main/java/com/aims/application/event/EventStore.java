package com.aims.application.event;

import com.aims.domain.order.service.event.DomainEvent;

import java.util.List;

/**
 * Event Store interface for persisting and retrieving domain events
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 3
 */
public interface EventStore {
    
    /**
     * Store a domain event
     * 
     * @param event The domain event to store
     */
    void store(DomainEvent event);
    
    /**
     * Store multiple domain events in a batch
     * 
     * @param events The domain events to store
     */
    void storeAll(List<DomainEvent> events);
    
    /**
     * Retrieve events by event type
     * 
     * @param eventType The type of events to retrieve
     * @return List of events of the specified type
     */
    List<DomainEvent> getEventsByType(String eventType);
    
    /**
     * Retrieve events by aggregate ID (e.g., order ID)
     * 
     * @param aggregateId The aggregate identifier
     * @return List of events for the specified aggregate
     */
    List<DomainEvent> getEventsByAggregateId(String aggregateId);
    
    /**
     * Retrieve all events in chronological order
     * 
     * @return List of all stored events
     */
    List<DomainEvent> getAllEvents();
    
    /**
     * Get the total count of stored events
     * 
     * @return The total number of events in the store
     */
    long getEventCount();
}
