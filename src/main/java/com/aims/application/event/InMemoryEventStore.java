package com.aims.application.event;

import com.aims.domain.order.service.event.DomainEvent;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory implementation of Event Store
 * For production use, this should be replaced with a persistent storage solution
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 3
 */
@Component
public class InMemoryEventStore implements EventStore {
    
    private final List<DomainEvent> events = new CopyOnWriteArrayList<>();
    private final Map<String, List<DomainEvent>> eventsByType = new ConcurrentHashMap<>();
    private final Map<String, List<DomainEvent>> eventsByAggregateId = new ConcurrentHashMap<>();
    
    @Override
    public void store(DomainEvent event) {
        events.add(event);
        
        // Index by type
        eventsByType.computeIfAbsent(event.getEventType(), k -> new CopyOnWriteArrayList<>()).add(event);
        
        // Index by aggregate ID if available (extracted from event ID or specific field)
        String aggregateId = extractAggregateId(event);
        if (aggregateId != null) {
            eventsByAggregateId.computeIfAbsent(aggregateId, k -> new CopyOnWriteArrayList<>()).add(event);
        }
    }
    
    @Override
    public void storeAll(List<DomainEvent> events) {
        events.forEach(this::store);
    }
    
    @Override
    public List<DomainEvent> getEventsByType(String eventType) {
        return new ArrayList<>(eventsByType.getOrDefault(eventType, Collections.emptyList()));
    }
    
    @Override
    public List<DomainEvent> getEventsByAggregateId(String aggregateId) {
        return new ArrayList<>(eventsByAggregateId.getOrDefault(aggregateId, Collections.emptyList()));
    }
    
    @Override
    public List<DomainEvent> getAllEvents() {
        return new ArrayList<>(events);
    }
    
    @Override
    public long getEventCount() {
        return events.size();
    }
    
    /**
     * Extract aggregate ID from domain event
     * This is a simple implementation - in a real system, this could be more sophisticated
     */
    private String extractAggregateId(DomainEvent event) {
        // For order events, we can extract the order ID
        if (event.getClass().getSimpleName().startsWith("Order")) {
            try {
                // Use reflection to get orderId field
                var field = event.getClass().getDeclaredField("orderId");
                Object orderId = field.get(event);
                return orderId != null ? orderId.toString() : null;
            } catch (Exception e) {
                // If reflection fails, return null
                return null;
            }
        }
        return null;
    }
}
