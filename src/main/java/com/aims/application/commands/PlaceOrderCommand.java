package com.aims.application.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Command for placing a new order.
 * Encapsulates all required information for order creation with comprehensive validation.
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 1 - Foundation
 */
@Data
@Builder
public class PlaceOrderCommand {
    
    /**
     * Customer identifier placing the order
     */
    @NotBlank(message = "Customer ID is required")
    private String customerId;
    
    /**
     * Customer name for order processing
     */
    @NotBlank(message = "Customer name is required")
    @Size(min = 2, max = 100, message = "Customer name must be between 2 and 100 characters")
    private String customerName;
    
    /**
     * Customer email for notifications
     */
    @NotBlank(message = "Customer email is required")
    @Email(message = "Valid email address is required")
    private String customerEmail;
    
    /**
     * Customer phone for delivery coordination
     */
    @NotBlank(message = "Customer phone is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Valid phone number is required")
    private String customerPhone;
    
    /**
     * List of items to be ordered
     */
    @NotNull(message = "Order items are required")
    @NotEmpty(message = "At least one order item is required")
    @Valid
    private List<OrderItemCommand> orderItems;
    
    /**
     * Delivery address information
     */
    @NotNull(message = "Delivery address is required")
    @Valid
    private DeliveryAddressCommand deliveryAddress;
    
    /**
     * Payment method selection
     */
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
    
    /**
     * Rush order flag for expedited processing
     */
    @Builder.Default
    private Boolean rushOrder = false;
    
    /**
     * Special delivery instructions
     */
    @Size(max = 500, message = "Delivery instructions cannot exceed 500 characters")
    private String deliveryInstructions;
    
    /**
     * Order notes or comments
     */
    @Size(max = 1000, message = "Order notes cannot exceed 1000 characters")
    private String orderNotes;
    
    /**
     * Represents an individual item in the order
     */
    @Data
    @Builder
    public static class OrderItemCommand {
        
        @NotBlank(message = "Product ID is required")
        private String productId;
        
        @NotBlank(message = "Product name is required")
        private String productName;
        
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 999, message = "Quantity cannot exceed 999")
        private Integer quantity;
        
        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.01", message = "Unit price must be positive")
        private Double unitPrice;
        
        /**
         * Optional product variant (size, color, etc.)
         */
        private String variant;
    }
    
    /**
     * Represents delivery address information
     */
    @Data
    @Builder
    public static class DeliveryAddressCommand {
        
        @NotBlank(message = "Street address is required")
        @Size(max = 200, message = "Street address cannot exceed 200 characters")
        private String streetAddress;
        
        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City cannot exceed 100 characters")
        private String city;
        
        @NotBlank(message = "State/Province is required")
        @Size(max = 100, message = "State/Province cannot exceed 100 characters")
        private String state;
        
        @NotBlank(message = "Postal code is required")
        @Pattern(regexp = "^[A-Za-z0-9\\s-]{3,10}$", message = "Valid postal code is required")
        private String postalCode;
        
        @NotBlank(message = "Country is required")
        @Size(max = 100, message = "Country cannot exceed 100 characters")
        private String country;
    }
}
