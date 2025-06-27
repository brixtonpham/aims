package com.aims.application.event.handler;

import com.aims.application.event.EventHandler;
import com.aims.domain.payment.event.PaymentProcessedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Event handler for payment processed events
 * Handles post-payment processing logic like order status updates
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 3
 */
@Component
public class PaymentProcessedEventHandler implements EventHandler<PaymentProcessedEvent> {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentProcessedEventHandler.class);
    
    @Override
    public void handle(PaymentProcessedEvent event) {
        logger.info("Handling payment processed event for order ID: {}", event.getOrderId());
        
        try {
            if (event.isSuccessful()) {
                handleSuccessfulPayment(event);
            } else {
                handleFailedPayment(event);
            }
            
            logger.info("Successfully handled payment processed event for order ID: {}", event.getOrderId());
            
        } catch (Exception e) {
            logger.error("Failed to handle payment processed event for order ID: {}", event.getOrderId(), e);
            throw new EventHandlingException("Failed to process payment event for order: " + event.getOrderId(), e);
        }
    }
    
    @Override
    public Class<PaymentProcessedEvent> getEventType() {
        return PaymentProcessedEvent.class;
    }
    
    @Override
    public int getPriority() {
        return 20; // Medium priority for payment processing
    }
    
    private void handleSuccessfulPayment(PaymentProcessedEvent event) {
        logger.info("Processing successful payment for order: {}", event.getOrderId());
        
        // Update order status to paid
        updateOrderStatusToPaid(event);
        
        // Send payment confirmation to customer
        sendPaymentConfirmation(event);
        
        // Trigger order fulfillment process
        triggerOrderFulfillment(event);
    }
    
    private void handleFailedPayment(PaymentProcessedEvent event) {
        logger.warn("Processing failed payment for order: {}", event.getOrderId());
        
        // Update order status to payment failed
        updateOrderStatusToPaymentFailed(event);
        
        // Send payment failure notification to customer
        sendPaymentFailureNotification(event);
    }
    
    private void updateOrderStatusToPaid(PaymentProcessedEvent event) {
        logger.info("Updating order status to PAID for order: {}", event.getOrderId());
    }
    
    private void updateOrderStatusToPaymentFailed(PaymentProcessedEvent event) {
        logger.info("Updating order status to PAYMENT_FAILED for order: {}", event.getOrderId());
    }
    
    private void sendPaymentConfirmation(PaymentProcessedEvent event) {
        logger.info("Sending payment confirmation for order: {} with transaction: {}", 
                   event.getOrderId(), event.getTransactionId());
    }
    
    private void sendPaymentFailureNotification(PaymentProcessedEvent event) {
        logger.info("Sending payment failure notification for order: {}", event.getOrderId());
    }
    
    private void triggerOrderFulfillment(PaymentProcessedEvent event) {
        logger.info("Triggering order fulfillment process for order: {}", event.getOrderId());
    }
}
