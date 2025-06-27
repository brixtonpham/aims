package com.aims.domain.notification.service;

/**
 * Domain service interface for notification operations.
 * Encapsulates business logic for sending notifications without infrastructure concerns.
 * 
 * This interface follows Clean Architecture principles by:
 * - Defining pure business contracts for notifications
 * - Having no dependencies on external notification systems
 * - Being independent of frameworks and UI
 * - Focusing on business notification rules
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 1 - Foundation
 */
public interface NotificationService {

    /**
     * Sends order confirmation notification to customer.
     * 
     * @param orderId Unique order identifier
     * @param customerEmail Customer email address
     * @param customerName Customer name
     * @param orderDetails Order summary information
     */
    void sendOrderConfirmation(String orderId, String customerEmail, String customerName, String orderDetails);

    /**
     * Sends order cancellation notification to customer.
     * 
     * @param orderId Unique order identifier
     * @param customerEmail Customer email address
     * @param customerName Customer name
     * @param cancellationReason Reason for cancellation
     */
    void sendOrderCancellation(String orderId, String customerEmail, String customerName, String cancellationReason);

    /**
     * Sends payment confirmation notification to customer.
     * 
     * @param transactionId Payment transaction identifier
     * @param customerEmail Customer email address
     * @param customerName Customer name
     * @param amount Payment amount
     * @param currency Payment currency
     */
    void sendPaymentConfirmation(String transactionId, String customerEmail, String customerName, 
                                Double amount, String currency);

    /**
     * Sends refund confirmation notification to customer.
     * 
     * @param refundTransactionId Refund transaction identifier
     * @param customerEmail Customer email address
     * @param customerName Customer name
     * @param refundAmount Refund amount
     * @param currency Refund currency
     */
    void sendRefundConfirmation(String refundTransactionId, String customerEmail, String customerName, 
                               Double refundAmount, String currency);

    /**
     * Sends order status update notification to customer.
     * 
     * @param orderId Unique order identifier
     * @param customerEmail Customer email address
     * @param customerName Customer name
     * @param newStatus New order status
     * @param statusDescription Status description
     */
    void sendOrderStatusUpdate(String orderId, String customerEmail, String customerName, 
                              String newStatus, String statusDescription);
}
