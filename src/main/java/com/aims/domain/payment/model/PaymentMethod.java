package com.aims.domain.payment.model;

/**
 * Domain enum for payment methods
 */
public enum PaymentMethod {
    VNPAY("VNPay Electronic Payment"),
    COD("Cash on Delivery"),
    BANK_TRANSFER("Bank Transfer"),
    CREDIT_CARD("Credit Card");
    
    private final String description;
    
    PaymentMethod(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}