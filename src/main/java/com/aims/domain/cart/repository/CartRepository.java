package com.aims.domain.cart.repository;

import com.aims.domain.cart.entity.Cart;
import java.util.List;
import java.util.Optional;

/**
 * Cart repository interface following Domain-Driven Design principles.
 * Defines essential operations for Cart domain.
 */
public interface CartRepository {
    
    /**
     * Save a cart entity
     * @param cart the cart to save
     * @return the saved cart with generated ID
     */
    Cart save(Cart cart);
    
    /**
     * Find a cart by its ID
     * @param cartId the cart ID
     * @return Optional containing the cart if found
     */
    Optional<Cart> findById(Long cartId);
    
    /**
     * Find cart by customer ID
     * @param customerId the customer ID
     * @return Optional containing the cart if found
     */
    Optional<Cart> findByCustomerId(Long customerId);
    
    /**
     * Update cart information
     * @param cart the cart to update
     * @return the updated cart
     */
    Cart update(Cart cart);
    
    /**
     * Delete a cart by ID
     * @param cartId the cart ID to delete
     */
    void deleteById(Long cartId);
    
    /**
     * Check if cart exists by ID
     * @param cartId the cart ID
     * @return true if cart exists
     */
    boolean existsById(Long cartId);
    
    /**
     * Clear all items from a cart
     * @param cartId the cart ID
     */
    void clearCart(Long cartId);
    
    /**
     * Get cart with items eagerly loaded
     * @param cartId the cart ID
     * @return Optional containing the cart with items if found
     */
    Optional<Cart> findByIdWithItems(Long cartId);
    
    /**
     * Find all carts (for admin purposes)
     * @return list of all carts
     */
    List<Cart> findAll();
    
    /**
     * Delete inactive carts older than specified days
     * @param days number of days
     * @return number of deleted carts
     */
    int deleteInactiveCartsOlderThan(int days);
}
