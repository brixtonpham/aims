package com.aims.domain.payment.repository;

import com.aims.domain.payment.entity.PaymentTransaction;
import java.util.List;
import java.util.Optional;

/**
 * PaymentTransaction repository interface following Domain-Driven Design principles.
 * Defines essential operations for PaymentTransaction domain.
 */
public interface PaymentTransactionRepository {
    
    /**
     * Save a payment transaction entity
     * @param transaction the transaction to save
     * @return the saved transaction with generated ID
     */
    PaymentTransaction save(PaymentTransaction transaction);
    
    /**
     * Find a payment transaction by its ID
     * @param transactionId the transaction ID
     * @return Optional containing the transaction if found
     */
    Optional<PaymentTransaction> findById(Long transactionId);
    
    /**
     * Find a payment transaction by external transaction ID (e.g., VNPay transaction ID)
     * @param externalTransactionId the external transaction ID
     * @return Optional containing the transaction if found
     */
    Optional<PaymentTransaction> findByExternalTransactionId(String externalTransactionId);
    
    /**
     * Find all payment transactions for a specific order
     * @param orderId the order ID
     * @return list of payment transactions for the order
     */
    List<PaymentTransaction> findByOrderId(Long orderId);
    
    /**
     * Find payment transactions by status
     * @param status the transaction status
     * @return list of transactions with the specified status
     */
    List<PaymentTransaction> findByStatus(String status);
    
    /**
     * Find payment transactions by payment method
     * @param paymentMethod the payment method (e.g., VNPAY, CASH, CARD)
     * @return list of transactions with the specified payment method
     */
    List<PaymentTransaction> findByPaymentMethod(String paymentMethod);
    
    /**
     * Find all payment transactions
     * @return list of all payment transactions
     */
    List<PaymentTransaction> findAll();
    
    /**
     * Update payment transaction information
     * @param transaction the transaction to update
     * @return the updated transaction
     */
    PaymentTransaction update(PaymentTransaction transaction);
    
    /**
     * Delete a payment transaction by ID
     * @param transactionId the transaction ID to delete
     */
    void deleteById(Long transactionId);
    
    /**
     * Check if payment transaction exists by ID
     * @param transactionId the transaction ID
     * @return true if transaction exists
     */
    boolean existsById(Long transactionId);
    
    /**
     * Find successful payment transactions for an order
     * @param orderId the order ID
     * @return list of successful payment transactions
     */
    List<PaymentTransaction> findSuccessfulTransactionsByOrderId(Long orderId);
    
    /**
     * Find failed payment transactions within date range
     * @param startDate the start date (YYYY-MM-DD format)
     * @param endDate the end date (YYYY-MM-DD format)
     * @return list of failed transactions within the date range
     */
    List<PaymentTransaction> findFailedTransactionsByDateRange(String startDate, String endDate);
    
    /**
     * Get total transaction amount by status
     * @param status the transaction status
     * @return total amount for the status
     */
    double getTotalAmountByStatus(String status);
    
    /**
     * Find transactions that can be refunded
     * @return list of refundable transactions
     */
    List<PaymentTransaction> findRefundableTransactions();
    
    /**
     * Get total transaction count
     * @return total number of transactions
     */
    long count();
    
    /**
     * Count transactions by status
     * @param status the transaction status
     * @return number of transactions with the status
     */
    long countByStatus(String status);
    
    /**
     * Find payment transaction by order ID (string format)
     * @param orderId the order ID as string
     * @return Optional containing the transaction if found
     */
    Optional<PaymentTransaction> findByOrderId(String orderId);
}
