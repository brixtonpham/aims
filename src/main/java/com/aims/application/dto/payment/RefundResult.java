package com.aims.application.dto.payment;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Result object for refund processing operations.
 * Contains the outcome of refund processing with all relevant information.
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 1 - Foundation
 */
@Data
@Builder
public class RefundResult {
    
    /**
     * Indicates if the refund was successfully processed
     */
    private boolean success;
    
    /**
     * Unique refund transaction identifier
     */
    private String refundTransactionId;
    
    /**
     * Original transaction ID being refunded
     */
    private String originalTransactionId;
    
    /**
     * Order ID associated with the refund
     */
    private String orderId;
    
    /**
     * Current refund status
     */
    private String refundStatus;
    
    /**
     * Refund amount processed
     */
    private Double refundAmount;
    
    /**
     * Refund currency
     */
    private String currency;
    
    /**
     * Gateway-specific refund reference
     */
    private String gatewayReference;
    
    /**
     * Reason for the refund
     */
    private String refundReason;
    
    /**
     * Timestamp when refund was processed
     */
    private LocalDateTime processedAt;
    
    /**
     * Expected completion date for the refund
     */
    private LocalDateTime expectedCompletion;
    
    /**
     * Success or error message
     */
    private String message;
    
    /**
     * Error code if refund failed
     */
    private String errorCode;
    
    /**
     * Additional metadata from payment gateway
     */
    private String metadata;
    
    /**
     * Creates a successful refund result
     */
    public static RefundResult success(String refundTransactionId, String originalTransactionId, 
                                     String orderId, Double refundAmount, String currency) {
        return RefundResult.builder()
                .success(true)
                .refundTransactionId(refundTransactionId)
                .originalTransactionId(originalTransactionId)
                .orderId(orderId)
                .refundStatus("PENDING")
                .refundAmount(refundAmount)
                .currency(currency)
                .processedAt(LocalDateTime.now())
                .expectedCompletion(LocalDateTime.now().plusDays(7)) // Default 7 days
                .message("Refund processed successfully")
                .build();
    }
    
    /**
     * Creates a failed refund result
     */
    public static RefundResult failure(String originalTransactionId, String orderId, 
                                     String errorCode, String message) {
        return RefundResult.builder()
                .success(false)
                .originalTransactionId(originalTransactionId)
                .orderId(orderId)
                .errorCode(errorCode)
                .message(message)
                .processedAt(LocalDateTime.now())
                .build();
    }
}
