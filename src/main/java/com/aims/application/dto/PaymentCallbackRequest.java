package com.aims.application.dto;

import java.util.Map;

/**
 * Payment Callback Request DTO
 * 
 * Application layer DTO for payment callback/notification requests.
 * Used to process IPN (Instant Payment Notification) from payment providers.
 */
public class PaymentCallbackRequest {

    private String transactionId;
    private String responseCode;
    private String message;
    private Map<String, String> params;
    private String provider;
    private String ipAddress;

    // Constructors
    public PaymentCallbackRequest() {}

    public PaymentCallbackRequest(String transactionId, Map<String, String> params) {
        this.transactionId = transactionId;
        this.params = params;
        this.responseCode = params.get("vnp_ResponseCode");
        this.message = params.get("vnp_Message");
        this.provider = "VNPay"; // Default provider
    }

    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
        if (params != null) {
            this.responseCode = params.get("vnp_ResponseCode");
            this.message = params.get("vnp_Message");
            this.transactionId = params.get("vnp_TxnRef");
        }
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public boolean isSuccessful() {
        return "00".equals(responseCode);
    }

    @Override
    public String toString() {
        return "PaymentCallbackRequest{" +
                "transactionId='" + transactionId + '\'' +
                ", responseCode='" + responseCode + '\'' +
                ", message='" + message + '\'' +
                ", provider='" + provider + '\'' +
                ", successful=" + isSuccessful() +
                '}';
    }
}
