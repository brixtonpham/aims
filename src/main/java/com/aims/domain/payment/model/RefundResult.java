package com.aims.domain.payment.model;

/**
 * Domain model for refund results
 */
public class RefundResult {
    private final boolean success;
    private final String message;
    private final Long amount;
    private final String method;
    private final String transactionId;
    private final String refundId;
    
    private RefundResult(Builder builder) {
        this.success = builder.success;
        this.message = builder.message;
        this.amount = builder.amount;
        this.method = builder.method;
        this.transactionId = builder.transactionId;
        this.refundId = builder.refundId;
    }
    
    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Long getAmount() { return amount; }
    public String getMethod() { return method; }
    public String getTransactionId() { return transactionId; }
    public String getRefundId() { return refundId; }
    
    public static RefundResult success(Long amount, String transactionId, String refundId) {
        return builder()
            .success(true)
            .amount(amount)
            .transactionId(transactionId)
            .refundId(refundId)
            .method("VNPAY")
            .message("Refund processed successfully")
            .build();
    }
    
    public static RefundResult failure(String message) {
        return builder()
            .success(false)
            .message(message)
            .build();
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private boolean success;
        private String message;
        private Long amount;
        private String method;
        private String transactionId;
        private String refundId;
        
        public Builder success(boolean success) {
            this.success = success;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder amount(Long amount) {
            this.amount = amount;
            return this;
        }
        
        public Builder method(String method) {
            this.method = method;
            return this;
        }
        
        public Builder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }
        
        public Builder refundId(String refundId) {
            this.refundId = refundId;
            return this;
        }
        
        public RefundResult build() {
            return new RefundResult(this);
        }
    }
}