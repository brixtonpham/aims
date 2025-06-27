package com.aims.domain.payment.dto;

/**
 * Request DTO for processing a refund transaction.
 * 
 * Encapsulates all information needed to process a refund:
 * - Original transaction reference
 * - Refund amount and reason
 * - Business context and authorization
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 1 - Foundation
 */
public class RefundRequest {
    
    // Original Transaction Information
    private String originalTransactionId;
    private String orderId;
    private Double refundAmount;
    private String currency;
    
    // Refund Information
    private String refundReason;
    private String refundType; // FULL, PARTIAL
    private String refundReference;
    
    // Business Context
    private String initiatedBy;
    private String customerNotification;
    private String notes;
    
    // Authorization
    private String authorizationCode;
    private String approvedBy;
    
    // Constructors
    public RefundRequest() {
        this.currency = "VND";
        this.refundType = "FULL";
    }
    
    public RefundRequest(String originalTransactionId, Double refundAmount, String refundReason) {
        this();
        this.originalTransactionId = originalTransactionId;
        this.refundAmount = refundAmount;
        this.refundReason = refundReason;
    }
    
    // Business validation methods
    
    /**
     * Validates if the refund request has all required information
     */
    public boolean isValid() {
        return originalTransactionId != null && !originalTransactionId.trim().isEmpty() &&
               refundAmount != null && refundAmount > 0 &&
               refundReason != null && !refundReason.trim().isEmpty();
    }
    
    /**
     * Checks if this is a full refund
     */
    public boolean isFullRefund() {
        return "FULL".equals(refundType);
    }
    
    /**
     * Checks if this is a partial refund
     */
    public boolean isPartialRefund() {
        return "PARTIAL".equals(refundType);
    }
    
    // Getters and Setters
    
    public String getOriginalTransactionId() {
        return originalTransactionId;
    }
    
    public void setOriginalTransactionId(String originalTransactionId) {
        this.originalTransactionId = originalTransactionId;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public Double getRefundAmount() {
        return refundAmount;
    }
    
    public void setRefundAmount(Double refundAmount) {
        this.refundAmount = refundAmount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getRefundReason() {
        return refundReason;
    }
    
    public void setRefundReason(String refundReason) {
        this.refundReason = refundReason;
    }
    
    public String getRefundType() {
        return refundType;
    }
    
    public void setRefundType(String refundType) {
        this.refundType = refundType;
    }
    
    public String getRefundReference() {
        return refundReference;
    }
    
    public void setRefundReference(String refundReference) {
        this.refundReference = refundReference;
    }
    
    public String getInitiatedBy() {
        return initiatedBy;
    }
    
    public void setInitiatedBy(String initiatedBy) {
        this.initiatedBy = initiatedBy;
    }
    
    public String getCustomerNotification() {
        return customerNotification;
    }
    
    public void setCustomerNotification(String customerNotification) {
        this.customerNotification = customerNotification;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getAuthorizationCode() {
        return authorizationCode;
    }
    
    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }
    
    public String getApprovedBy() {
        return approvedBy;
    }
    
    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }
    
    @Override
    public String toString() {
        return String.format("RefundRequest{originalTransactionId='%s', refundAmount=%.2f %s, reason='%s', type='%s'}", 
                           originalTransactionId, refundAmount, currency, refundReason, refundType);
    }
}
