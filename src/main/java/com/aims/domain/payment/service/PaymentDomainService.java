// src/main/java/com/aims/domain/payment/service/PaymentDomainService.java
package com.aims.domain.payment.service;

import com.aims.domain.payment.model.*;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Domain service interface for payment operations
 * Clean Architecture - Domain Layer
 * No infrastructure dependencies
 */
public interface PaymentDomainService {
    
    /**
     * Process payment for an order
     * @param request Domain payment request
     * @return Payment result with URL or error details
     */
    PaymentResult processPayment(DomainPaymentRequest request);
    
    /**
     * Query payment status
     * @param transactionId Transaction identifier
     * @return Payment status information
     */
    PaymentStatus getPaymentStatus(String transactionId);
    
    /**
     * Process refund for a payment
     * @param request Domain refund request
     * @return Refund result
     */
    RefundResult processRefund(DomainRefundRequest request);
    
    /**
     * Validate transaction authenticity
     * @param transactionId Transaction to validate
     * @return true if transaction is valid
     */
    boolean validateTransaction(String transactionId);
    
    /**
     * Get payment method name
     * @return Payment method identifier
     */
    String getPaymentMethodName();
}