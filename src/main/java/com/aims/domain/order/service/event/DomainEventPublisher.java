package com.aims.domain.order.service.event;

import org.springframework.stereotype.Component;

/**
 * Domain Event Publisher interface
 * Publishes domain events for loose coupling between domain services
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 2 - Task 2.1
 */
public interface DomainEventPublisher {
    
    /**
     * Publish a domain event
     * 
     * @param event The domain event to publish
     */
    void publish(DomainEvent event);
}

/**
 * Simple implementation of DomainEventPublisher
 * For now, just logs events - can be enhanced with Spring Events later
 */
@Component
class SimpleDomainEventPublisher implements DomainEventPublisher {
    
    private static final org.slf4j.Logger logger = 
        org.slf4j.LoggerFactory.getLogger(SimpleDomainEventPublisher.class);
    
    @Override
    public void publish(DomainEvent event) {
        logger.info("Publishing domain event: {}", event);
        
        // Phase 2: Simple logging implementation
        // Phase 3 will enhance this with Spring Application Events
    }
}
