package com.aims.domain.order.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * OrderItem entity (formerly Orderline)
 * Represents individual line items in an order
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long orderItemId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @NotBlank(message = "Product title is required")
    @Column(name = "product_title")
    private String productTitle;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private OrderItemStatus status = OrderItemStatus.PENDING;
    
    @Column(name = "rush_order_enabled")
    @Builder.Default
    private Boolean rushOrderEnabled = false;
    
    @Positive(message = "Quantity must be positive")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @PositiveOrZero(message = "Unit price must be positive or zero")
    @Column(name = "unit_price", nullable = false)
    private Integer unitPrice;
    
    @PositiveOrZero(message = "Total fee must be positive or zero")
    @Column(name = "total_fee")
    private Long totalFee;
    
    @Column(name = "delivery_time")
    private LocalDateTime deliveryTime;
    
    @Column(name = "instructions")
    private String instructions;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calculateTotalFee();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateTotalFee();
    }
    
    // Business methods
    
    /**
     * Calculate total fee (quantity * unit price)
     */
    public void calculateTotalFee() {
        if (quantity != null && unitPrice != null) {
            this.totalFee = (long) quantity * unitPrice;
        }
    }
    
    /**
     * Get total price for this order item
     */
    public long getTotalPrice() {
        if (totalFee != null) {
            return totalFee;
        }
        if (quantity != null && unitPrice != null) {
            return (long) quantity * unitPrice;
        }
        return 0;
    }
    
    /**
     * Update quantity and recalculate total
     */
    public void updateQuantity(int newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.quantity = newQuantity;
        calculateTotalFee();
    }
    
    /**
     * Update unit price and recalculate total
     */
    public void updateUnitPrice(int newUnitPrice) {
        if (newUnitPrice < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
        this.unitPrice = newUnitPrice;
        calculateTotalFee();
    }
    
    /**
     * Mark item as confirmed
     */
    public void confirm() {
        if (status != OrderItemStatus.PENDING) {
            throw new IllegalStateException("Only pending items can be confirmed");
        }
        this.status = OrderItemStatus.CONFIRMED;
    }
    
    /**
     * Mark item as shipped
     */
    public void ship() {
        if (status != OrderItemStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed items can be shipped");
        }
        this.status = OrderItemStatus.SHIPPED;
    }
    
    /**
     * Mark item as delivered
     */
    public void deliver() {
        if (status != OrderItemStatus.SHIPPED) {
            throw new IllegalStateException("Only shipped items can be delivered");
        }
        this.status = OrderItemStatus.DELIVERED;
        this.deliveryTime = LocalDateTime.now();
    }
    
    /**
     * Cancel the item
     */
    public void cancel() {
        if (status == OrderItemStatus.DELIVERED) {
            throw new IllegalStateException("Delivered items cannot be cancelled");
        }
        this.status = OrderItemStatus.CANCELLED;
    }
    
    /**
     * Check if item is delivered
     */
    public boolean isDelivered() {
        return status == OrderItemStatus.DELIVERED;
    }
    
    /**
     * Check if item is cancelled
     */
    public boolean isCancelled() {
        return status == OrderItemStatus.CANCELLED;
    }
    
    /**
     * Get estimated weight (assuming 1kg if not specified)
     */
    public float getTotalWeight() {
        // This is a placeholder - in a real scenario, we'd get weight from Product
        return quantity != null ? quantity * 1.0f : 0.0f;
    }
    
    /**
     * Get formatted total for display
     */
    public String getFormattedTotal() {
        return String.format("%,d VND", getTotalPrice());
    }
    
    /**
     * Business validation
     */
    public boolean isValid() {
        return productId != null &&
               quantity != null && quantity > 0 &&
               unitPrice != null && unitPrice >= 0 &&
               status != null;
    }
    
    /**
     * Factory method to create order item with validation
     */
    public static OrderItem create(Order order, Long productId, String productTitle, int quantity, int unitPrice) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
        
        return OrderItem.builder()
            .order(order)
            .productId(productId)
            .productTitle(productTitle)
            .quantity(quantity)
            .unitPrice(unitPrice)
            .build();
    }
    
    /**
     * Order item status enumeration
     */
    public enum OrderItemStatus {
        PENDING("Pending"),
        CONFIRMED("Confirmed"),
        SHIPPED("Shipped"),
        DELIVERED("Delivered"),
        CANCELLED("Cancelled");
        
        private final String displayName;
        
        OrderItemStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
