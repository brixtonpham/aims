package com.aims.presentation.dto.payment;

import java.time.LocalDateTime;

/**
 * Refund Response DTO
 * 
 * Data transfer object for payment refund responses.
 * Contains refund processing status and details.
 */
public class RefundResponse {

    private String responseCode;
    private String message;
    private String transactionId;
    private String vnpayTransactionNo;
    private Long amount;
    private String orderInfo;
    private String bankCode;
    private String transactionStatus;
    private String transactionType;
    private boolean success;
    private LocalDateTime processTime;

    // Constructors
    public RefundResponse() {
        this.processTime = LocalDateTime.now();
    }

    public RefundResponse(String responseCode, String message) {
        this();
        this.responseCode = responseCode;
        this.message = message;
        this.success = "00".equals(responseCode);
    }

    // Static factory methods
    public static RefundResponse success(String transactionId, String vnpayTransactionNo, Long amount) {
        RefundResponse response = new RefundResponse();
        response.responseCode = "00";
        response.message = "Refund processed successfully";
        response.transactionId = transactionId;
        response.vnpayTransactionNo = vnpayTransactionNo;
        response.amount = amount;
        response.success = true;
        return response;
    }

    public static RefundResponse failure(String responseCode, String message) {
        RefundResponse response = new RefundResponse();
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

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public LocalDateTime getProcessTime() {
        return processTime;
    }

    public void setProcessTime(LocalDateTime processTime) {
        this.processTime = processTime;
    }

    @Override
    public String toString() {
        return "RefundResponse{" +
                "responseCode='" + responseCode + '\'' +
                ", message='" + message + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", vnpayTransactionNo='" + vnpayTransactionNo + '\'' +
                ", amount=" + amount +
                ", success=" + success +
                '}';
    }
}
