package com.aims.application.commands;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

/**
 * Command for processing a refund.
 * Encapsulates all required information for refund processing with validation.
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 1 - Foundation
 */
@Data
@Builder
public class RefundCommand {
    
    /**
     * Original transaction ID to refund
     */
    @NotBlank(message = "Transaction ID is required")
    private String transactionId;
    
    /**
     * Order ID associated with the refund
     */
    @NotBlank(message = "Order ID is required")
    private String orderId;
    
    /**
     * Customer ID requesting the refund
     */
    @NotBlank(message = "Customer ID is required")
    private String customerId;
    
    /**
     * Refund amount (can be partial)
     */
    @NotNull(message = "Refund amount is required")
    @DecimalMin(value = "0.01", message = "Refund amount must be positive")
    private Double refundAmount;
    
    /**
     * Refund currency (should match original payment)
     */
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO code")
    private String currency;
    
    /**
     * Reason for the refund
     */
    @NotBlank(message = "Refund reason is required")
    @Size(min = 5, max = 500, message = "Refund reason must be between 5 and 500 characters")
    private String refundReason;
    
    /**
     * Additional notes for the refund
     */
    @Size(max = 1000, message = "Additional notes cannot exceed 1000 characters")
    private String additionalNotes;
}
