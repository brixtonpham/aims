package com.aims.domain.cart.service;

import com.aims.domain.cart.entity.Cart;
import com.aims.domain.cart.entity.CartItem;
import com.aims.domain.cart.repository.CartRepository;
import com.aims.domain.product.entity.Product;
import com.aims.domain.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Domain service implementation for Cart business logic.
 * Contains pure business logic without orchestration concerns.
 * Follows Domain-Driven Design principles.
 */
@Service
@Transactional
public class CartServiceImpl implements CartService {
    
    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);
    
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    
    @Autowired
    public CartServiceImpl(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }
    
    @Override
    public Cart addProductToCart(Cart cart, Long productId, Integer quantity) {
        logger.debug("Adding product {} with quantity {} to cart", productId, quantity);
        
        // Business validation
        validateAddToCartRequest(productId, quantity);
        
        // Get product for validation
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        
        // Business rule: Use cart's built-in business logic
        cart.addProduct(product, quantity);
        
        // Save and return
        Cart savedCart = cartRepository.save(cart);
        logger.info("Product {} added to cart successfully", productId);
        
        return savedCart;
    }
    
    @Override
    public Cart updateCartItemQuantity(Cart cart, Long cartItemId, Integer quantity) {
        logger.debug("Updating cart item {} quantity to {}", cartItemId, quantity);
        
        // Business validation
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        
        // Find cart item
        CartItem cartItem = cart.getCartItems().stream()
            .filter(item -> item.getCartItemId().equals(cartItemId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + cartItemId));
        
        if (quantity == 0) {
            // Business rule: Remove item if quantity is 0
            cart.removeProduct(cartItem.getProduct().getProductId());
        } else {
            // Business rule: Update quantity using cart item's business logic
            cartItem.updateQuantity(quantity);
        }
        
        Cart savedCart = cartRepository.save(cart);
        logger.info("Cart item {} updated successfully", cartItemId);
        
        return savedCart;
    }
    
    @Override
    public Cart removeItemFromCart(Cart cart, Long cartItemId) {
        logger.debug("Removing cart item {} from cart", cartItemId);
        
        CartItem cartItem = cart.getCartItems().stream()
            .filter(item -> item.getCartItemId().equals(cartItemId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + cartItemId));
        
        cart.removeProduct(cartItem.getProduct().getProductId());
        
        Cart savedCart = cartRepository.save(cart);
        logger.info("Cart item {} removed successfully", cartItemId);
        
        return savedCart;
    }
    
    @Override
    public void clearCart(Cart cart) {
        logger.debug("Clearing cart {}", cart.getCartId());
        
        cart.clearCart();
        
        cartRepository.save(cart);
        logger.info("Cart {} cleared successfully", cart.getCartId());
    }
    
    @Override
    public Integer calculateCartTotal(Cart cart) {
        logger.debug("Calculating total for cart {}", cart.getCartId());
        
        long total = cart.getTotalValue();
        
        logger.debug("Cart total calculated: {}", total);
        return (int) total;
    }
    
    // Private helper methods for business logic
    
    private void validateAddToCartRequest(Long productId, Integer quantity) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Invalid product ID: " + productId);
        }
        
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive: " + quantity);
        }
    }
}
