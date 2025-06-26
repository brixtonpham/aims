package com.aims.application.commands;

import com.aims.domain.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.Valid;

/**
 * Command object for updating an existing product.
 * Encapsulates all data needed for product update workflow.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductCommand {
    
    @NotNull(message = "Product ID is required")
    @Positive(message = "Product ID must be positive")
    private Long productId;
    
    @NotNull(message = "Product information is required")
    @Valid
    private Product product;
    
    /**
     * Optional field for audit purposes - who is updating the product
     */
    private String updatedBy;
    
    /**
     * Optional field for business context - source of product update
     */
    private String source; // e.g., "ADMIN_PANEL", "BULK_UPDATE", "API"
    
    /**
     * Optional field to indicate if inventory changes should trigger notifications
     */
    @Builder.Default
    private boolean notifyInventoryChanges = false;
    
    /**
     * Validation method to ensure command is ready for processing
     */
    public boolean isValid() {
        return productId != null && productId > 0 && 
               product != null && product.isValid();
    }
}
