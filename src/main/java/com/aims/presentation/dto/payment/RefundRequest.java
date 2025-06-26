package com.aims.presentation.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Refund Request DTO
 * 
 * Data transfer object for payment refund requests.
 * Contains all necessary information to process a refund.
 */
public class RefundRequest {

    @NotBlank(message = "Order ID is required")
    private String orderId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Long amount;

    @NotBlank(message = "Transaction date is required")
    private String transactionDate;

    @NotBlank(message = "Transaction type is required")
    private String transactionType = "02"; // Default to partial refund

    private String refundReason;

    private String user;

    private String vnpayTransactionNo;

    // Constructors
    public RefundRequest() {}

    public RefundRequest(String orderId, Long amount, String transactionDate) {
        this.orderId = orderId;
        this.amount = amount;
        this.transactionDate = transactionDate;
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

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getRefundReason() {
        return refundReason;
    }

    public void setRefundReason(String refundReason) {
        this.refundReason = refundReason;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getVnpayTransactionNo() {
        return vnpayTransactionNo;
    }

    public void setVnpayTransactionNo(String vnpayTransactionNo) {
        this.vnpayTransactionNo = vnpayTransactionNo;
    }

    @Override
    public String toString() {
        return "RefundRequest{" +
                "orderId='" + orderId + '\'' +
                ", amount=" + amount +
                ", transactionDate='" + transactionDate + '\'' +
                ", transactionType='" + transactionType + '\'' +
                ", refundReason='" + refundReason + '\'' +
                '}';
    }
}
