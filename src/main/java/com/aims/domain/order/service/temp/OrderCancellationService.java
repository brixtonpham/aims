package com.aims.domain.order.service.temp;

import com.aims.domain.order.entity.Order;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TEMPORARY FLATTENED SERVICE - Business logic extracted from CancelOrder module
 * 
 * Source: CancelOrder/Service/OrderCancellationService.java
 * Business Logic: Order cancellation rules, status validation, refund eligibility
 * 
 * PHASE 1 TASK 1.3: Flatten existing cancellation business logic
 */
@Service
public class OrderCancellationService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderCancellationService.class);
    
    /**
     * Validate order cancellation eligibility - BUSINESS LOGIC EXTRACTED
     * 
     * Original Business Rules from CancelOrder/Service/OrderValidationService:
     * - Order must exist
     * - Order must not already be cancelled
     * - Order must not be delivered
     * - Cancellation window rules apply
     */
    public void validateCancellationEligibility(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }
        
        if (Order.OrderStatus.CANCELLED == order.getStatus()) {
            throw new IllegalStateException("Order is already cancelled");
        }
        
        if (Order.OrderStatus.DELIVERED == order.getStatus()) {
            throw new IllegalStateException("Cannot cancel delivered order");
        }
        
        // Additional business rules from original CancelOrder logic
        if (Order.OrderStatus.SHIPPED == order.getStatus()) {
            throw new IllegalStateException("Cannot cancel shipped order");
        }
        
        logger.info("Order {} is eligible for cancellation", order.getOrderId());
    }
    
    /**
     * Cancel order - BUSINESS LOGIC EXTRACTED
     * 
     * Original Business Rules from CancelOrder orchestration:
     * - Validate cancellation eligibility
     * - Update order status to cancelled
     * - Calculate refund amount (full refund for eligible orders)
     */
    public void cancelOrder(Order order) {
        logger.info("Starting cancellation process for order: {}", order.getOrderId());
        
        // Step 1: Validate eligibility
        validateCancellationEligibility(order);
        
        // Step 2: Update status (business rule from original)
        order.setStatus(Order.OrderStatus.CANCELLED);
        
        logger.info("Order {} successfully cancelled", order.getOrderId());
    }
    
    /**
     * Calculate refund amount - BUSINESS LOGIC EXTRACTED
     * 
     * Original Business Rules from CancelOrder/Service/RefundService:
     * - Full refund for eligible cancellations
     * - Refund = total_after_VAT (includes all charges)
     */
    public long calculateRefundAmount(Order order) {
        validateCancellationEligibility(order);
        
        // Business rule: Full refund of total after VAT
        long refundAmount = order.getTotalAfterVAT();
        
        logger.info("Calculated refund amount: {} for order: {}", refundAmount, order.getOrderId());
        return refundAmount;
    }
    
    /**
     * Determine payment method for refund - BUSINESS LOGIC EXTRACTED
     * 
     * Original Business Rules from CancelOrder/Strategy pattern:
     * - VNPay orders -> VNPay refund
     * - COD orders -> Manual refund process
     * 
     * Note: Payment method will be added to Order entity in Phase 2
     */
    public String determineRefundMethod() {
        // Default from original CancelOrder implementation
        // Will be enhanced when Order entity includes payment method
        return "VNPAY";
    }
}
