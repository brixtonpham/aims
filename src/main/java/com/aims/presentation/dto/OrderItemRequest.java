package com.aims.presentation.dto;

import java.math.BigDecimal;

/**
 * Request DTO for Order Item operations
 * Used within OrderRequest for placing orders with multiple items
 */
public class OrderItemRequest {
    
    private Long productId;
    private String productTitle;
    private String productType;
    private BigDecimal unitPrice;
    private Integer quantity;
    
    // Constructors
    public OrderItemRequest() {
        // Default constructor for JSON deserialization
    }
    
    public OrderItemRequest(Long productId, String productTitle, String productType, 
                           BigDecimal unitPrice, Integer quantity) {
        this.productId = productId;
        this.productTitle = productTitle;
        this.productType = productType;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }
    
    // Getters and Setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public String getProductTitle() { return productTitle; }
    public void setProductTitle(String productTitle) { this.productTitle = productTitle; }
    
    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }
    
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    // Calculated field
    public BigDecimal getTotalPrice() {
        if (unitPrice != null && quantity != null) {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }
}
