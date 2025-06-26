package com.aims.presentation.dto.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Payment Request DTO
 * 
 * Data transfer object for payment initiation requests.
 * Contains all necessary information to create a payment transaction.
 */
public class PaymentRequest {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Long amount;

    @NotBlank(message = "Order ID is required")
    private String orderId;

    private String orderInfo;

    private String bankCode;

    private String language = "vn";

    private String returnUrl;

    private String ipAddress;

    @JsonProperty("vnp_Version")
    private String vnpVersion = "2.1.0";

    @JsonProperty("vnp_ExpireDate")
    private String vnpExpireDate;

    // Constructors
    public PaymentRequest() {}

    public PaymentRequest(Long amount, String orderId, String orderInfo) {
        this.amount = amount;
        this.orderId = orderId;
        this.orderInfo = orderInfo;
    }

    // Getters and Setters
    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getVnpVersion() {
        return vnpVersion;
    }

    public void setVnpVersion(String vnpVersion) {
        this.vnpVersion = vnpVersion;
    }

    public String getVnpExpireDate() {
        return vnpExpireDate;
    }

    public void setVnpExpireDate(String vnpExpireDate) {
        this.vnpExpireDate = vnpExpireDate;
    }

    @Override
    public String toString() {
        return "PaymentRequest{" +
                "amount=" + amount +
                ", orderId='" + orderId + '\'' +
                ", orderInfo='" + orderInfo + '\'' +
                ", bankCode='" + bankCode + '\'' +
                ", language='" + language + '\'' +
                '}';
    }
}
