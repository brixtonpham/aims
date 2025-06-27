package com.aims.domain.payment.model;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * Domain model for refund requests
 */
public class DomainRefundRequest {
    private final String orderId;
    private final String transactionId;
    private final Long amount;
    private final String reason;
    private final String requestedBy;
    private final LocalDateTime transactionDate;
    private final HttpServletRequest httpRequest;
    
    private DomainRefundRequest(Builder builder) {
        this.orderId = builder.orderId;
        this.transactionId = builder.transactionId;
        this.amount = builder.amount;
        this.reason = builder.reason;
        this.requestedBy = builder.requestedBy;
        this.transactionDate = builder.transactionDate;
        this.httpRequest = builder.httpRequest;
    }
    
    // Getters
    public String getOrderId() { return orderId; }
    public String getTransactionId() { return transactionId; }
    public Long getAmount() { return amount; }
    public String getReason() { return reason; }
    public String getRequestedBy() { return requestedBy; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public HttpServletRequest getHttpRequest() { return httpRequest; }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String orderId;
        private String transactionId;
        private Long amount;
        private String reason;
        private String requestedBy;
        private LocalDateTime transactionDate;
        private HttpServletRequest httpRequest;
        
        public Builder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }
        
        public Builder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }
        
        public Builder amount(Long amount) {
            this.amount = amount;
            return this;
        }
        
        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }
        
        public Builder requestedBy(String requestedBy) {
            this.requestedBy = requestedBy;
            return this;
        }
        
        public Builder transactionDate(LocalDateTime transactionDate) {
            this.transactionDate = transactionDate;
            return this;
        }
        
        public Builder httpRequest(HttpServletRequest httpRequest) {
            this.httpRequest = httpRequest;
            return this;
        }
        
        public DomainRefundRequest build() {
            return new DomainRefundRequest(this);
        }
    }
}
