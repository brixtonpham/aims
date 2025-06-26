package com.aims.domain.cart.entity;

import com.aims.domain.product.entity.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * CartItem entity representing individual items in a shopping cart
 * Enhanced with proper JPA mappings and business methods
 */
@Entity
@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Long cartItemId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Positive(message = "Quantity must be positive")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
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
     * Get total price for this cart item (quantity * unit price)
     */
    public long getTotalPrice() {
        if (product == null || product.getPrice() == null || quantity == null) {
            return 0;
        }
        return (long) product.getPrice() * quantity;
    }
    
    /**
     * Update quantity with validation
     */
    public void updateQuantity(int newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        
        if (product != null && !product.isAvailable(newQuantity)) {
            throw new IllegalStateException("Insufficient stock for requested quantity");
        }
        
        this.quantity = newQuantity;
    }
    
    /**
     * Check if the requested quantity is available
     */
    public boolean isAvailable() {
        return product != null && product.isAvailable(quantity);
    }
    
    /**
     * Get unit price from product
     */
    public Integer getUnitPrice() {
        return product != null ? product.getPrice() : 0;
    }
    
    /**
     * Get product title for display
     */
    public String getProductTitle() {
        return product != null ? product.getTitle() : "Unknown Product";
    }
    
    /**
     * Check if this item supports rush order
     */
    public boolean supportsRushOrder() {
        return product != null && product.supportsRushOrder();
    }
    
    /**
     * Get total weight for this cart item
     */
    public float getTotalWeight() {
        if (product == null || quantity == null) {
            return 0.0f;
        }
        return product.getShippingWeight() * quantity;
    }
    
    /**
     * Business validation
     */
    public boolean isValid() {
        return product != null && 
               quantity != null && quantity > 0 && 
               isAvailable();
    }
    
    /**
     * Create cart item with validation
     */
    public static CartItem create(Cart cart, Product product, int quantity) {
        if (cart == null) {
            throw new IllegalArgumentException("Cart cannot be null");
        }
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (!product.isAvailable(quantity)) {
            throw new IllegalStateException("Product not available in requested quantity");
        }
        
        return CartItem.builder()
            .cart(cart)
            .product(product)
            .quantity(quantity)
            .build();
    }
}
