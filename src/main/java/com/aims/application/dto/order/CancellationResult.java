package com.aims.application.dto.order;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Result object for order cancellation operations.
 * Contains the outcome of order cancellation with all relevant information.
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 1 - Foundation
 */
@Data
@Builder
public class CancellationResult {
    
    /**
     * Indicates if the cancellation was successful
     */
    private boolean success;
    
    /**
     * Unique identifier of the cancelled order
     */
    private String orderId;
    
    /**
     * New status of the order after cancellation
     */
    private String orderStatus;
    
    /**
     * Timestamp when the cancellation was processed
     */
    private LocalDateTime cancelledAt;
    
    /**
     * Refund amount to be processed
     */
    private Double refundAmount;
    
    /**
     * Currency of the refund
     */
    private String currency;
    
    /**
     * Refund transaction ID if processed
     */
    private String refundTransactionId;
    
    /**
     * Refund status
     */
    private String refundStatus;
    
    /**
     * Cancellation reason
     */
    private String cancellationReason;
    
    /**
     * Success or error message
     */
    private String message;
    
    /**
     * Error code if operation failed
     */
    private String errorCode;
    
    /**
     * Additional metadata for the result
     */
    private String metadata;
    
    /**
     * Creates a successful cancellation result
     */
    public static CancellationResult success(String orderId, String orderStatus, Double refundAmount, 
                                           String currency, String refundTransactionId) {
        return CancellationResult.builder()
                .success(true)
                .orderId(orderId)
                .orderStatus(orderStatus)
                .refundAmount(refundAmount)
                .currency(currency)
                .refundTransactionId(refundTransactionId)
                .refundStatus("PENDING")
                .cancelledAt(LocalDateTime.now())
                .message("Order cancelled successfully")
                .build();
    }
    
    /**
     * Creates a failed cancellation result
     */
    public static CancellationResult failure(String orderId, String errorCode, String message) {
        return CancellationResult.builder()
                .success(false)
                .orderId(orderId)
                .errorCode(errorCode)
                .message(message)
                .cancelledAt(LocalDateTime.now())
                .build();
    }
}
