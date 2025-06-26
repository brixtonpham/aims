package com.aims.presentation.boundary;

import com.aims.presentation.dto.payment.PaymentRequest;
import com.aims.presentation.dto.payment.PaymentResponse;
import com.aims.presentation.dto.payment.PaymentStatusResponse;
import com.aims.presentation.dto.payment.RefundRequest;
import com.aims.presentation.dto.payment.RefundResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Payment Boundary Interface
 * 
 * Clean Architecture boundary interface for payment operations.
 * Decouples payment logic from specific payment providers.
 * Implementations should be in the infrastructure layer.
 */
public interface PaymentBoundary {

    /**
     * Initiates a payment transaction
     * 
     * @param request Payment request containing amount, order info, etc.
     * @param servletRequest HTTP request for getting client IP
     * @return PaymentResponse containing payment URL and transaction details
     */
    PaymentResponse initiatePayment(PaymentRequest request, HttpServletRequest servletRequest);

    /**
     * Checks the status of a payment transaction
     * 
     * @param transactionId The transaction ID to check
     * @param transactionDate The original transaction date
     * @param servletRequest HTTP request for getting client IP
     * @return PaymentStatusResponse containing current transaction status
     */
    PaymentStatusResponse checkPaymentStatus(String transactionId, String transactionDate, HttpServletRequest servletRequest);

    /**
     * Processes a refund request
     * 
     * @param request Refund request containing transaction details
     * @param servletRequest HTTP request for getting client IP
     * @return RefundResponse containing refund status
     */
    RefundResponse processRefund(RefundRequest request, HttpServletRequest servletRequest);

    /**
     * Gets the name of the payment provider
     * 
     * @return Provider name (e.g., "VNPay", "MoMo", "ZaloPay")
     */
    String getProviderName();

    /**
     * Validates payment callback/notification
     * Used for IPN (Instant Payment Notification) validation
     * 
     * @param params Parameters received from payment provider
     * @return true if the callback is valid and authentic
     */
    boolean validatePaymentCallback(java.util.Map<String, String> params);
}
