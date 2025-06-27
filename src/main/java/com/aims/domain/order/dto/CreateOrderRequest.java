package com.aims.domain.order.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Request DTO for creating a new order.
 * 
 * Encapsulates all information needed to create an order:
 * - Customer information
 * - Order items with quantities
 * - Delivery information
 * - Payment preferences
 * - Special requirements
 * 
 * This DTO is used in the domain layer and should be independent
 * of presentation layer concerns.
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 1 - Foundation
 */
public class CreateOrderRequest {
    
    // Customer Information
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    
    // Order Items
    private List<OrderItemRequest> orderItems;
    
    // Delivery Information
    private String deliveryAddress;
    private String deliveryCity;
    private String deliveryPostalCode;
    private String deliveryCountry;
    private LocalDateTime requestedDeliveryDate;
    private Boolean isRushOrder;
    private String deliveryInstructions;
    
    // Payment Information
    private String paymentMethod;
    private String paymentReference;
    
    // Additional Information
    private String orderSource;
    private String notes;
    private String promotionCode;
    
    // Constructors
    public CreateOrderRequest() {
        this.isRushOrder = false;
        this.deliveryCountry = "Vietnam";
    }
    
    public CreateOrderRequest(String customerId, String customerName, String customerEmail) {
        this();
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
    }
    
    // Business validation methods
    
    /**
     * Validates if the request has all required information
     */
    public boolean isValid() {
        return customerId != null && !customerId.trim().isEmpty() &&
               customerName != null && !customerName.trim().isEmpty() &&
               customerEmail != null && !customerEmail.trim().isEmpty() &&
               deliveryAddress != null && !deliveryAddress.trim().isEmpty() &&
               orderItems != null && !orderItems.isEmpty() &&
               orderItems.stream().allMatch(OrderItemRequest::isValid);
    }
    
    /**
     * Gets total quantity of all items
     */
    public int getTotalQuantity() {
        return orderItems != null ? 
            orderItems.stream().mapToInt(OrderItemRequest::getQuantity).sum() : 0;
    }
    
    /**
     * Checks if order qualifies for rush delivery
     */
    public boolean qualifiesForRushDelivery() {
        return Boolean.TRUE.equals(isRushOrder) && 
               requestedDeliveryDate != null &&
               requestedDeliveryDate.isBefore(LocalDateTime.now().plusDays(1));
    }
    
    // Getters and Setters
    
    public String getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public String getCustomerEmail() {
        return customerEmail;
    }
    
    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }
    
    public String getCustomerPhone() {
        return customerPhone;
    }
    
    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }
    
    public List<OrderItemRequest> getOrderItems() {
        return orderItems;
    }
    
    public void setOrderItems(List<OrderItemRequest> orderItems) {
        this.orderItems = orderItems;
    }
    
    public String getDeliveryAddress() {
        return deliveryAddress;
    }
    
    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }
    
    public String getDeliveryCity() {
        return deliveryCity;
    }
    
    public void setDeliveryCity(String deliveryCity) {
        this.deliveryCity = deliveryCity;
    }
    
    public String getDeliveryPostalCode() {
        return deliveryPostalCode;
    }
    
    public void setDeliveryPostalCode(String deliveryPostalCode) {
        this.deliveryPostalCode = deliveryPostalCode;
    }
    
    public String getDeliveryCountry() {
        return deliveryCountry;
    }
    
    public void setDeliveryCountry(String deliveryCountry) {
        this.deliveryCountry = deliveryCountry;
    }
    
    public LocalDateTime getRequestedDeliveryDate() {
        return requestedDeliveryDate;
    }
    
    public void setRequestedDeliveryDate(LocalDateTime requestedDeliveryDate) {
        this.requestedDeliveryDate = requestedDeliveryDate;
    }
    
    public Boolean getIsRushOrder() {
        return isRushOrder;
    }
    
    public void setIsRushOrder(Boolean isRushOrder) {
        this.isRushOrder = isRushOrder;
    }
    
    public String getDeliveryInstructions() {
        return deliveryInstructions;
    }
    
    public void setDeliveryInstructions(String deliveryInstructions) {
        this.deliveryInstructions = deliveryInstructions;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getPaymentReference() {
        return paymentReference;
    }
    
    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }
    
    public String getOrderSource() {
        return orderSource;
    }
    
    public void setOrderSource(String orderSource) {
        this.orderSource = orderSource;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getPromotionCode() {
        return promotionCode;
    }
    
    public void setPromotionCode(String promotionCode) {
        this.promotionCode = promotionCode;
    }
    
    @Override
    public String toString() {
        return String.format("CreateOrderRequest{customerId='%s', customerName='%s', itemCount=%d, totalQuantity=%d}", 
                           customerId, customerName, 
                           orderItems != null ? orderItems.size() : 0, 
                           getTotalQuantity());
    }
    
    /**
     * Nested DTO for order items
     */
    public static class OrderItemRequest {
        private Long productId;
        private String productTitle;
        private int quantity;
        private int unitPrice;
        
        public OrderItemRequest() {}
        
        public OrderItemRequest(Long productId, String productTitle, int quantity, int unitPrice) {
            this.productId = productId;
            this.productTitle = productTitle;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
        
        public boolean isValid() {
            return productId != null && 
                   productTitle != null && !productTitle.trim().isEmpty() &&
                   quantity > 0 && 
                   unitPrice >= 0;
        }
        
        // Getters and Setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        
        public String getProductTitle() { return productTitle; }
        public void setProductTitle(String productTitle) { this.productTitle = productTitle; }
        
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        
        public int getUnitPrice() { return unitPrice; }
        public void setUnitPrice(int unitPrice) { this.unitPrice = unitPrice; }
        
        @Override
        public String toString() {
            return String.format("OrderItemRequest{productId=%d, title='%s', quantity=%d, unitPrice=%d}", 
                productId, productTitle, quantity, unitPrice);
        }
    }
    
    /**
     * Nested DTO for delivery information
     */
    public static class DeliveryInfoRequest {
        private String name;
        private String phone;
        private String email;
        private String address;
        private String province;
        private String deliveryMessage;
        
        public DeliveryInfoRequest() {}
        
        public DeliveryInfoRequest(String name, String phone, String email, String address, String province) {
            this.name = name;
            this.phone = phone;
            this.email = email;
            this.address = address;
            this.province = province;
        }
        
        public boolean isValid() {
            return name != null && !name.trim().isEmpty() &&
                   phone != null && !phone.trim().isEmpty() &&
                   address != null && !address.trim().isEmpty() &&
                   province != null && !province.trim().isEmpty();
        }
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        
        public String getProvince() { return province; }
        public void setProvince(String province) { this.province = province; }
        
        public String getDeliveryMessage() { return deliveryMessage; }
        public void setDeliveryMessage(String deliveryMessage) { this.deliveryMessage = deliveryMessage; }
        
        @Override
        public String toString() {
            return String.format("DeliveryInfoRequest{name='%s', phone='%s', address='%s', province='%s'}", 
                name, phone, address, province);
        }
    }
    
    // Additional fields needed for order creation
    private DeliveryInfoRequest deliveryInfo;
    
    public DeliveryInfoRequest getDeliveryInfo() {
        return deliveryInfo;
    }
    
    public void setDeliveryInfo(DeliveryInfoRequest deliveryInfo) {
        this.deliveryInfo = deliveryInfo;
    }
    
    public boolean isRushOrder() {
        return Boolean.TRUE.equals(isRushOrder);
    }
}
