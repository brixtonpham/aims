package com.aims.application.dto.payment;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Result object for payment processing operations.
 * Contains the outcome of payment processing with all relevant information.
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 1 - Foundation
 */
@Data
@Builder
public class PaymentResult {
    
    /**
     * Indicates if the payment was successfully processed
     */
    private boolean success;
    
    /**
     * Unique transaction identifier
     */
    private String transactionId;
    
    /**
     * Order ID associated with the payment
     */
    private String orderId;
    
    /**
     * Current payment status
     */
    private String paymentStatus;
    
    /**
     * Payment amount processed
     */
    private Double amount;
    
    /**
     * Payment currency
     */
    private String currency;
    
    /**
     * Payment method used
     */
    private String paymentMethod;
    
    /**
     * Payment URL for gateway redirect (if applicable)
     */
    private String paymentUrl;
    
    /**
     * Gateway-specific transaction reference
     */
    private String gatewayReference;
    
    /**
     * Timestamp when payment was processed
     */
    private LocalDateTime processedAt;
    
    /**
     * Success or error message
     */
    private String message;
    
    /**
     * Error code if payment failed
     */
    private String errorCode;
    
    /**
     * Additional metadata from payment gateway
     */
    private String metadata;
    
    /**
     * Creates a successful payment result
     */
    public static PaymentResult success(String transactionId, String orderId, String paymentStatus,
                                      Double amount, String currency, String paymentUrl) {
        return PaymentResult.builder()
                .success(true)
                .transactionId(transactionId)
                .orderId(orderId)
                .paymentStatus(paymentStatus)
                .amount(amount)
                .currency(currency)
                .paymentUrl(paymentUrl)
                .processedAt(LocalDateTime.now())
                .message("Payment processed successfully")
                .build();
    }
    
    /**
     * Creates a failed payment result
     */
    public static PaymentResult failure(String orderId, String errorCode, String message) {
        return PaymentResult.builder()
                .success(false)
                .orderId(orderId)
                .errorCode(errorCode)
                .message(message)
                .processedAt(LocalDateTime.now())
                .build();
    }
}
