package com.aims.domain.payment.dto;

/**
 * Result DTO for refund eligibility checks.
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 1 - Foundation
 */
public class RefundEligibilityResult {
    
    private final boolean isEligible;
    private final String reason;
    private final Double maxRefundableAmount;
    private final Double alreadyRefundedAmount;
    
    public RefundEligibilityResult(boolean isEligible, String reason, 
                                 Double maxRefundableAmount, Double alreadyRefundedAmount) {
        this.isEligible = isEligible;
        this.reason = reason;
        this.maxRefundableAmount = maxRefundableAmount;
        this.alreadyRefundedAmount = alreadyRefundedAmount;
    }
    
    // Factory methods
    public static RefundEligibilityResult eligible(Double maxRefundableAmount) {
        return new RefundEligibilityResult(true, "Refund eligible", maxRefundableAmount, 0.0);
    }
    
    public static RefundEligibilityResult notEligible(String reason) {
        return new RefundEligibilityResult(false, reason, 0.0, 0.0);
    }
    
    // Getters
    public boolean isEligible() { return isEligible; }
    public String getReason() { return reason; }
    public Double getMaxRefundableAmount() { return maxRefundableAmount; }
    public Double getAlreadyRefundedAmount() { return alreadyRefundedAmount; }
}
