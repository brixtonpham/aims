package com.aims.application.event.handler;

import com.aims.application.event.EventHandler;
import com.aims.domain.order.service.event.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Event handler for order created events
 * Handles post-order creation logic like sending notifications
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 3
 */
@Component
public class OrderCreatedEventHandler implements EventHandler<OrderCreatedEvent> {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderCreatedEventHandler.class);
    
    @Override
    public void handle(OrderCreatedEvent event) {
        logger.info("Handling order created event for order ID: {}", event.getOrderId());
        
        try {
            // Send notification to customer
            sendCustomerNotification(event);
            
            // Update inventory reservations
            updateInventoryReservations(event);
            
            // Log for audit trail
            logOrderCreation(event);
            
            logger.info("Successfully handled order created event for order ID: {}", event.getOrderId());
            
        } catch (Exception e) {
            logger.error("Failed to handle order created event for order ID: {}", event.getOrderId(), e);
            // Re-throw with additional context
            throw new EventHandlingException("Failed to process order created event for order: " + event.getOrderId(), e);
        }
    }
    
    @Override
    public Class<OrderCreatedEvent> getEventType() {
        return OrderCreatedEvent.class;
    }
    
    @Override
    public int getPriority() {
        return 10; // High priority for order creation handling
    }
    
    private void sendCustomerNotification(OrderCreatedEvent event) {
        // Placeholder: In a real implementation, this would integrate with notification service
        logger.info("Sending order confirmation notification to customer: {}", event.getCustomerId());
    }
    
    private void updateInventoryReservations(OrderCreatedEvent event) {
        // Placeholder: In a real implementation, this would integrate with inventory service
        logger.info("Updating inventory reservations for order: {}", event.getOrderId());
    }
    
    private void logOrderCreation(OrderCreatedEvent event) {
        logger.info("Order created - ID: {}, Customer: {}, Amount: {}", 
                   event.getOrderId(), event.getCustomerId(), event.getTotalAmount());
    }
}
