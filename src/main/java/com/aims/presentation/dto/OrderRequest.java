package com.aims.presentation.dto;

/**
 * Request DTO for Order operations
 */
public class OrderRequest {
    
    private Long customerId;
    private String deliveryAddress;
    private String deliveryInstructions;
    private String paymentMethod;
    private boolean isRushOrder;
    private String rushOrderInstructions;
    
    // Constructors
    public OrderRequest() {
        // Default constructor for JSON deserialization
    }
    
    // Getters and Setters
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    
    public String getDeliveryInstructions() { return deliveryInstructions; }
    public void setDeliveryInstructions(String deliveryInstructions) { this.deliveryInstructions = deliveryInstructions; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public boolean isRushOrder() { return isRushOrder; }
    public void setRushOrder(boolean rushOrder) { isRushOrder = rushOrder; }
    
    public String getRushOrderInstructions() { return rushOrderInstructions; }
    public void setRushOrderInstructions(String rushOrderInstructions) { this.rushOrderInstructions = rushOrderInstructions; }
}
