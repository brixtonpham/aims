package com.aims.presentation.web;

import com.aims.application.commands.AddProductToCartCommand;
import com.aims.application.commands.UpdateCartCommand;
import com.aims.application.commands.ViewCartQuery;
import com.aims.application.services.CartApplicationService;
import com.aims.domain.cart.entity.Cart;
import com.aims.domain.cart.entity.CartItem;
import com.aims.presentation.dto.ApiResponse;
import com.aims.presentation.dto.CartRequest;
import com.aims.presentation.dto.CartResponse;
import com.aims.presentation.dto.CartItemResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for Cart operations
 * Handles cart workflows based on sequence diagrams
 * Delegates business logic to CartApplicationService
 */
@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/cart")
public class CartController {

    private static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    private static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    private final CartApplicationService cartApplicationService;

    @Autowired
    public CartController(CartApplicationService cartApplicationService) {
        this.cartApplicationService = cartApplicationService;
    }

    /**
     * Add product to cart
     * POST /api/cart/items
     * Based on "Add Product to Cart" sequence diagram
     */
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addProductToCart(@RequestBody CartRequest request) {
        try {
            // Validate request
            validateAddToCartRequest(request);

            // Create command
            AddProductToCartCommand command = AddProductToCartCommand.builder()
                .customerId(request.getCustomerId().toString())
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .build();

            // Delegate to application service
            Cart cart = cartApplicationService.addProductToCart(command);

            // Convert to response
            CartResponse response = mapToCartResponse(cart);

            return ResponseEntity.ok(ApiResponse.success("Product added to cart successfully", response));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), VALIDATION_ERROR));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to add product to cart", INTERNAL_ERROR));
        }
    }

    /**
     * View cart
     * GET /api/cart/{customerId}
     * Based on "View Cart" workflow
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<ApiResponse<CartResponse>> viewCart(@PathVariable("customerId") Long customerId) {
        try {
            // Validate customer ID
            if (customerId == null || customerId <= 0) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid customer ID", VALIDATION_ERROR));
            }

            // Create query
            ViewCartQuery query = ViewCartQuery.builder()
                .customerId(customerId.toString())
                .build();

            // Delegate to application service
            Optional<Cart> cart = cartApplicationService.viewCart(query);

            if (cart.isPresent()) {
                CartResponse response = mapToCartResponse(cart.get());
                return ResponseEntity.ok(ApiResponse.success(response));
            } else {
                // Return empty cart for customer
                CartResponse emptyCart = CartResponse.empty(customerId);
                return ResponseEntity.ok(ApiResponse.success(emptyCart));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to get cart", INTERNAL_ERROR));
        }
    }

    /**
     * Update cart item quantity
     * PUT /api/cart/items/{itemId}
     */
    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            @PathVariable("itemId") Long itemId,
            @RequestBody CartRequest request) {
        try {
            // Validate request
            if (itemId == null || itemId <= 0) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid cart item ID", VALIDATION_ERROR));
            }
            if (request.getQuantity() == null || request.getQuantity() < 0) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Valid quantity is required", VALIDATION_ERROR));
            }

            // Create command
            UpdateCartCommand command = UpdateCartCommand.builder()
                .cartItemId(itemId)
                .quantity(request.getQuantity())
                .build();

            // Delegate to application service
            Cart cart = cartApplicationService.updateCartItem(command);

            // Convert to response
            CartResponse response = mapToCartResponse(cart);

            return ResponseEntity.ok(ApiResponse.success("Cart item updated successfully", response));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), VALIDATION_ERROR));
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(ApiResponse.error("Operation not implemented", "NOT_IMPLEMENTED"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to update cart item", INTERNAL_ERROR));
        }
    }

    /**
     * Remove item from cart
     * DELETE /api/cart/items/{itemId}
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> removeCartItem(@PathVariable("itemId") Long itemId) {
        try {
            // Validate cart item ID
            if (itemId == null || itemId <= 0) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid cart item ID", VALIDATION_ERROR));
            }

            // Delegate to application service
            cartApplicationService.removeCartItem(itemId);

            return ResponseEntity.ok(ApiResponse.success("Cart item removed successfully", null));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), VALIDATION_ERROR));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to remove cart item", INTERNAL_ERROR));
        }
    }

    /**
     * Clear cart
     * DELETE /api/cart/{customerId}
     */
    @DeleteMapping("/{customerId}")
    public ResponseEntity<ApiResponse<Void>> clearCart(@PathVariable("customerId") Long customerId) {
        try {
            // Validate customer ID
            if (customerId == null || customerId <= 0) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid customer ID", VALIDATION_ERROR));
            }

            // Delegate to application service
            cartApplicationService.clearCart(customerId.toString());

            return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully", null));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to clear cart", INTERNAL_ERROR));
        }
    }

    // Private helper methods

    private void validateAddToCartRequest(CartRequest request) {
        if (request.getCustomerId() == null || request.getCustomerId() <= 0) {
            throw new IllegalArgumentException("Valid customer ID is required");
        }
        if (request.getProductId() == null || request.getProductId() <= 0) {
            throw new IllegalArgumentException("Valid product ID is required");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Valid quantity is required");
        }
    }

    private CartResponse mapToCartResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setCartId(cart.getCartId());
        response.setCustomerId(Long.parseLong(cart.getCustomerId()));
        response.setCreatedAt(cart.getCreatedAt());
        response.setUpdatedAt(cart.getUpdatedAt());

        // Map cart items
        List<CartItemResponse> itemResponses = cart.getCartItems().stream()
            .map(this::mapToCartItemResponse)
            .toList();
        response.setItems(itemResponses);

        // Calculate totals
        response.setTotalItems(cart.getCartItems().size());
        BigDecimal totalAmount = cart.getCartItems().stream()
            .map(item -> BigDecimal.valueOf(item.getProduct().getPrice()).multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setTotalAmount(totalAmount);

        return response;
    }

    private CartItemResponse mapToCartItemResponse(CartItem cartItem) {
        CartItemResponse response = new CartItemResponse();
        response.setCartItemId(cartItem.getCartItemId());
        response.setProductId(cartItem.getProduct().getProductId());
        response.setProductTitle(cartItem.getProduct().getTitle());
        response.setProductType(cartItem.getProduct().getType());
        response.setUnitPrice(BigDecimal.valueOf(cartItem.getProduct().getPrice()));
        response.setQuantity(cartItem.getQuantity());
        response.setTotalPrice(BigDecimal.valueOf((long) cartItem.getProduct().getPrice() * cartItem.getQuantity()));
        response.setImageUrl(cartItem.getProduct().getImageUrl());
        response.setAvailable(cartItem.getProduct().isAvailable(cartItem.getQuantity()));

        return response;
    }
}
