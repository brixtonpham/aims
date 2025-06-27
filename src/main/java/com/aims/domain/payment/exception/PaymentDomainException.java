package com.aims.domain.payment.exception;

/**
 * Domain exception for payment-related business rule violations.
 * 
 * This exception is thrown when:
 * - Payment business rules are violated
 * - Payment constraints are not met
 * - Invalid payment state transitions are attempted
 * - Payment validation fails
 * - Fraud detection rules are triggered
 * 
 * Follows Clean Architecture by being part of the domain layer
 * with no infrastructure dependencies.
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 1 - Foundation
 */
public class PaymentDomainException extends Exception {
    
    private final String errorCode;
    private final String businessReason;
    
    /**
     * Creates payment domain exception with message
     */
    public PaymentDomainException(String message) {
        super(message);
        this.errorCode = "PAYMENT_DOMAIN_ERROR";
        this.businessReason = message;
    }
    
    /**
     * Creates payment domain exception with message and cause
     */
    public PaymentDomainException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "PAYMENT_DOMAIN_ERROR";
        this.businessReason = message;
    }
    
    /**
     * Creates payment domain exception with error code and business reason
     */
    public PaymentDomainException(String errorCode, String businessReason) {
        super(businessReason);
        this.errorCode = errorCode;
        this.businessReason = businessReason;
    }
    
    /**
     * Creates payment domain exception with error code, business reason and cause
     */
    public PaymentDomainException(String errorCode, String businessReason, Throwable cause) {
        super(businessReason, cause);
        this.errorCode = errorCode;
        this.businessReason = businessReason;
    }
    
    /**
     * Gets the business error code
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * Gets the business reason for the exception
     */
    public String getBusinessReason() {
        return businessReason;
    }
    
    // Common factory methods for typical payment domain exceptions
    
    public static PaymentDomainException paymentNotFound(String transactionId) {
        return new PaymentDomainException("PAYMENT_NOT_FOUND", 
            String.format("Payment with transaction ID '%s' not found", transactionId));
    }
    
    public static PaymentDomainException paymentNotRefundable(String transactionId, String currentStatus) {
        return new PaymentDomainException("PAYMENT_NOT_REFUNDABLE", 
            String.format("Payment '%s' with status '%s' cannot be refunded", transactionId, currentStatus));
    }
    
    public static PaymentDomainException invalidStatusTransition(String from, String to) {
        return new PaymentDomainException("INVALID_PAYMENT_STATUS_TRANSITION", 
            String.format("Invalid payment status transition from '%s' to '%s'", from, to));
    }
    
    public static PaymentDomainException insufficientFunds(String customerId, double amount) {
        return new PaymentDomainException("INSUFFICIENT_FUNDS", 
            String.format("Customer '%s' has insufficient funds for amount %.2f", customerId, amount));
    }
    
    public static PaymentDomainException paymentMethodNotSupported(String paymentMethod) {
        return new PaymentDomainException("PAYMENT_METHOD_NOT_SUPPORTED", 
            String.format("Payment method '%s' is not supported", paymentMethod));
    }
    
    public static PaymentDomainException fraudDetected(String transactionId, String reason) {
        return new PaymentDomainException("FRAUD_DETECTED", 
            String.format("Fraud detected for transaction '%s': %s", transactionId, reason));
    }
    
    public static PaymentDomainException amountExceedsLimit(double amount, double limit) {
        return new PaymentDomainException("AMOUNT_EXCEEDS_LIMIT", 
            String.format("Payment amount %.2f exceeds allowed limit %.2f", amount, limit));
    }
    
    public static PaymentDomainException invalidCurrency(String currency) {
        return new PaymentDomainException("INVALID_CURRENCY", 
            String.format("Currency '%s' is not supported", currency));
    }
    
    public static PaymentDomainException refundAmountExceedsOriginal(double refundAmount, double originalAmount) {
        return new PaymentDomainException("REFUND_AMOUNT_EXCEEDS_ORIGINAL", 
            String.format("Refund amount %.2f exceeds original payment amount %.2f", refundAmount, originalAmount));
    }
    
    public static PaymentDomainException validationFailed(String fieldName, String reason) {
        return new PaymentDomainException("PAYMENT_VALIDATION_FAILED", 
            String.format("Payment validation failed for field '%s': %s", fieldName, reason));
    }
}
