package com.aims.domain.payment.model;

/**
 * Domain model for payment results
 */
public class PaymentResult {
    private final boolean success;
    private final String paymentUrl;
    private final String transactionId;
    private final String message;
    private final String errorCode;
    
    private PaymentResult(Builder builder) {
        this.success = builder.success;
        this.paymentUrl = builder.paymentUrl;
        this.transactionId = builder.transactionId;
        this.message = builder.message;
        this.errorCode = builder.errorCode;
    }
    
    // Getters
    public boolean isSuccess() { return success; }
    public String getPaymentUrl() { return paymentUrl; }
    public String getTransactionId() { return transactionId; }
    public String getMessage() { return message; }
    public String getErrorCode() { return errorCode; }
    
    // Static factory methods
    public static PaymentResult success(String paymentUrl, String transactionId) {
        return builder()
            .success(true)
            .paymentUrl(paymentUrl)
            .transactionId(transactionId)
            .message("Payment created successfully")
            .build();
    }
    
    public static PaymentResult failure(String message, String errorCode) {
        return builder()
            .success(false)
            .message(message)
            .errorCode(errorCode)
            .build();
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private boolean success;
        private String paymentUrl;
        private String transactionId;
        private String message;
        private String errorCode;
        
        public Builder success(boolean success) {
            this.success = success;
            return this;
        }
        
        public Builder paymentUrl(String paymentUrl) {
            this.paymentUrl = paymentUrl;
            return this;
        }
        
        public Builder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }
        
        public PaymentResult build() {
            return new PaymentResult(this);
        }
    }
}