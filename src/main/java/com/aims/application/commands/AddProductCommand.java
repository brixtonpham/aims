package com.aims.application.commands;

import com.aims.domain.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

/**
 * Command object for adding a new product.
 * Encapsulates all data needed for product creation workflow.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddProductCommand {
    
    @NotNull(message = "Product information is required")
    @Valid
    private Product product;
    
    /**
     * Optional field to indicate if product should be immediately available
     */
    @Builder.Default
    private boolean makeAvailableImmediately = true;
    
    /**
     * Optional field for audit purposes - who is creating the product
     */
    private String createdBy;
    
    /**
     * Optional field for business context - source of product creation
     */
    private String source; // e.g., "ADMIN_PANEL", "BULK_IMPORT", "API"
    
    /**
     * Validation method to ensure command is ready for processing
     */
    public boolean isValid() {
        return product != null && product.isValid();
    }
}
