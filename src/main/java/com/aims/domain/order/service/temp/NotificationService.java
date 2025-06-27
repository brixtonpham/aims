package com.aims.domain.order.service.temp;

import com.aims.domain.order.entity.Order;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TEMPORARY FLATTENED SERVICE - Business logic extracted from CancelOrder and PlaceOrder modules
 * 
 * Source: CancelOrder/Service/EmailNotification_CancelOrder.java
 *         PlaceOrder/Service/EmailNotification_PlaceOrder.java
 * Business Logic: Customer notifications, email templates, notification rules
 * 
 * PHASE 1 TASK 1.3: Flatten existing notification business logic
 */
@Service
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    /**
     * Send order confirmation notification - BUSINESS LOGIC EXTRACTED
     * 
     * Original Business Rules from PlaceOrder email notification:
     * - Send to customer email
     * - Include order details and tracking info
     * - Provide payment instructions if needed
     */
    public void sendOrderConfirmationNotification(Order order) {
        logger.info("Sending order confirmation notification for order: {}", order.getOrderId());
        
        // Business logic flattened from original email notification
        String message = buildOrderConfirmationMessage(order);
        
        // For Phase 1: Log the notification (actual email sending in Phase 2)
        logger.info("Order confirmation notification prepared: {}", message);
        
        // TODO Phase 2: Integrate with actual email service
    }
    
    /**
     * Send order cancellation notification - BUSINESS LOGIC EXTRACTED
     * 
     * Original Business Rules from CancelOrder email notification:
     * - Notify customer of successful cancellation
     * - Include refund information
     * - Provide cancellation reference number
     */
    public void sendOrderCancellationNotification(Order order, long refundAmount) {
        logger.info("Sending order cancellation notification for order: {}", order.getOrderId());
        
        String message = buildOrderCancellationMessage(order, refundAmount);
        
        // For Phase 1: Log the notification (actual email sending in Phase 2)
        logger.info("Order cancellation notification prepared: {}", message);
        
        // TODO Phase 2: Integrate with actual email service
    }
    
    /**
     * Send order status update notification - BUSINESS LOGIC EXTRACTED
     * 
     * Business Rules: Notify customer when order status changes
     */
    public void sendOrderStatusUpdateNotification(Order order, Order.OrderStatus previousStatus) {
        logger.info("Sending status update notification for order: {} from {} to {}", 
            order.getOrderId(), previousStatus, order.getStatus());
        
        String message = buildStatusUpdateMessage(order, previousStatus);
        
        // For Phase 1: Log the notification (actual email sending in Phase 2)
        logger.info("Status update notification prepared: {}", message);
    }
    
    /**
     * Build order confirmation message - BUSINESS LOGIC EXTRACTED
     */
    private String buildOrderConfirmationMessage(Order order) {
        return String.format(
            "Order Confirmation - Order #%d\n" +
            "Status: %s\n" +
            "Total Amount: %d VND\n" +
            "Order Date: %s\n" +
            "Thank you for your order!",
            order.getOrderId(),
            order.getStatus().getDisplayName(),
            order.getTotalAfterVAT(),
            order.getOrderTime()
        );
    }
    
    /**
     * Build order cancellation message - BUSINESS LOGIC EXTRACTED
     */
    private String buildOrderCancellationMessage(Order order, long refundAmount) {
        return String.format(
            "Order Cancellation Confirmation - Order #%d\n" +
            "Your order has been successfully cancelled.\n" +
            "Refund Amount: %d VND\n" +
            "Refund will be processed within 3-5 business days.\n" +
            "Thank you for your understanding.",
            order.getOrderId(),
            refundAmount
        );
    }
    
    /**
     * Build status update message - BUSINESS LOGIC EXTRACTED
     */
    private String buildStatusUpdateMessage(Order order, Order.OrderStatus previousStatus) {
        return String.format(
            "Order Status Update - Order #%d\n" +
            "Previous Status: %s\n" +
            "Current Status: %s\n" +
            "Order Date: %s",
            order.getOrderId(),
            previousStatus.getDisplayName(),
            order.getStatus().getDisplayName(),
            order.getOrderTime()
        );
    }
    
    /**
     * Check if notification should be sent - BUSINESS LOGIC EXTRACTED
     * 
     * Business Rules: Some status changes don't require notifications
     */
    public boolean shouldSendNotification(Order.OrderStatus currentStatus, Order.OrderStatus previousStatus) {
        // Don't send notification if status hasn't changed
        if (currentStatus == previousStatus) {
            return false;
        }
        
        // Always notify for major status changes
        return currentStatus == Order.OrderStatus.CONFIRMED ||
               currentStatus == Order.OrderStatus.SHIPPED ||
               currentStatus == Order.OrderStatus.DELIVERED ||
               currentStatus == Order.OrderStatus.CANCELLED;
    }
}
