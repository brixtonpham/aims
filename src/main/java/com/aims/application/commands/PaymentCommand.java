package com.aims.application.commands;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

/**
 * Command for processing a payment.
 * Encapsulates all required information for payment processing with comprehensive validation.
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 1 - Foundation
 */
@Data
@Builder
public class PaymentCommand {
    
    /**
     * Order ID associated with the payment
     */
    @NotBlank(message = "Order ID is required")
    private String orderId;
    
    /**
     * Customer ID making the payment
     */
    @NotBlank(message = "Customer ID is required")
    private String customerId;
    
    /**
     * Payment amount
     */
    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be positive")
    private Double amount;
    
    /**
     * Payment currency (ISO 4217 code)
     */
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO code")
    private String currency;
    
    /**
     * Payment method (VNPAY, COD, BANK_TRANSFER, etc.)
     */
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
    
    /**
     * Return URL for payment gateway redirect
     */
    @NotBlank(message = "Return URL is required")
    private String returnUrl;
    
    /**
     * Customer IP address for fraud detection
     */
    @NotBlank(message = "Customer IP is required")
    private String customerIpAddress;
    
    /**
     * Payment description for transaction records
     */
    @Size(max = 255, message = "Payment description cannot exceed 255 characters")
    private String description;
    
    /**
     * Additional payment metadata
     */
    private String metadata;
}
