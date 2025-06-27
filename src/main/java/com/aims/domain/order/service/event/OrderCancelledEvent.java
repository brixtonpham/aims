package com.aims.domain.order.service.event;

/**
 * Domain event fired when an order is cancelled
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 2 - Task 2.1
 */
public class OrderCancelledEvent extends DomainEvent {
    
    private final Long orderId;
    private final String customerId;
    private final Long refundAmount;
    
    public OrderCancelledEvent(Long orderId, String customerId, Long refundAmount) {
        super("OrderCancelled");
        this.orderId = orderId;
        this.customerId = customerId;
        this.refundAmount = refundAmount;
    }
    
    public Long getOrderId() {
        return orderId;
    }
    
    public String getCustomerId() {
        return customerId;
    }
    
    public Long getRefundAmount() {
        return refundAmount;
    }
    
    @Override
    public String toString() {
        return String.format("OrderCancelledEvent{orderId=%d, customerId='%s', refundAmount=%d, %s}", 
            orderId, customerId, refundAmount, super.toString());
    }
}
