package com.aims.domain.payment.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * PaymentTransaction entity consolidating multiple TransactionInfo entities
 * Follows Clean Architecture principles with proper business methods
 */
@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @NotBlank(message = "Order ID is required")
    @Column(name = "order_id", nullable = false)
    private String orderId;
    
    @Column(name = "transaction_no")
    private String transactionNo;
    
    @Positive(message = "Amount must be positive")
    @Column(name = "amount", nullable = false)
    private Long amount;
    
    @Column(name = "bank_code")
    private String bankCode;
    
    @Column(name = "response_code")
    private String responseCode;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_status")
    private TransactionStatus transactionStatus;
    
    @Column(name = "pay_date")
    private String payDate;
    
    @Column(name = "payment_method")
    private String paymentMethod;
    
    @Column(name = "gateway_transaction_id")
    private String gatewayTransactionId;
    
    @Column(name = "gateway_response_message")
    private String gatewayResponseMessage;
    
    @Column(name = "currency_code")
    @Builder.Default
    private String currencyCode = "VND";
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (transactionStatus == null) {
            transactionStatus = TransactionStatus.PENDING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Business methods
    
    /**
     * Check if transaction was successful
     */
    public boolean isSuccessful() {
        return TransactionStatus.SUCCESS.equals(transactionStatus) && 
               "00".equals(responseCode);
    }
    
    /**
     * Check if transaction failed
     */
    public boolean isFailed() {
        return TransactionStatus.FAILED.equals(transactionStatus);
    }
    
    /**
     * Check if transaction is still pending
     */
    public boolean isPending() {
        return TransactionStatus.PENDING.equals(transactionStatus);
    }
    
    /**
     * Check if transaction can be refunded
     * Business rule: Can refund successful transactions within 30 days
     */
    public boolean canBeRefunded() {
        if (!isSuccessful()) {
            return false;
        }
        
        if (createdAt == null) {
            return false;
        }
        
        // Can refund within 30 days
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        return createdAt.isAfter(cutoffDate);
    }
    
    /**
     * Mark transaction as successful
     */
    public void markAsSuccessful(String transactionNo, String payDate) {
        this.transactionStatus = TransactionStatus.SUCCESS;
        this.transactionNo = transactionNo;
        this.payDate = payDate;
        this.responseCode = "00";
    }
    
    /**
     * Mark transaction as failed
     */
    public void markAsFailed(String responseCode, String errorMessage) {
        this.transactionStatus = TransactionStatus.FAILED;
        this.responseCode = responseCode;
        this.gatewayResponseMessage = errorMessage;
    }
    
    /**
     * Mark transaction as refunded
     */
    public void markAsRefunded() {
        if (canBeRefunded()) {
            this.transactionStatus = TransactionStatus.REFUNDED;
        } else {
            throw new IllegalStateException("Transaction cannot be refunded");
        }
    }
    
    /**
     * Get formatted amount for display
     */
    public String getFormattedAmount() {
        if (amount == null) {
            return "0 VND";
        }
        return String.format("%,d %s", amount, currencyCode);
    }
    
    /**
     * Check if transaction is from VNPay gateway
     */
    public boolean isVNPayTransaction() {
        return transactionNo != null && transactionNo.startsWith("VNP");
    }
    
    /**
     * Business validation
     */
    public boolean isValid() {
        return orderId != null && !orderId.trim().isEmpty() &&
               amount != null && amount > 0 &&
               transactionStatus != null;
    }
    
    /**
     * Transaction status enumeration
     */
    public enum TransactionStatus {
        PENDING("Pending"),
        SUCCESS("Success"),
        FAILED("Failed"),
        REFUNDED("Refunded"),
        CANCELLED("Cancelled");
        
        private final String displayName;
        
        TransactionStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
