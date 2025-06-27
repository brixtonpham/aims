package com.aims.domain.order.entity;

/**
 * Order status enumeration defining the lifecycle states of an order.
 * 
 * Status Flow:
 * PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
 *    ↓         ↓           ↓          ↓
 * CANCELLED  CANCELLED   CANCELLED   RETURNED
 * 
 * Business Rules:
 * - Orders start in PENDING status
 * - CANCELLED and RETURNED are terminal states
 * - Status transitions must follow the defined flow
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 1 - Foundation
 */
public enum OrderStatus {
    
    /**
     * Order has been created but not yet confirmed.
     * Customer can still modify or cancel.
     */
    PENDING("Pending", "Order is waiting for confirmation"),
    
    /**
     * Order has been confirmed and accepted for processing.
     * Inventory has been reserved.
     */
    CONFIRMED("Confirmed", "Order confirmed and inventory reserved"),
    
    /**
     * Order is being prepared and processed.
     * Items are being picked and packed.
     */
    PROCESSING("Processing", "Order is being prepared for shipment"),
    
    /**
     * Order has been shipped and is on the way to customer.
     * Tracking information is available.
     */
    SHIPPED("Shipped", "Order has been shipped"),
    
    /**
     * Order has been successfully delivered to customer.
     * Final successful state.
     */
    DELIVERED("Delivered", "Order has been delivered successfully"),
    
    /**
     * Order has been cancelled before delivery.
     * Terminal state - refund may be processed.
     */
    CANCELLED("Cancelled", "Order has been cancelled"),
    
    /**
     * Order was delivered but returned by customer.
     * Terminal state - return processing required.
     */
    RETURNED("Returned", "Order has been returned by customer");
    
    private final String displayName;
    private final String description;
    
    OrderStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * Gets user-friendly display name for the status
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets detailed description of the status
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Checks if this status allows order cancellation
     */
    public boolean isCancellable() {
        return this == PENDING || this == CONFIRMED || this == PROCESSING;
    }
    
    /**
     * Checks if this status allows order modification
     */
    public boolean isModifiable() {
        return this == PENDING;
    }
    
    /**
     * Checks if this is a terminal status (no further transitions allowed)
     */
    public boolean isTerminal() {
        return this == CANCELLED || this == RETURNED || this == DELIVERED;
    }
    
    /**
     * Checks if this status indicates successful completion
     */
    public boolean isSuccessful() {
        return this == DELIVERED;
    }
    
    /**
     * Checks if refund may be applicable for this status
     */
    public boolean isRefundEligible() {
        return this == CANCELLED || this == RETURNED;
    }
    
    /**
     * Gets the next possible statuses from current status
     */
    public OrderStatus[] getNextPossibleStatuses() {
        switch (this) {
            case PENDING:
                return new OrderStatus[]{CONFIRMED, CANCELLED};
            case CONFIRMED:
                return new OrderStatus[]{PROCESSING, CANCELLED};
            case PROCESSING:
                return new OrderStatus[]{SHIPPED, CANCELLED};
            case SHIPPED:
                return new OrderStatus[]{DELIVERED, RETURNED};
            case DELIVERED:
                return new OrderStatus[]{RETURNED};
            case CANCELLED:
            case RETURNED:
            default:
                return new OrderStatus[0]; // Terminal states or invalid
        }
    }
    
    /**
     * Validates if transition to target status is allowed
     */
    public boolean canTransitionTo(OrderStatus targetStatus) {
        OrderStatus[] possibleStatuses = getNextPossibleStatuses();
        for (OrderStatus status : possibleStatuses) {
            if (status == targetStatus) {
                return true;
            }
        }
        return false;
    }
}
