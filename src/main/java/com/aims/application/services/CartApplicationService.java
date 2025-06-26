package com.aims.application.services;

import com.aims.application.commands.AddProductToCartCommand;
import com.aims.application.commands.UpdateCartCommand;
import com.aims.application.commands.ViewCartQuery;
import com.aims.application.exceptions.CartApplicationException;
import com.aims.domain.cart.entity.Cart;
import com.aims.domain.cart.entity.CartItem;
import com.aims.domain.cart.service.CartService;
import com.aims.domain.cart.repository.CartRepository;
import com.aims.domain.product.entity.Product;
import com.aims.domain.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Application Service for Cart domain orchestration.
 * Handles cart workflows and coordinates between cart and product services.
 */
@Service
@Transactional
public class CartApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(CartApplicationService.class);

    private final CartService cartService;
    private final ProductService productService;
    private final CartRepository cartRepository;

    @Autowired
    public CartApplicationService(@Qualifier("cartServiceImpl") CartService cartService, 
                                 ProductService productService,
                                 CartRepository cartRepository) {
        this.cartService = cartService;
        this.productService = productService;
        this.cartRepository = cartRepository;
    }

    /**
     * Add product to cart workflow
     * Implements the Add Product to Cart sequence from requirements
     */
    public Cart addProductToCart(AddProductToCartCommand command) {
        try {
            logger.info("Starting add product to cart workflow for customer: {}, product: {}", 
                       command.getCustomerId(), command.getProductId());

            // Step 1: Validate product availability
            validateProductAvailability(command.getProductId(), command.getQuantity());

            // Step 2: Get or create cart for customer
            Cart cart = getOrCreateCart(command.getCustomerId());

            // Step 3: Delegate to domain service for business logic
            Cart updatedCart = cartService.addProductToCart(cart, command.getProductId(), command.getQuantity());

            logger.info("Product added to cart successfully for customer: {}", command.getCustomerId());
            return updatedCart;

        } catch (IllegalArgumentException e) {
            logger.error("Validation failed for add product to cart: {}", e.getMessage());
            throw new CartApplicationException("Validation failed: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Failed to add product to cart: {}", e.getMessage(), e);
            throw new CartApplicationException("Failed to add product to cart: " + e.getMessage(), e);
        }
    }

    /**
     * View cart workflow
     */
    public Optional<Cart> viewCart(ViewCartQuery query) {
        try {
            logger.debug("Starting view cart workflow for customer: {}", query.getCustomerId());

            // Validate customer ID
            if (query.getCustomerId() == null || query.getCustomerId().isEmpty()) {
                throw new IllegalArgumentException("Customer ID is required");
            }

            // Convert String customerId to Long for repository call
            Long customerIdLong;
            try {
                customerIdLong = Long.parseLong(query.getCustomerId());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid customer ID format: " + query.getCustomerId());
            }

            // Get cart for customer
            Optional<Cart> cart = cartRepository.findByCustomerId(customerIdLong);

            if (cart.isPresent()) {
                // Validate cart items availability
                validateCartItemsAvailability(cart.get());
            }

            return cart;

        } catch (Exception e) {
            logger.error("Failed to view cart for customer {}: {}", query.getCustomerId(), e.getMessage(), e);
            throw new CartApplicationException("Failed to view cart: " + e.getMessage(), e);
        }
    }

    /**
     * Update cart item workflow
     */
    public Cart updateCartItem(UpdateCartCommand command) {
        try {
            logger.info("Starting update cart item workflow for item: {}", command.getCartItemId());

            // Step 1: Validate command
            validateUpdateCartCommand(command);

            // Step 2: Get cart
            Cart cart = getCartByItemId(command.getCartItemId());

            // Step 3: Validate product availability for new quantity
            CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getCartItemId().equals(command.getCartItemId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

            validateProductAvailability(cartItem.getProduct().getProductId(), command.getQuantity());

            // Step 4: Delegate to domain service
            Cart updatedCart = cartService.updateCartItemQuantity(cart, command.getCartItemId(), command.getQuantity());

            logger.info("Cart item updated successfully: {}", command.getCartItemId());
            return updatedCart;

        } catch (IllegalArgumentException e) {
            logger.error("Validation failed for update cart item: {}", e.getMessage());
            throw new CartApplicationException("Validation failed: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Failed to update cart item: {}", e.getMessage(), e);
            throw new CartApplicationException("Failed to update cart item: " + e.getMessage(), e);
        }
    }

    /**
     * Clear cart workflow
     */
    public void clearCart(String customerId) {
        try {
            logger.info("Starting clear cart workflow for customer: {}", customerId);

            // Validate customer ID
            if (customerId == null || customerId.isEmpty()) {
                throw new IllegalArgumentException("Customer ID is required");
            }

            // Convert String customerId to Long for repository call
            Long customerIdLong;
            try {
                customerIdLong = Long.parseLong(customerId);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid customer ID format: " + customerId);
            }

            // Get cart
            Optional<Cart> cartOpt = cartRepository.findByCustomerId(customerIdLong);
            if (cartOpt.isPresent()) {
                Cart cart = cartOpt.get();
                cartService.clearCart(cart);
            }

            logger.info("Cart cleared successfully for customer: {}", customerId);

        } catch (Exception e) {
            logger.error("Failed to clear cart for customer {}: {}", customerId, e.getMessage(), e);
            throw new CartApplicationException("Failed to clear cart: " + e.getMessage(), e);
        }
    }

    // Private helper methods

    private void validateProductAvailability(Long productId, Integer quantity) {
        Optional<Product> product = productService.getProductById(productId);
        if (product.isEmpty()) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }

        if (!product.get().isAvailable(quantity)) {
            throw new IllegalArgumentException("Insufficient stock for product: " + productId);
        }
    }

    private Cart getOrCreateCart(String customerId) {
        // Convert String customerId to Long for repository call
        // In production, this would depend on your customer ID strategy
        Long customerIdLong;
        try {
            customerIdLong = Long.parseLong(customerId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid customer ID format: " + customerId);
        }
        
        Optional<Cart> existingCart = cartRepository.findByCustomerId(customerIdLong);
        if (existingCart.isPresent()) {
            return existingCart.get();
        }

        // Create new cart
        Cart newCart = Cart.builder()
            .customerId(customerId)
            .build();
        return cartRepository.save(newCart);
    }

    private void validateCartItemsAvailability(Cart cart) {
        for (CartItem item : cart.getCartItems()) {
            if (!item.getProduct().isAvailable(item.getQuantity())) {
                logger.warn("Cart item {} is no longer available in requested quantity", item.getCartItemId());
                // In production, you might want to update the cart or notify the user
            }
        }
    }

    private void validateUpdateCartCommand(UpdateCartCommand command) {
        if (command.getCartItemId() == null) {
            throw new IllegalArgumentException("Cart item ID is required");
        }
        if (command.getQuantity() == null || command.getQuantity() < 0) {
            throw new IllegalArgumentException("Valid quantity is required");
        }
    }

    private Cart getCartByItemId(Long cartItemId) {
        // This would typically be handled by the repository
        // For now, we'll throw an exception as this needs implementation
        throw new UnsupportedOperationException("getCartByItemId not implemented yet - need to implement cart item to cart lookup");
    }
}
