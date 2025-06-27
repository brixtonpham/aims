package com.aims.domain.payment.dto;

/**
 * Request DTO for processing a payment transaction.
 * 
 * Encapsulates all information needed to process a payment:
 * - Transaction identification and amounts
 * - Customer information
 * - Payment method details
 * - Business context and metadata
 * 
 * This DTO is used in the domain layer and should be independent
 * of presentation layer concerns.
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 1 - Foundation
 */
public class PaymentRequest {
    
    // Transaction Information
    private String orderId;
    private String transactionId;
    private Double amount;
    private String currency;
    private String description;
    
    // Customer Information
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    
    // Payment Method Information
    private String paymentMethod;
    private String paymentProvider;
    private String bankCode;
    private String returnUrl;
    private String cancelUrl;
    private String ipAddress;
    
    // Business Context
    private String orderInfo;
    private String language;
    private Boolean isRushPayment;
    private String merchantReference;
    
    // Constructors
    public PaymentRequest() {
        this.currency = "VND";
        this.language = "vn";
        this.isRushPayment = false;
    }
    
    public PaymentRequest(String orderId, Double amount, String paymentMethod) {
        this();
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }
    
    // Business validation methods
    
    /**
     * Validates if the payment request has all required information
     */
    public boolean isValid() {
        return orderId != null && !orderId.trim().isEmpty() &&
               amount != null && amount > 0 &&
               currency != null && !currency.trim().isEmpty() &&
               paymentMethod != null && !paymentMethod.trim().isEmpty() &&
               customerId != null && !customerId.trim().isEmpty();
    }
    
    /**
     * Gets amount in cents/smallest currency unit
     */
    public Long getAmountInCents() {
        return amount != null ? Math.round(amount * 100) : null;
    }
    
    /**
     * Checks if this is a high-value transaction
     */
    public boolean isHighValueTransaction() {
        return amount != null && amount >= 10000000; // 10M VND
    }
    
    // Getters and Setters
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public Double getAmount() {
        return amount;
    }
    
    public void setAmount(Double amount) {
        this.amount = amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
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
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getPaymentProvider() {
        return paymentProvider;
    }
    
    public void setPaymentProvider(String paymentProvider) {
        this.paymentProvider = paymentProvider;
    }
    
    public String getBankCode() {
        return bankCode;
    }
    
    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }
    
    public String getReturnUrl() {
        return returnUrl;
    }
    
    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }
    
    public String getCancelUrl() {
        return cancelUrl;
    }
    
    public void setCancelUrl(String cancelUrl) {
        this.cancelUrl = cancelUrl;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getOrderInfo() {
        return orderInfo;
    }
    
    public void setOrderInfo(String orderInfo) {
        this.orderInfo = orderInfo;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public Boolean getIsRushPayment() {
        return isRushPayment;
    }
    
    public void setIsRushPayment(Boolean isRushPayment) {
        this.isRushPayment = isRushPayment;
    }
    
    public String getMerchantReference() {
        return merchantReference;
    }
    
    public void setMerchantReference(String merchantReference) {
        this.merchantReference = merchantReference;
    }
    
    @Override
    public String toString() {
        return String.format("PaymentRequest{orderId='%s', amount=%.2f %s, paymentMethod='%s', customerId='%s'}", 
                           orderId, amount, currency, paymentMethod, customerId);
    }
}
