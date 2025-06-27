package com.aims.domain.order.dto;

import java.util.Map;

/**
 * Value object representing the total calculation for an order.
 * 
 * Provides detailed breakdown of order amounts:
 * - Subtotal before taxes and fees
 * - Tax calculations
 * - Delivery fees
 * - Discounts and promotions
 * - Final total amount
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 1 - Foundation
 */
public class OrderTotal {
    
    private final Double subtotal;
    private final Double taxAmount;
    private final Double deliveryFee;
    private final Double rushOrderFee;
    private final Double discountAmount;
    private final Double totalAmount;
    private final String currency;
    private final Map<String, Double> taxBreakdown;
    private final Map<String, Double> feeBreakdown;
    
    public OrderTotal(Builder builder) {
        this.subtotal = builder.subtotal;
        this.taxAmount = builder.taxAmount;
        this.deliveryFee = builder.deliveryFee;
        this.rushOrderFee = builder.rushOrderFee;
        this.discountAmount = builder.discountAmount;
        this.totalAmount = builder.totalAmount;
        this.currency = builder.currency;
        this.taxBreakdown = builder.taxBreakdown;
        this.feeBreakdown = builder.feeBreakdown;
    }
    
    // Getters
    
    public Double getSubtotal() {
        return subtotal;
    }
    
    public Double getTaxAmount() {
        return taxAmount;
    }
    
    public Double getDeliveryFee() {
        return deliveryFee;
    }
    
    public Double getRushOrderFee() {
        return rushOrderFee;
    }
    
    public Double getDiscountAmount() {
        return discountAmount;
    }
    
    public Double getTotalAmount() {
        return totalAmount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public Map<String, Double> getTaxBreakdown() {
        return taxBreakdown;
    }
    
    public Map<String, Double> getFeeBreakdown() {
        return feeBreakdown;
    }
    
    // Business methods
    
    /**
     * Gets the total before taxes
     */
    public Double getTotalBeforeTax() {
        return subtotal + (deliveryFee != null ? deliveryFee : 0.0) + 
               (rushOrderFee != null ? rushOrderFee : 0.0) - 
               (discountAmount != null ? discountAmount : 0.0);
    }
    
    /**
     * Gets the tax rate as percentage
     */
    public Double getTaxRate() {
        Double totalBeforeTax = getTotalBeforeTax();
        return totalBeforeTax != null && totalBeforeTax > 0 && taxAmount != null ? 
               (taxAmount / totalBeforeTax) * 100 : 0.0;
    }
    
    /**
     * Gets total savings from discounts
     */
    public Double getTotalSavings() {
        return discountAmount != null ? discountAmount : 0.0;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Double subtotal;
        private Double taxAmount;
        private Double deliveryFee;
        private Double rushOrderFee;
        private Double discountAmount;
        private Double totalAmount;
        private String currency = "VND";
        private Map<String, Double> taxBreakdown;
        private Map<String, Double> feeBreakdown;
        
        public Builder subtotal(Double subtotal) {
            this.subtotal = subtotal;
            return this;
        }
        
        public Builder taxAmount(Double taxAmount) {
            this.taxAmount = taxAmount;
            return this;
        }
        
        public Builder deliveryFee(Double deliveryFee) {
            this.deliveryFee = deliveryFee;
            return this;
        }
        
        public Builder rushOrderFee(Double rushOrderFee) {
            this.rushOrderFee = rushOrderFee;
            return this;
        }
        
        public Builder discountAmount(Double discountAmount) {
            this.discountAmount = discountAmount;
            return this;
        }
        
        public Builder totalAmount(Double totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }
        
        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }
        
        public Builder taxBreakdown(Map<String, Double> taxBreakdown) {
            this.taxBreakdown = taxBreakdown;
            return this;
        }
        
        public Builder feeBreakdown(Map<String, Double> feeBreakdown) {
            this.feeBreakdown = feeBreakdown;
            return this;
        }
        
        public OrderTotal build() {
            return new OrderTotal(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format("OrderTotal{subtotal=%.2f, tax=%.2f, delivery=%.2f, total=%.2f %s}", 
                           subtotal, taxAmount, deliveryFee, totalAmount, currency);
    }
    
    // Additional convenience methods for order domain service
    public long getTotalBeforeVat() {
        return subtotal != null ? subtotal.longValue() : 0L;
    }
    
    public long getTotalAfterVat() {
        return (subtotal != null && taxAmount != null) ? 
            (subtotal.longValue() + taxAmount.longValue()) : 0L;
    }
    
    public long getGrandTotal() {
        return totalAmount != null ? totalAmount.longValue() : 0L;
    }
    
    public long getVatAmount() {
        return taxAmount != null ? taxAmount.longValue() : 0L;
    }
    
    public int getVatRate() {
        if (subtotal != null && subtotal > 0 && taxAmount != null) {
            return (int) Math.round((taxAmount / subtotal) * 100);
        }
        return 0;
    }
    
    public float getTotalWeight() {
        // This is a simple implementation - in real scenarios weight would be calculated separately
        return 1.0f; // Default weight
    }
}
