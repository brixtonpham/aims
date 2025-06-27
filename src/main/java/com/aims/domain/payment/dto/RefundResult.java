package com.aims.domain.payment.dto;

import com.aims.domain.payment.entity.PaymentStatus;
import java.time.LocalDateTime;

/**
 * Result DTO for refund processing operations.
 * 
 * Contains the outcome of a refund processing request:
 * - Refund transaction details
 * - Processing status and information
 * - Financial breakdown
 * - Error information if refund failed
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 1 - Foundation
 */
public class RefundResult {
    
    // Success/Failure Information
    private boolean success;
    private String code;
    private String message;
    
    // Refund Transaction Details
    private String refundTransactionId;
    private String originalTransactionId;
    private String orderId;
    private PaymentStatus status;
    
    // Financial Information
    private Double refundAmount;
    private Double originalAmount;
    private Double remainingRefundableAmount;
    private String currency;
    
    // Processing Information
    private String refundMethod;
    private String refundProvider;
    private LocalDateTime processedAt;
    private LocalDateTime estimatedCompletionDate;
    
    // Business Information
    private String refundReason;
    private String refundType;
    private String processedBy;
    
    // Error Information
    private String errorCode;
    private String errorMessage;
    private String errorDetails;
    
    // Constructors
    public RefundResult() {
        this.processedAt = LocalDateTime.now();
    }
    
    public RefundResult(boolean success, String code, String message) {
        this();
        this.success = success;
        this.code = code;
        this.message = message;
    }
    
    // Factory methods for common scenarios
    
    public static RefundResult success(String refundTransactionId, Double refundAmount) {
        RefundResult result = new RefundResult(true, "00", "Refund processed successfully");
        result.setRefundTransactionId(refundTransactionId);
        result.setRefundAmount(refundAmount);
        result.setStatus(PaymentStatus.REFUNDED);
        return result;
    }
    
    public static RefundResult failure(String errorCode, String errorMessage) {
        RefundResult result = new RefundResult(false, errorCode, errorMessage);
        result.setErrorCode(errorCode);
        result.setErrorMessage(errorMessage);
        result.setStatus(PaymentStatus.FAILED);
        return result;
    }
    
    // Business methods
    
    /**
     * Checks if refund was successful
     */
    public boolean isSuccessful() {
        return success && "00".equals(code);
    }
    
    /**
     * Checks if this was a full refund
     */
    public boolean isFullRefund() {
        return refundAmount != null && originalAmount != null && 
               refundAmount.equals(originalAmount);
    }
    
    /**
     * Checks if this was a partial refund
     */
    public boolean isPartialRefund() {
        return !isFullRefund() && remainingRefundableAmount != null && remainingRefundableAmount > 0;
    }
    
    /**
     * Gets refund percentage of original amount
     */
    public Double getRefundPercentage() {
        return refundAmount != null && originalAmount != null && originalAmount > 0 ? 
               (refundAmount / originalAmount) * 100 : 0.0;
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
    
    public String getRefundTransactionId() {
        return refundTransactionId;
    }
    
    public void setRefundTransactionId(String refundTransactionId) {
        this.refundTransactionId = refundTransactionId;
    }
    
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
    
    public PaymentStatus getStatus() {
        return status;
    }
    
    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
    
    public Double getRefundAmount() {
        return refundAmount;
    }
    
    public void setRefundAmount(Double refundAmount) {
        this.refundAmount = refundAmount;
    }
    
    public Double getOriginalAmount() {
        return originalAmount;
    }
    
    public void setOriginalAmount(Double originalAmount) {
        this.originalAmount = originalAmount;
    }
    
    public Double getRemainingRefundableAmount() {
        return remainingRefundableAmount;
    }
    
    public void setRemainingRefundableAmount(Double remainingRefundableAmount) {
        this.remainingRefundableAmount = remainingRefundableAmount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getRefundMethod() {
        return refundMethod;
    }
    
    public void setRefundMethod(String refundMethod) {
        this.refundMethod = refundMethod;
    }
    
    public String getRefundProvider() {
        return refundProvider;
    }
    
    public void setRefundProvider(String refundProvider) {
        this.refundProvider = refundProvider;
    }
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
    
    public LocalDateTime getEstimatedCompletionDate() {
        return estimatedCompletionDate;
    }
    
    public void setEstimatedCompletionDate(LocalDateTime estimatedCompletionDate) {
        this.estimatedCompletionDate = estimatedCompletionDate;
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
    
    public String getProcessedBy() {
        return processedBy;
    }
    
    public void setProcessedBy(String processedBy) {
        this.processedBy = processedBy;
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
        return String.format("RefundResult{success=%s, code='%s', refundTransactionId='%s', refundAmount=%.2f, status=%s}", 
                           success, code, refundTransactionId, refundAmount, status);
    }
}
