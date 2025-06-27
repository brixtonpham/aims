/**
 * DTO for payment result display
 * Contains all information needed to display payment results to users
 */
package com.aims.vnpay.common.dto;

/**
 * Payment Result Data Transfer Object
 * Used to pass payment result information to the view layer
 */
public class PaymentResultDto {
    
    private String status;          // SUCCESS, FAILED, PENDING, INVALID, ERROR
    private String message;         // User-friendly message
    private String transactionId;   // Order ID
    private Long amount;           // Amount in VND cents
    private String vnpayTransactionId;  // VNPay transaction ID
    private String bankCode;       // Bank code
    private String payDate;        // Formatted payment date
    private String orderInfo;      // Order information
    private String responseCode;   // VNPay response code
    private boolean validHash;     // Hash validation result
    
    // Constructors
    public PaymentResultDto() {}
    
    public PaymentResultDto(String status, String message) {
        this.status = status;
        this.message = message;
    }
    
    // Getters and Setters
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
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
    
    public Long getAmount() {
        return amount;
    }
    
    public void setAmount(Long amount) {
        this.amount = amount;
    }
    
    public String getVnpayTransactionId() {
        return vnpayTransactionId;
    }
    
    public void setVnpayTransactionId(String vnpayTransactionId) {
        this.vnpayTransactionId = vnpayTransactionId;
    }
    
    public String getBankCode() {
        return bankCode;
    }
    
    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }
    
    public String getPayDate() {
        return payDate;
    }
    
    public void setPayDate(String payDate) {
        this.payDate = payDate;
    }
    
    public String getOrderInfo() {
        return orderInfo;
    }
    
    public void setOrderInfo(String orderInfo) {
        this.orderInfo = orderInfo;
    }
    
    public String getResponseCode() {
        return responseCode;
    }
    
    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }
    
    public boolean isValidHash() {
        return validHash;
    }
    
    public void setValidHash(boolean validHash) {
        this.validHash = validHash;
    }
    
    /**
     * Builder pattern for easy construction
     */
    public static class Builder {
        private PaymentResultDto dto = new PaymentResultDto();
        
        public Builder status(String status) {
            dto.setStatus(status);
            return this;
        }
        
        public Builder message(String message) {
            dto.setMessage(message);
            return this;
        }
        
        public Builder transactionId(String transactionId) {
            dto.setTransactionId(transactionId);
            return this;
        }
        
        public Builder amount(Long amount) {
            dto.setAmount(amount);
            return this;
        }
        
        public Builder vnpayTransactionId(String vnpayTransactionId) {
            dto.setVnpayTransactionId(vnpayTransactionId);
            return this;
        }
        
        public Builder bankCode(String bankCode) {
            dto.setBankCode(bankCode);
            return this;
        }
        
        public Builder payDate(String payDate) {
            dto.setPayDate(payDate);
            return this;
        }
        
        public Builder orderInfo(String orderInfo) {
            dto.setOrderInfo(orderInfo);
            return this;
        }
        
        public Builder responseCode(String responseCode) {
            dto.setResponseCode(responseCode);
            return this;
        }
        
        public Builder validHash(boolean validHash) {
            dto.setValidHash(validHash);
            return this;
        }
        
        public PaymentResultDto build() {
            return dto;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @Override
    public String toString() {
        return "PaymentResultDto{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", amount=" + amount +
                ", vnpayTransactionId='" + vnpayTransactionId + '\'' +
                ", bankCode='" + bankCode + '\'' +
                ", payDate='" + payDate + '\'' +
                ", orderInfo='" + orderInfo + '\'' +
                ", responseCode='" + responseCode + '\'' +
                ", validHash=" + validHash +
                '}';
    }
}
