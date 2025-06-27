package com.aims.domain.order.dto;

/**
 * Request DTO for order items in create order request.
 * 
 * Represents a single item that the customer wants to order:
 * - Product identification
 * - Quantity requested
 * - Special requirements or customizations
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 1 - Foundation
 */
public class OrderItemRequest {
    
    private String productId;
    private String productName;
    private Integer quantity;
    private Double unitPrice;
    private String productSku;
    private String productVariant;
    private String customizations;
    private String notes;
    
    // Constructors
    public OrderItemRequest() {
    }
    
    public OrderItemRequest(String productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
    
    public OrderItemRequest(String productId, String productName, Integer quantity, Double unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
    
    // Business validation methods
    
    /**
     * Validates if the order item request is valid
     */
    public boolean isValid() {
        return productId != null && !productId.trim().isEmpty() &&
               quantity != null && quantity > 0;
    }
    
    /**
     * Calculates total price for this item
     */
    public Double getTotalPrice() {
        return unitPrice != null && quantity != null ? unitPrice * quantity : null;
    }
    
    // Getters and Setters
    
    public String getProductId() {
        return productId;
    }
    
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public Double getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }
    
    public String getProductSku() {
        return productSku;
    }
    
    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }
    
    public String getProductVariant() {
        return productVariant;
    }
    
    public void setProductVariant(String productVariant) {
        this.productVariant = productVariant;
    }
    
    public String getCustomizations() {
        return customizations;
    }
    
    public void setCustomizations(String customizations) {
        this.customizations = customizations;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    @Override
    public String toString() {
        return String.format("OrderItemRequest{productId='%s', productName='%s', quantity=%d, unitPrice=%.2f}", 
                           productId, productName, quantity, unitPrice);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        OrderItemRequest that = (OrderItemRequest) obj;
        return productId != null ? productId.equals(that.productId) : that.productId == null;
    }
    
    @Override
    public int hashCode() {
        return productId != null ? productId.hashCode() : 0;
    }
}
