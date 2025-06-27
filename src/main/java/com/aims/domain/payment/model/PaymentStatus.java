package com.aims.domain.payment.model;

/**
 * Domain enum for payment status
 */
public enum PaymentStatus {
    PENDING("Pending payment"),
    SUCCESS("Payment successful"),
    FAILED("Payment failed"),
    CANCELLED("Payment cancelled"),
    REFUNDED("Payment refunded"),
    PROCESSING("Payment processing");
    
    private final String description;
    
    PaymentStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
