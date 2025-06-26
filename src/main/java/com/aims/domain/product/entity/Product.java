package com.aims.domain.product.entity;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Product entity following Clean Architecture principles
 * Consolidated from multiple Product entities with proper JPA mappings
 */
@Entity
@Table(name = "products")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "product_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Book.class, name = "book"),
    @JsonSubTypes.Type(value = CD.class, name = "cd"),
    @JsonSubTypes.Type(value = DVD.class, name = "dvd")
})
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;
    
    @NotBlank(message = "Product title is required")
    @Column(name = "title", nullable = false)
    private String title;
    
    @Positive(message = "Price must be positive")
    @Column(name = "price", nullable = false)
    private Integer price;
    
    @PositiveOrZero(message = "Weight must be positive or zero")
    @Column(name = "weight")
    private Float weight;
    
    @Column(name = "rush_order_supported")
    @Builder.Default
    private Boolean rushOrderSupported = false;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "barcode")
    private String barcode;
    
    @Column(name = "import_date")
    private LocalDateTime importDate;
    
    @Column(name = "introduction", columnDefinition = "TEXT")
    private String introduction;
    
    @PositiveOrZero(message = "Quantity must be positive or zero")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "product_type", insertable = false, updatable = false)
    private String type;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (rushOrderSupported == null) {
            rushOrderSupported = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Business methods
    
    /**
     * Check if product is available for the requested quantity
     */
    public boolean isAvailable(int requestedQuantity) {
        return quantity != null && quantity >= requestedQuantity && requestedQuantity > 0;
    }
    
    /**
     * Reserve quantity for an order
     * @param requestedQuantity quantity to reserve
     * @return true if successfully reserved, false if insufficient stock
     */
    public boolean reserveQuantity(int requestedQuantity) {
        if (!isAvailable(requestedQuantity)) {
            return false;
        }
        this.quantity -= requestedQuantity;
        return true;
    }
    
    /**
     * Release reserved quantity (e.g., when order is cancelled)
     */
    public void releaseQuantity(int quantityToRelease) {
        if (quantityToRelease > 0) {
            this.quantity += quantityToRelease;
        }
    }
    
    /**
     * Check if product supports rush order delivery
     */
    public boolean supportsRushOrder() {
        return Boolean.TRUE.equals(rushOrderSupported);
    }
    
    /**
     * Check if product is in stock
     */
    public boolean isInStock() {
        return quantity != null && quantity > 0;
    }
    
    /**
     * Get total value of available stock
     */
    public long getTotalStockValue() {
        if (quantity == null || price == null) {
            return 0;
        }
        return (long) quantity * price;
    }
    
    /**
     * Business validation for product consistency
     */
    public boolean isValid() {
        return title != null && !title.trim().isEmpty() &&
               price != null && price > 0 &&
               quantity != null && quantity >= 0;
    }
    
    /**
     * Calculate shipping weight (can be overridden by subclasses)
     */
    public float getShippingWeight() {
        return weight != null ? weight : 0.0f;
    }
}
