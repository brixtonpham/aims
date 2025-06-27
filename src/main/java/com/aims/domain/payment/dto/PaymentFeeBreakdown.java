package com.aims.domain.payment.dto;

import java.util.Map;

/**
 * DTO for payment fee breakdown calculations.
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 1 - Foundation
 */
public class PaymentFeeBreakdown {
    
    private final Double baseAmount;
    private final Double processingFee;
    private final Double currencyConversionFee;
    private final Double rushProcessingFee;
    private final Double totalFees;
    private final Double totalAmount;
    private final Map<String, Double> detailedFees;
    
    public PaymentFeeBreakdown(Double baseAmount, Double processingFee, 
                             Double currencyConversionFee, Double rushProcessingFee,
                             Map<String, Double> detailedFees) {
        this.baseAmount = baseAmount;
        this.processingFee = processingFee;
        this.currencyConversionFee = currencyConversionFee;
        this.rushProcessingFee = rushProcessingFee;
        this.totalFees = (processingFee != null ? processingFee : 0.0) + 
                        (currencyConversionFee != null ? currencyConversionFee : 0.0) +
                        (rushProcessingFee != null ? rushProcessingFee : 0.0);
        this.totalAmount = baseAmount + this.totalFees;
        this.detailedFees = detailedFees;
    }
    
    // Getters
    public Double getBaseAmount() { return baseAmount; }
    public Double getProcessingFee() { return processingFee; }
    public Double getCurrencyConversionFee() { return currencyConversionFee; }
    public Double getRushProcessingFee() { return rushProcessingFee; }
    public Double getTotalFees() { return totalFees; }
    public Double getTotalAmount() { return totalAmount; }
    public Map<String, Double> getDetailedFees() { return detailedFees; }
}
