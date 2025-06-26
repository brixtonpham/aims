package com.aims.domain.order.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order entity following Clean Architecture principles
 * Enhanced with proper JPA mappings and business methods
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;
    
    @Column(name = "customer_id")
    private String customerId;
    
    @PositiveOrZero(message = "Total before VAT must be positive or zero")
    @Column(name = "total_before_vat")
    private Long totalBeforeVAT;
    
    @PositiveOrZero(message = "Total after VAT must be positive or zero")
    @Column(name = "total_after_vat")
    private Long totalAfterVAT;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;
    
    @Column(name = "vat_rate")
    @Builder.Default
    private Integer vatRate = 10; // 10% VAT
    
    @Column(name = "order_time")
    private LocalDateTime orderTime;
    
    @Column(name = "payment_method")
    private String paymentMethod;
    
    @Column(name = "is_rush_order")
    @Builder.Default
    private Boolean isRushOrder = false;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();
    
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id")
    private DeliveryInfo deliveryInfo;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (orderTime == null) {
            orderTime = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Business methods
    
    /**
     * Add order item to the order
     */
    public OrderItem addOrderItem(Long productId, String productTitle, int quantity, int unitPrice) {
        OrderItem orderItem = OrderItem.builder()
            .order(this)
            .productId(productId)
            .productTitle(productTitle)
            .quantity(quantity)
            .unitPrice(unitPrice)
            .build();
        
        orderItems.add(orderItem);
        recalculateTotals();
        return orderItem;
    }
    
    /**
     * Remove order item
     */
    public boolean removeOrderItem(Long orderItemId) {
        boolean removed = orderItems.removeIf(item -> orderItemId.equals(item.getOrderItemId()));
        if (removed) {
            recalculateTotals();
        }
        return removed;
    }
    
    /**
     * Recalculate order totals
     */
    public void recalculateTotals() {
        totalBeforeVAT = orderItems.stream()
            .mapToLong(OrderItem::getTotalPrice)
            .sum();
        
        totalAfterVAT = totalBeforeVAT + (totalBeforeVAT * vatRate / 100);
    }
    
    /**
     * Get total number of items in order
     */
    public int getTotalItems() {
        return orderItems.stream()
            .mapToInt(OrderItem::getQuantity)
            .sum();
    }
    
    /**
     * Check if order can be cancelled
     */
    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }
    
    /**
     * Cancel the order
     */
    public void cancel() {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Order cannot be cancelled in current status: " + status);
        }
        this.status = OrderStatus.CANCELLED;
    }
    
    /**
     * Confirm the order
     */
    public void confirm() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be confirmed");
        }
        this.status = OrderStatus.CONFIRMED;
    }
    
    /**
     * Mark order as shipped
     */
    public void ship() {
        if (status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed orders can be shipped");
        }
        this.status = OrderStatus.SHIPPED;
    }
    
    /**
     * Mark order as delivered
     */
    public void deliver() {
        if (status != OrderStatus.SHIPPED) {
            throw new IllegalStateException("Only shipped orders can be delivered");
        }
        this.status = OrderStatus.DELIVERED;
    }
    
    /**
     * Check if order is completed
     */
    public boolean isCompleted() {
        return status == OrderStatus.DELIVERED;
    }
    
    /**
     * Check if order is cancelled
     */
    public boolean isCancelled() {
        return status == OrderStatus.CANCELLED;
    }
    
    /**
     * Get formatted total for display
     */
    public String getFormattedTotal() {
        return String.format("%,d VND", totalAfterVAT);
    }
    
    /**
     * Get total weight for shipping calculation
     */
    public float getTotalWeight() {
        return orderItems.stream()
            .map(OrderItem::getTotalWeight)
            .reduce(0.0f, Float::sum);
    }
    
    /**
     * Business validation
     */
    public boolean isValid() {
        return !orderItems.isEmpty() && 
               totalBeforeVAT != null && totalBeforeVAT >= 0 &&
               totalAfterVAT != null && totalAfterVAT >= 0 &&
               status != null;
    }
    
    /**
     * Order status enumeration
     */
    public enum OrderStatus {
        PENDING("Pending"),
        CONFIRMED("Confirmed"), 
        SHIPPED("Shipped"),
        DELIVERED("Delivered"),
        CANCELLED("Cancelled");
        
        private final String displayName;
        
        OrderStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
