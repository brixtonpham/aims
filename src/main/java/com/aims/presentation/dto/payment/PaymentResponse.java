package com.aims.presentation.dto.payment;

/**
 * Payment Response DTO
 * 
 * Data transfer object for payment initiation responses.
 * Contains payment URL and transaction status.
 */
public class PaymentResponse {

    private String code;
    private String message;
    private String paymentUrl;
    private String transactionId;
    private String ipAddress;
    private boolean success;

    // Constructors
    public PaymentResponse() {}

    public PaymentResponse(String code, String message, String paymentUrl) {
        this.code = code;
        this.message = message;
        this.paymentUrl = paymentUrl;
        this.success = "00".equals(code);
    }

    // Static factory methods
    public static PaymentResponse success(String paymentUrl, String transactionId) {
        PaymentResponse response = new PaymentResponse();
        response.code = "00";
        response.message = "Success";
        response.paymentUrl = paymentUrl;
        response.transactionId = transactionId;
        response.success = true;
        return response;
    }

    public static PaymentResponse failure(String code, String message) {
        PaymentResponse response = new PaymentResponse();
        response.code = code;
        response.message = message;
        response.success = false;
        return response;
    }

    // Getters and Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
        this.success = "00".equals(code);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "PaymentResponse{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", paymentUrl='" + paymentUrl + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", success=" + success +
                '}';
    }
}
