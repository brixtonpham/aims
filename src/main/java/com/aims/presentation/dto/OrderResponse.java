package com.aims.presentation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for Order operations
 */
public class OrderResponse {
    
    private Long orderId;
    private Long customerId;
    private String orderStatus;
    private BigDecimal totalAmount;
    private BigDecimal shippingFee;
    private String deliveryAddress;
    private String deliveryInstructions;
    private boolean isRushOrder;
    private String rushOrderInstructions;
    private LocalDateTime orderDate;
    private LocalDateTime expectedDeliveryDate;
    private List<OrderItemResponse> orderItems;
    
    // Constructors
    public OrderResponse() {
        // Default constructor for JSON serialization
    }
    
    // Getters and Setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public BigDecimal getShippingFee() { return shippingFee; }
    public void setShippingFee(BigDecimal shippingFee) { this.shippingFee = shippingFee; }
    
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    
    public String getDeliveryInstructions() { return deliveryInstructions; }
    public void setDeliveryInstructions(String deliveryInstructions) { this.deliveryInstructions = deliveryInstructions; }
    
    public boolean isRushOrder() { return isRushOrder; }
    public void setRushOrder(boolean rushOrder) { isRushOrder = rushOrder; }
    
    public String getRushOrderInstructions() { return rushOrderInstructions; }
    public void setRushOrderInstructions(String rushOrderInstructions) { this.rushOrderInstructions = rushOrderInstructions; }
    
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    
    public LocalDateTime getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public void setExpectedDeliveryDate(LocalDateTime expectedDeliveryDate) { this.expectedDeliveryDate = expectedDeliveryDate; }
    
    public List<OrderItemResponse> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItemResponse> orderItems) { this.orderItems = orderItems; }
}
