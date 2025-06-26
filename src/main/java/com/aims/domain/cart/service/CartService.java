package com.aims.domain.cart.service;

import com.aims.domain.cart.entity.Cart;

/**
 * Cart domain service interface
 * Contains pure business logic for cart operations
 */
public interface CartService {
    
    /**
     * Add product to cart
     */
    Cart addProductToCart(Cart cart, Long productId, Integer quantity);
    
    /**
     * Update cart item quantity
     */
    Cart updateCartItemQuantity(Cart cart, Long cartItemId, Integer quantity);
    
    /**
     * Remove item from cart
     */
    Cart removeItemFromCart(Cart cart, Long cartItemId);
    
    /**
     * Clear all items from cart
     */
    void clearCart(Cart cart);
    
    /**
     * Calculate cart total
     */
    Integer calculateCartTotal(Cart cart);
}
