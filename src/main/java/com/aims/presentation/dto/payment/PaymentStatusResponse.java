package com.aims.presentation.dto.payment;

import java.time.LocalDateTime;

/**
 * Payment Status Response DTO
 * 
 * Data transfer object for payment status query responses.
 * Contains transaction details and current payment status.
 */
public class PaymentStatusResponse {

    private String responseCode;
    private String message;
    private String transactionId;
    private String vnpayTransactionNo;
    private Long amount;
    private String bankCode;
    private String transactionStatus;
    private String payDate;
    private boolean success;
    private LocalDateTime queryTime;

    // Constructors
    public PaymentStatusResponse() {
        this.queryTime = LocalDateTime.now();
    }

    public PaymentStatusResponse(String responseCode, String message) {
        this();
        this.responseCode = responseCode;
        this.message = message;
        this.success = "00".equals(responseCode);
    }

    // Static factory methods
    public static PaymentStatusResponse success(String transactionId, String vnpayTransactionNo, 
                                              Long amount, String transactionStatus) {
        PaymentStatusResponse response = new PaymentStatusResponse();
        response.responseCode = "00";
        response.message = "Success";
        response.transactionId = transactionId;
        response.vnpayTransactionNo = vnpayTransactionNo;
        response.amount = amount;
        response.transactionStatus = transactionStatus;
        response.success = true;
        return response;
    }

    public static PaymentStatusResponse failure(String responseCode, String message) {
        PaymentStatusResponse response = new PaymentStatusResponse();
        response.responseCode = responseCode;
        response.message = message;
        response.success = false;
        return response;
    }

    // Getters and Setters
    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
        this.success = "00".equals(responseCode);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getVnpayTransactionNo() {
        return vnpayTransactionNo;
    }

    public void setVnpayTransactionNo(String vnpayTransactionNo) {
        this.vnpayTransactionNo = vnpayTransactionNo;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public String getPayDate() {
        return payDate;
    }

    public void setPayDate(String payDate) {
        this.payDate = payDate;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public LocalDateTime getQueryTime() {
        return queryTime;
    }

    public void setQueryTime(LocalDateTime queryTime) {
        this.queryTime = queryTime;
    }

    @Override
    public String toString() {
        return "PaymentStatusResponse{" +
                "responseCode='" + responseCode + '\'' +
                ", message='" + message + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", vnpayTransactionNo='" + vnpayTransactionNo + '\'' +
                ", amount=" + amount +
                ", transactionStatus='" + transactionStatus + '\'' +
                ", success=" + success +
                '}';
    }
}
