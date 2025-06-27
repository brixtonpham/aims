package com.aims.application.event;

import com.aims.domain.order.service.event.DomainEvent;
import com.aims.domain.order.service.event.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Enhanced Domain Event Publisher that integrates with Spring Application Events
 * and supports event handlers and event store
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 3
 */
@Component
public class EnhancedDomainEventPublisher implements DomainEventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(EnhancedDomainEventPublisher.class);
    
    private final ApplicationEventPublisher springEventPublisher;
    private final EventStore eventStore;
    private final Map<Class<? extends DomainEvent>, List<EventHandler<? extends DomainEvent>>> handlers;
    
    public EnhancedDomainEventPublisher(ApplicationEventPublisher springEventPublisher, 
                                      EventStore eventStore) {
        this.springEventPublisher = springEventPublisher;
        this.eventStore = eventStore;
        this.handlers = new ConcurrentHashMap<>();
    }
    
    @Override
    public void publish(DomainEvent event) {
        logger.info("Publishing domain event: {}", event);
        
        try {
            // 1. Store the event
            eventStore.store(event);
            
            // 2. Publish via Spring Application Events
            springEventPublisher.publishEvent(event);
            
            // 3. Execute registered handlers
            executeHandlers(event);
            
            logger.debug("Successfully published domain event: {}", event.getEventId());
            
        } catch (Exception e) {
            logger.error("Failed to publish domain event: {}", event, e);
            throw new EventPublishingException("Failed to publish event: " + event.getEventId(), e);
        }
    }
    
    /**
     * Register an event handler
     * 
     * @param handler The event handler to register
     */
    public <T extends DomainEvent> void registerHandler(EventHandler<T> handler) {
        Class<T> eventType = handler.getEventType();
        handlers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add(handler);
        
        logger.info("Registered event handler {} for event type {}", 
                   handler.getClass().getSimpleName(), eventType.getSimpleName());
    }
    
    /**
     * Unregister an event handler
     * 
     * @param handler The event handler to unregister
     */
    public <T extends DomainEvent> void unregisterHandler(EventHandler<T> handler) {
        Class<T> eventType = handler.getEventType();
        List<EventHandler<? extends DomainEvent>> eventHandlers = handlers.get(eventType);
        if (eventHandlers != null) {
            eventHandlers.remove(handler);
            logger.info("Unregistered event handler {} for event type {}", 
                       handler.getClass().getSimpleName(), eventType.getSimpleName());
        }
    }
    
    /**
     * Execute all registered handlers for the given event
     */
    @SuppressWarnings("unchecked")
    private void executeHandlers(DomainEvent event) {
        List<EventHandler<? extends DomainEvent>> eventHandlers = handlers.get(event.getClass());
        if (eventHandlers != null && !eventHandlers.isEmpty()) {
            // Sort handlers by priority
            eventHandlers.stream()
                    .sorted((h1, h2) -> Integer.compare(h1.getPriority(), h2.getPriority()))
                    .forEach(handler -> {
                        try {
                            ((EventHandler<DomainEvent>) handler).handle(event);
                            logger.debug("Successfully executed handler {} for event {}", 
                                       handler.getClass().getSimpleName(), event.getEventId());
                        } catch (Exception e) {
                            logger.error("Failed to execute handler {} for event {}", 
                                       handler.getClass().getSimpleName(), event.getEventId(), e);
                            // Don't rethrow - we want other handlers to still execute
                        }
                    });
        }
    }
    
    /**
     * Get the number of registered handlers for a specific event type
     */
    public int getHandlerCount(Class<? extends DomainEvent> eventType) {
        List<EventHandler<? extends DomainEvent>> eventHandlers = handlers.get(eventType);
        return eventHandlers != null ? eventHandlers.size() : 0;
    }
}
