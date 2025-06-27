package com.aims.domain.order.service.event;

/**
 * Domain event fired when an order is created
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 2 - Task 2.1
 */
public class OrderCreatedEvent extends DomainEvent {
    
    private final Long orderId;
    private final String customerId;
    private final Long totalAmount;
    
    public OrderCreatedEvent(Long orderId, String customerId, Long totalAmount) {
        super("OrderCreated");
        this.orderId = orderId;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
    }
    
    public Long getOrderId() {
        return orderId;
    }
    
    public String getCustomerId() {
        return customerId;
    }
    
    public Long getTotalAmount() {
        return totalAmount;
    }
    
    @Override
    public String toString() {
        return String.format("OrderCreatedEvent{orderId=%d, customerId='%s', totalAmount=%d, %s}", 
            orderId, customerId, totalAmount, super.toString());
    }
}
