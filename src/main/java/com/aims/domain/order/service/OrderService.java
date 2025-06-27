package com.aims.domain.order.service;

/**
 * Order Service Interface
 * 
 * Domain service interface for order operations.
 */
public interface OrderService {

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
