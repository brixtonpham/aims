package com.aims.domain.payment.dto;

import com.aims.domain.payment.entity.PaymentStatus;
import java.time.LocalDateTime;

/**
 * Result DTO for payment processing operations.
 * 
 * Contains the outcome of a payment processing request:
 * - Transaction details and identifiers
 * - Payment status and processing information
 * - URLs for completing the payment flow
 * - Error information if payment failed
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 1 - Foundation
 */
public class PaymentResult {
    
    // Success/Failure Information
    private boolean success;
    private String code;
    private String message;
    
    // Transaction Details
    private String transactionId;
    private String orderId;
    private PaymentStatus status;
    private Double amount;
    private String currency;
    
    // Payment Provider Information
    private String paymentUrl;
    private String providerTransactionId;
    private String paymentMethod;
    private String bankCode;
    
    // Timing Information
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    
    // Customer Information
    private String customerId;
    private String ipAddress;
    
    // Error Information
    private String errorCode;
    private String errorMessage;
    private String errorDetails;
    
    // Constructors
    public PaymentResult() {
        this.createdAt = LocalDateTime.now();
    }
    
    public PaymentResult(boolean success, String code, String message) {
        this();
        this.success = success;
        this.code = code;
        this.message = message;
    }
    
    // Factory methods for common scenarios
    
    public static PaymentResult success(String transactionId, String paymentUrl) {
        PaymentResult result = new PaymentResult(true, "00", "Payment created successfully");
        result.setTransactionId(transactionId);
        result.setPaymentUrl(paymentUrl);
        result.setStatus(PaymentStatus.PENDING);
        return result;
    }
    
    public static PaymentResult failure(String errorCode, String errorMessage) {
        PaymentResult result = new PaymentResult(false, errorCode, errorMessage);
        result.setErrorCode(errorCode);
        result.setErrorMessage(errorMessage);
        result.setStatus(PaymentStatus.FAILED);
        return result;
    }
    
    // Business methods
    
    /**
     * Checks if payment was successful
     */
    public boolean isSuccessful() {
        return success && "00".equals(code);
    }
    
    /**
     * Checks if payment requires customer action (redirect to payment page)
     */
    public boolean requiresCustomerAction() {
        return success && paymentUrl != null && !paymentUrl.trim().isEmpty();
    }
    
    /**
     * Checks if payment is expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    // Getters and Setters
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public PaymentStatus getStatus() {
        return status;
    }
    
    public void setStatus(PaymentStatus status) {
        this.status = status;
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
    
    public String getPaymentUrl() {
        return paymentUrl;
    }
    
    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }
    
    public String getProviderTransactionId() {
        return providerTransactionId;
    }
    
    public void setProviderTransactionId(String providerTransactionId) {
        this.providerTransactionId = providerTransactionId;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getBankCode() {
        return bankCode;
    }
    
    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public String getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getErrorDetails() {
        return errorDetails;
    }
    
    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }
    
    @Override
    public String toString() {
        return String.format("PaymentResult{success=%s, code='%s', transactionId='%s', status=%s, amount=%.2f}", 
                           success, code, transactionId, status, amount);
    }
}
