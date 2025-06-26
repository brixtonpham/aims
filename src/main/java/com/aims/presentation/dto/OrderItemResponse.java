package com.aims.presentation.dto;

import java.math.BigDecimal;

/**
 * Response DTO for Order Item operations
 */
public class OrderItemResponse {
    
    private Long orderItemId;
    private Long productId;
    private String productTitle;
    private String productType;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal totalPrice;
    
    // Constructors
    public OrderItemResponse() {
        // Default constructor for JSON serialization
    }
    
    // Getters and Setters
    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }
    
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
    
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
}
