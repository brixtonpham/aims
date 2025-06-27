package com.aims.application.commands;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

/**
 * Command for cancelling an existing order.
 * Encapsulates all required information for order cancellation with validation.
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 1 - Foundation
 */
@Data
@Builder
public class CancelOrderCommand {
    
    /**
     * Unique identifier of the order to cancel
     */
    @NotBlank(message = "Order ID is required")
    private String orderId;
    
    /**
     * Customer ID requesting the cancellation (for authorization)
     */
    @NotBlank(message = "Customer ID is required")
    private String customerId;
    
    /**
     * Reason for order cancellation
     */
    @NotBlank(message = "Cancellation reason is required")
    @Size(min = 5, max = 500, message = "Cancellation reason must be between 5 and 500 characters")
    private String cancellationReason;
    
    /**
     * Flag indicating if refund should be processed
     */
    @Builder.Default
    private Boolean processRefund = true;
    
    /**
     * Additional notes for cancellation
     */
    @Size(max = 1000, message = "Additional notes cannot exceed 1000 characters")
    private String additionalNotes;
}
