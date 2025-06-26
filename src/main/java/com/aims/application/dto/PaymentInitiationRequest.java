package com.aims.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Payment Initiation Request DTO
 * 
 * Application layer DTO for payment initiation requests.
 * Used to orchestrate payment flows in the application service.
 */
public class PaymentInitiationRequest {

    @NotBlank(message = "Order ID is required")
    private String orderId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Long amount;

    private String orderInfo;

    private String bankCode;

    private String language = "vn";

    private String returnUrl;

    private String customerId;

    // Constructors
    public PaymentInitiationRequest() {}

    public PaymentInitiationRequest(String orderId, Long amount, String orderInfo) {
        this.orderId = orderId;
        this.amount = amount;
        this.orderInfo = orderInfo;
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getOrderInfo() {
        return orderInfo;
    }

    public void setOrderInfo(String orderInfo) {
        this.orderInfo = orderInfo;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    @Override
    public String toString() {
        return "PaymentInitiationRequest{" +
                "orderId='" + orderId + '\'' +
                ", amount=" + amount +
                ", orderInfo='" + orderInfo + '\'' +
                ", bankCode='" + bankCode + '\'' +
                ", language='" + language + '\'' +
                ", customerId='" + customerId + '\'' +
                '}';
    }
}
