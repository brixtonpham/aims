package com.aims.domain.cart.entity;

import com.aims.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Cart entity following Clean Architecture principles
 * Enhanced with proper JPA mappings and business methods
 */
@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long cartId;
    
    @Column(name = "customer_id")
    private String customerId;
    
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<CartItem> cartItems = new ArrayList<>();
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Business methods
    
    /**
     * Add product to cart or update quantity if already exists
     */
    public CartItem addProduct(Product product, int quantity) {
        if (product == null || quantity <= 0) {
            throw new IllegalArgumentException("Product and quantity must be valid");
        }
        
        Optional<CartItem> existingItem = findCartItemByProduct(product.getProductId());
        
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.updateQuantity(item.getQuantity() + quantity);
            return item;
        } else {
            CartItem newItem = CartItem.builder()
                .cart(this)
                .product(product)
                .quantity(quantity)
                .build();
            cartItems.add(newItem);
            return newItem;
        }
    }
    
    /**
     * Update product quantity in cart
     */
    public boolean updateProductQuantity(Long productId, int newQuantity) {
        if (newQuantity <= 0) {
            return removeProduct(productId);
        }
        
        Optional<CartItem> item = findCartItemByProduct(productId);
        if (item.isPresent()) {
            item.get().updateQuantity(newQuantity);
            return true;
        }
        return false;
    }
    
    /**
     * Remove product from cart
     */
    public boolean removeProduct(Long productId) {
        return cartItems.removeIf(item -> 
            item.getProduct() != null && productId.equals(item.getProduct().getProductId()));
    }
    
    /**
     * Clear all items from cart
     */
    public void clearCart() {
        cartItems.clear();
    }
    
    /**
     * Get total number of items in cart
     */
    public int getTotalItems() {
        return cartItems.stream()
            .mapToInt(CartItem::getQuantity)
            .sum();
    }
    
    /**
     * Get total value of cart
     */
    public long getTotalValue() {
        return cartItems.stream()
            .mapToLong(CartItem::getTotalPrice)
            .sum();
    }
    
    /**
     * Check if cart is empty
     */
    public boolean isEmpty() {
        return cartItems.isEmpty();
    }
    
    /**
     * Find cart item by product ID
     */
    public Optional<CartItem> findCartItemByProduct(Long productId) {
        return cartItems.stream()
            .filter(item -> item.getProduct() != null && 
                          productId.equals(item.getProduct().getProductId()))
            .findFirst();
    }
    
    /**
     * Check if all products in cart are available
     */
    public boolean areAllProductsAvailable() {
        return cartItems.stream()
            .allMatch(item -> item.getProduct().isAvailable(item.getQuantity()));
    }
    
    /**
     * Get items that are not available
     */
    public List<CartItem> getUnavailableItems() {
        return cartItems.stream()
            .filter(item -> !item.getProduct().isAvailable(item.getQuantity()))
            .toList();
    }
    
    /**
     * Check if cart supports rush order (all items must support rush order)
     */
    public boolean supportsRushOrder() {
        return !cartItems.isEmpty() && 
               cartItems.stream().allMatch(item -> item.getProduct().supportsRushOrder());
    }
    
    /**
     * Get total weight of cart for shipping calculation
     */
    public float getTotalWeight() {
        return cartItems.stream()
            .map(item -> item.getProduct().getShippingWeight() * item.getQuantity())
            .reduce(0.0f, Float::sum);
    }
    
    /**
     * Business validation
     */
    public boolean isValid() {
        return !cartItems.isEmpty() && areAllProductsAvailable();
    }
}
