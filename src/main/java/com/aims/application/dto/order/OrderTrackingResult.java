package com.aims.application.dto.order;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Result object for order tracking operations.
 * Contains comprehensive tracking information for order status inquiry.
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 1 - Foundation
 */
@Data
@Builder
public class OrderTrackingResult {
    
    /**
     * Indicates if the tracking inquiry was successful
     */
    private boolean success;
    
    /**
     * Unique identifier of the tracked order
     */
    private String orderId;
    
    /**
     * Current status of the order
     */
    private String currentStatus;
    
    /**
     * Human-readable status description
     */
    private String statusDescription;
    
    /**
     * Timestamp when status was last updated
     */
    private LocalDateTime lastUpdated;
    
    /**
     * Estimated delivery date
     */
    private LocalDateTime estimatedDelivery;
    
    /**
     * Order history with status transitions
     */
    private List<OrderStatusHistory> statusHistory;
    
    /**
     * Tracking number for delivery service
     */
    private String trackingNumber;
    
    /**
     * Current location of the order
     */
    private String currentLocation;
    
    /**
     * Next expected action or milestone
     */
    private String nextAction;
    
    /**
     * Success or error message
     */
    private String message;
    
    /**
     * Error code if operation failed
     */
    private String errorCode;
    
    /**
     * Represents a single status change in order history
     */
    @Data
    @Builder
    public static class OrderStatusHistory {
        private String status;
        private String description;
        private LocalDateTime timestamp;
        private String location;
        private String notes;
    }
    
    /**
     * Creates a successful tracking result
     */
    public static OrderTrackingResult success(String orderId, String currentStatus, 
                                            String statusDescription, List<OrderStatusHistory> history) {
        return OrderTrackingResult.builder()
                .success(true)
                .orderId(orderId)
                .currentStatus(currentStatus)
                .statusDescription(statusDescription)
                .statusHistory(history)
                .lastUpdated(LocalDateTime.now())
                .message("Order tracking retrieved successfully")
                .build();
    }
    
    /**
     * Creates a failed tracking result
     */
    public static OrderTrackingResult failure(String orderId, String errorCode, String message) {
        return OrderTrackingResult.builder()
                .success(false)
                .orderId(orderId)
                .errorCode(errorCode)
                .message(message)
                .build();
    }
}
