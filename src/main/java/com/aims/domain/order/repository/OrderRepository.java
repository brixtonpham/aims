package com.aims.domain.order.repository;

import com.aims.domain.order.entity.Order;
import java.util.List;
import java.util.Optional;

/**
 * Order repository interface following Domain-Driven Design principles.
 * Defines essential operations for Order domain.
 */
public interface OrderRepository {
    
    /**
     * Save an order entity
     * @param order the order to save
     * @return the saved order with generated ID
     */
    Order save(Order order);
    
    /**
     * Find an order by its ID
     * @param orderId the order ID
     * @return Optional containing the order if found
     */
    Optional<Order> findById(Long orderId);
    
    /**
     * Find all orders
     * @return list of all orders
     */
    List<Order> findAll();
    
    /**
     * Find orders by status
     * @param status the order status
     * @return list of orders with the specified status
     */
    List<Order> findByStatus(String status);
    
    /**
     * Find orders by customer ID
     * @param customerId the customer ID
     * @return list of orders for the customer
     */
    List<Order> findByCustomerId(Long customerId);
    
    /**
     * Find orders by delivery information ID
     * @param deliveryId the delivery information ID
     * @return list of orders with the specified delivery info
     */
    List<Order> findByDeliveryId(Long deliveryId);
    
    /**
     * Update order information
     * @param order the order to update
     * @return the updated order
     */
    Order update(Order order);
    
    /**
     * Delete an order by ID
     * @param orderId the order ID to delete
     */
    void deleteById(Long orderId);
    
    /**
     * Check if order exists by ID
     * @param orderId the order ID
     * @return true if order exists
     */
    boolean existsById(Long orderId);
    
    /**
     * Find order with items eagerly loaded
     * @param orderId the order ID
     * @return Optional containing the order with items if found
     */
    Optional<Order> findByIdWithItems(Long orderId);
    
    /**
     * Find orders within date range
     * @param startDate the start date (YYYY-MM-DD format)
     * @param endDate the end date (YYYY-MM-DD format)
     * @return list of orders within the date range
     */
    List<Order> findByDateRange(String startDate, String endDate);
    
    /**
     * Count orders by status
     * @param status the order status
     * @return number of orders with the status
     */
    long countByStatus(String status);
    
    /**
     * Find rush orders (orders requiring rush delivery)
     * @return list of rush orders
     */
    List<Order> findRushOrders();
    
    /**
     * Get total order count
     * @return total number of orders
     */
    long count();
}
