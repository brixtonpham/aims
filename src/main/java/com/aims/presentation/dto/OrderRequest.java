package com.aims.presentation.dto;

import java.util.List;

/**
 * Request DTO for Order operations
 * Enhanced for Phase 4 with complete customer and order item information
 */
public class OrderRequest {
    
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String deliveryAddress;
    private String deliveryCity;
    private String deliveryState;
    private String deliveryPostalCode;
    private String deliveryCountry;
    private String deliveryInstructions;
    private String paymentMethod;
    private boolean isRushOrder;
    private String rushOrderInstructions;
    private List<OrderItemRequest> orderItems;
    
    // Constructors
    public OrderRequest() {
        // Default constructor for JSON deserialization
    }
    
    // Getters and Setters
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    
    public String getDeliveryCity() { return deliveryCity; }
    public void setDeliveryCity(String deliveryCity) { this.deliveryCity = deliveryCity; }
    
    public String getDeliveryState() { return deliveryState; }
    public void setDeliveryState(String deliveryState) { this.deliveryState = deliveryState; }
    
    public String getDeliveryPostalCode() { return deliveryPostalCode; }
    public void setDeliveryPostalCode(String deliveryPostalCode) { this.deliveryPostalCode = deliveryPostalCode; }
    
    public String getDeliveryCountry() { return deliveryCountry; }
    public void setDeliveryCountry(String deliveryCountry) { this.deliveryCountry = deliveryCountry; }
    
    public String getDeliveryInstructions() { return deliveryInstructions; }
    public void setDeliveryInstructions(String deliveryInstructions) { this.deliveryInstructions = deliveryInstructions; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public boolean isRushOrder() { return isRushOrder; }
    public void setRushOrder(boolean rushOrder) { isRushOrder = rushOrder; }
    
    public String getRushOrderInstructions() { return rushOrderInstructions; }
    public void setRushOrderInstructions(String rushOrderInstructions) { this.rushOrderInstructions = rushOrderInstructions; }
    
    public List<OrderItemRequest> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItemRequest> orderItems) { this.orderItems = orderItems; }
}
