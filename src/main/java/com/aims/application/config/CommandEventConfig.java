package com.aims.application.config;

import com.aims.application.event.EnhancedDomainEventPublisher;
import com.aims.application.event.EventHandler;
import com.aims.application.event.EventStore;
import com.aims.application.event.InMemoryEventStore;
import com.aims.domain.order.service.event.DomainEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

/**
 * Configuration for Command and Event infrastructure
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 3
 */
@Configuration
public class CommandEventConfig {
    
    /**
     * Configure the CommandBus with middleware
     * Note: The CommandBus is already a @Component and will be auto-configured
     * This bean definition is removed to avoid conflicts
     */
    // @Bean - Removed as CommandBus is already a @Component
    
    /**
     * Configure the Event Store
     */
    @Bean
    public EventStore eventStore() {
        return new InMemoryEventStore();
    }
    
    /**
     * Configure the Enhanced Domain Event Publisher
     */
    @Bean
    @Primary
    public DomainEventPublisher domainEventPublisher(ApplicationEventPublisher springEventPublisher,
                                                    EventStore eventStore,
                                                    List<EventHandler<?>> eventHandlers) {
        EnhancedDomainEventPublisher publisher = new EnhancedDomainEventPublisher(springEventPublisher, eventStore);
        
        // Register all event handlers
        eventHandlers.forEach(handler -> registerEventHandler(publisher, handler));
        
        return publisher;
    }
    
    /**
     * Helper method to register event handlers with proper type safety
     */
    private <T extends com.aims.domain.order.service.event.DomainEvent> void registerEventHandler(
            EnhancedDomainEventPublisher publisher, EventHandler<T> handler) {
        publisher.registerHandler(handler);
    }
}
