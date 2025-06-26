package com.aims.presentation.dto;

/**
 * Request DTO for Cart operations
 */
public class CartRequest {
    
    private Long customerId;
    private Long productId;
    private Integer quantity;
    
    // Constructors
    public CartRequest() {
        // Default constructor for JSON deserialization
    }
    
    public CartRequest(Long customerId, Long productId, Integer quantity) {
        this.customerId = customerId;
        this.productId = productId;
        this.quantity = quantity;
    }
    
    // Getters and Setters
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
