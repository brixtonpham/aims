package com.aims.presentation.boundary;

/**
 * Notification Boundary Interface
 * 
 * Clean Architecture boundary interface for notification operations.
 * Supports multiple notification channels (email, SMS, push notifications).
 * Implementations should be in the infrastructure layer.
 */
public interface NotificationBoundary {

    /**
     * Sends payment success notification
     * 
     * @param orderId The order ID
     * @param amount The payment amount
     */
    void sendPaymentSuccessNotification(String orderId, Long amount);

    /**
     * Sends payment failure notification
     * 
     * @param orderId The order ID
     * @param responseCode The payment response code
     */
    void sendPaymentFailureNotification(String orderId, String responseCode);

    /**
     * Sends refund notification
     * 
     * @param orderId The order ID
     * @param refundAmount The refund amount
     */
    void sendRefundNotification(String orderId, Long refundAmount);

    /**
     * Sends order confirmation notification
     * 
     * @param orderId The order ID
     * @param customerEmail The customer email
     * @param orderDetails The order details
     */
    void sendOrderConfirmationNotification(String orderId, String customerEmail, String orderDetails);

    /**
     * Sends order cancellation notification
     * 
     * @param orderId The order ID
     * @param customerEmail The customer email
     * @param reason The cancellation reason
     */
    void sendOrderCancellationNotification(String orderId, String customerEmail, String reason);

    /**
     * Sends general notification
     * 
     * @param recipient The recipient (email, phone number, etc.)
     * @param subject The notification subject
     * @param message The notification message
     * @param channel The notification channel (EMAIL, SMS, PUSH)
     */
    void sendNotification(String recipient, String subject, String message, NotificationChannel channel);

    /**
     * Gets the notification provider name
     * 
     * @return Provider name (e.g., "Email", "SMS", "Push")
     */
    String getProviderName();

    /**
     * Notification channel enumeration
     */
    enum NotificationChannel {
        EMAIL,
        SMS,
        PUSH,
        IN_APP
    }
}
