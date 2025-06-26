package com.aims.domain.order.repository;

import com.aims.domain.order.entity.OrderItem;
import java.util.List;
import java.util.Optional;

/**
 * OrderItem repository interface following Domain-Driven Design principles.
 * Defines essential operations for OrderItem domain (previously Orderline).
 */
public interface OrderItemRepository {
    
    /**
     * Save an order item entity
     * @param orderItem the order item to save
     * @return the saved order item with generated ID
     */
    OrderItem save(OrderItem orderItem);
    
    /**
     * Save multiple order items for an order
     * @param orderItems the list of order items to save
     * @return the list of saved order items
     */
    List<OrderItem> saveAll(List<OrderItem> orderItems);
    
    /**
     * Find an order item by its ID
     * @param orderItemId the order item ID
     * @return Optional containing the order item if found
     */
    Optional<OrderItem> findById(Long orderItemId);
    
    /**
     * Find all order items for a specific order
     * @param orderId the order ID
     * @return list of order items for the order
     */
    List<OrderItem> findByOrderId(Long orderId);
    
    /**
     * Find all order items
     * @return list of all order items
     */
    List<OrderItem> findAll();
    
    /**
     * Find order items by product ID
     * @param productId the product ID
     * @return list of order items containing the product
     */
    List<OrderItem> findByProductId(Long productId);
    
    /**
     * Update order item information
     * @param orderItem the order item to update
     * @return the updated order item
     */
    OrderItem update(OrderItem orderItem);
    
    /**
     * Delete an order item by ID
     * @param orderItemId the order item ID to delete
     */
    void deleteById(Long orderItemId);
    
    /**
     * Delete all order items for a specific order
     * @param orderId the order ID
     */
    void deleteByOrderId(Long orderId);
    
    /**
     * Check if order item exists by ID
     * @param orderItemId the order item ID
     * @return true if order item exists
     */
    boolean existsById(Long orderItemId);
    
    /**
     * Calculate total quantity for a product across all orders
     * @param productId the product ID
     * @return total quantity ordered
     */
    int getTotalQuantityByProductId(Long productId);
    
    /**
     * Get total value of all order items for an order
     * @param orderId the order ID
     * @return total value of the order
     */
    double getTotalValueByOrderId(Long orderId);
    
    /**
     * Find order items with quantity greater than specified amount
     * @param quantity the minimum quantity
     * @return list of order items with quantity >= specified amount
     */
    List<OrderItem> findByQuantityGreaterThan(int quantity);
}
