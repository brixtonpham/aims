package com.aims.domain.order.service;

/**
 * Order Service Interface
 * 
 * Domain service interface for order operations.
 * Used by PaymentApplicationService to update order status based on payment results.
 */
public interface OrderService {

    /**
     * Check if an order can be processed for payment
     * 
     * @param orderId The order ID
     * @return true if order can be paid
     */
    boolean canProcessPayment(String orderId);

    /**
     * Mark order as paid
     * 
     * @param orderId The order ID
     */
    void markOrderAsPaid(String orderId);

    /**
     * Mark order payment as failed
     * 
     * @param orderId The order ID
     */
    void markOrderPaymentFailed(String orderId);

    /**
     * Mark order as refunded
     * 
     * @param orderId The order ID
     */
    void markOrderAsRefunded(String orderId);

    /**
     * Get order total amount
     * 
     * @param orderId The order ID
     * @return Order total amount
     */
    Long getOrderAmount(String orderId);

    /**
     * Check if order exists
     * 
     * @param orderId The order ID
     * @return true if order exists
     */
    boolean orderExists(String orderId);

    /**
     * Get order status
     * 
     * @param orderId The order ID
     * @return Order status
     */
    String getOrderStatus(String orderId);
}
