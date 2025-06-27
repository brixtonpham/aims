
// src/main/java/com/aims/domain/payment/model/DomainPaymentRequest.java
package com.aims.domain.payment.model;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Domain model for payment requests
 * Clean Architecture - Domain Layer
 */
public class DomainPaymentRequest {
    private final String orderId;
    private final Long amount; // Amount in VND
    private final String currency;
    private final String bankCode;
    private final String language;
    private final String customerId;
    private final String orderDescription;
    private final HttpServletRequest httpRequest; // For IP and session info
    
    private DomainPaymentRequest(Builder builder) {
        this.orderId = builder.orderId;
        this.amount = builder.amount;
        this.currency = builder.currency;
        this.bankCode = builder.bankCode;
        this.language = builder.language;
        this.customerId = builder.customerId;
        this.orderDescription = builder.orderDescription;
        this.httpRequest = builder.httpRequest;
    }
    
    // Getters
    public String getOrderId() { return orderId; }
    public Long getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getBankCode() { return bankCode; }
    public String getLanguage() { return language; }
    public String getCustomerId() { return customerId; }
    public String getOrderDescription() { return orderDescription; }
    public HttpServletRequest getHttpRequest() { return httpRequest; }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String orderId;
        private Long amount;
        private String currency = "VND";
        private String bankCode;
        private String language = "vn";
        private String customerId;
        private String orderDescription;
        private HttpServletRequest httpRequest;
        
        public Builder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }
        
        public Builder amount(Long amount) {
            this.amount = amount;
            return this;
        }
        
        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }
        
        public Builder bankCode(String bankCode) {
            this.bankCode = bankCode;
            return this;
        }
        
        public Builder language(String language) {
            this.language = language;
            return this;
        }
        
        public Builder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }
        
        public Builder orderDescription(String orderDescription) {
            this.orderDescription = orderDescription;
            return this;
        }
        
        public Builder httpRequest(HttpServletRequest httpRequest) {
            this.httpRequest = httpRequest;
            return this;
        }
        
        public DomainPaymentRequest build() {
            return new DomainPaymentRequest(this);
        }
    }
}
