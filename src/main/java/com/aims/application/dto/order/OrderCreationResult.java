package com.aims.application.dto.order;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Result object for order creation operations.
 * Contains the outcome of order placement with all relevant information.
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 1 - Foundation
 */
@Data
@Builder
public class OrderCreationResult {
    
    /**
     * Indicates if the order was successfully created
     */
    private boolean success;
    
    /**
     * Unique identifier of the created order
     */
    private String orderId;
    
    /**
     * Current status of the order
     */
    private String orderStatus;
    
    /**
     * Timestamp when the order was created
     */
    private LocalDateTime createdAt;
    
    /**
     * Total order amount including all charges
     */
    private Double totalAmount;
    
    /**
     * Currency of the order
     */
    private String currency;
    
    /**
     * Payment URL for online payment methods
     */
    private String paymentUrl;
    
    /**
     * Tracking information for order follow-up
     */
    private String trackingInfo;
    
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
     * Creates a successful order creation result
     */
    public static OrderCreationResult success(String orderId, String orderStatus, Double totalAmount, 
                                            String currency, String paymentUrl) {
        return OrderCreationResult.builder()
                .success(true)
                .orderId(orderId)
                .orderStatus(orderStatus)
                .totalAmount(totalAmount)
                .currency(currency)
                .paymentUrl(paymentUrl)
                .createdAt(LocalDateTime.now())
                .message("Order created successfully")
                .build();
    }
    
    /**
     * Creates a failed order creation result
     */
    public static OrderCreationResult failure(String errorCode, String message) {
        return OrderCreationResult.builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
