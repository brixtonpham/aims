package com.aims.domain.order.service.event;

import com.aims.domain.order.entity.Order.OrderStatus;

/**
 * Domain event fired when an order status changes
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 2 - Task 2.1
 */
public class OrderStatusChangedEvent extends DomainEvent {
    
    private final Long orderId;
    private final OrderStatus oldStatus;
    private final OrderStatus newStatus;
    private final String reason;
    
    public OrderStatusChangedEvent(Long orderId, OrderStatus oldStatus, OrderStatus newStatus, String reason) {
        super("OrderStatusChanged");
        this.orderId = orderId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.reason = reason;
    }
    
    public Long getOrderId() {
        return orderId;
    }
    
    public OrderStatus getOldStatus() {
        return oldStatus;
    }
    
    public OrderStatus getNewStatus() {
        return newStatus;
    }
    
    public String getReason() {
        return reason;
    }
    
    @Override
    public String toString() {
        return String.format("OrderStatusChangedEvent{orderId=%d, oldStatus=%s, newStatus=%s, reason='%s', %s}", 
            orderId, oldStatus, newStatus, reason, super.toString());
    }
}
