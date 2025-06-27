package com.aims.domain.payment.entity;

/**
 * Payment status enumeration defining the lifecycle states of a payment transaction.
 * 
 * Status Flow:
 * PENDING → AUTHORIZED → CAPTURED → COMPLETED
 *    ↓         ↓           ↓          
 * FAILED    CANCELLED   CANCELLED    
 *              ↓           ↓
 *          REFUNDED    REFUNDED
 * 
 * Business Rules:
 * - Payments start in PENDING status
 * - FAILED, CANCELLED, COMPLETED, and REFUNDED are terminal states
 * - Status transitions must follow the defined flow
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 1 - Foundation
 */
public enum PaymentStatus {
    
    /**
     * Payment has been initiated but not yet processed.
     * Customer has submitted payment information.
     */
    PENDING("Pending", "Payment is being processed"),
    
    /**
     * Payment has been authorized by payment provider.
     * Funds are reserved but not yet captured.
     */
    AUTHORIZED("Authorized", "Payment authorized, funds reserved"),
    
    /**
     * Payment has been captured from customer account.
     * Funds are being transferred.
     */
    CAPTURED("Captured", "Payment captured, funds being transferred"),
    
    /**
     * Payment has been successfully completed.
     * Funds have been received.
     */
    COMPLETED("Completed", "Payment completed successfully"),
    
    /**
     * Payment has failed due to various reasons.
     * No funds were transferred.
     */
    FAILED("Failed", "Payment failed"),
    
    /**
     * Payment was cancelled before completion.
     * Any reserved funds are released.
     */
    CANCELLED("Cancelled", "Payment was cancelled"),
    
    /**
     * Payment was refunded to customer.
     * Funds returned to customer account.
     */
    REFUNDED("Refunded", "Payment has been refunded");
    
    private final String displayName;
    private final String description;
    
    PaymentStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * Gets user-friendly display name for the status
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets detailed description of the status
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Checks if this status allows payment cancellation
     */
    public boolean isCancellable() {
        return this == PENDING || this == AUTHORIZED;
    }
    
    /**
     * Checks if this status allows refund processing
     */
    public boolean isRefundable() {
        return this == COMPLETED;
    }
    
    /**
     * Checks if this is a terminal status (no further transitions allowed)
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED || this == REFUNDED;
    }
    
    /**
     * Checks if this status indicates successful payment
     */
    public boolean isSuccessful() {
        return this == COMPLETED;
    }
    
    /**
     * Checks if this status indicates payment failure
     */
    public boolean isFailed() {
        return this == FAILED || this == CANCELLED;
    }
    
    /**
     * Gets the next possible statuses from current status
     */
    public PaymentStatus[] getNextPossibleStatuses() {
        switch (this) {
            case PENDING:
                return new PaymentStatus[]{AUTHORIZED, FAILED, CANCELLED};
            case AUTHORIZED:
                return new PaymentStatus[]{CAPTURED, CANCELLED};
            case CAPTURED:
                return new PaymentStatus[]{COMPLETED, CANCELLED};
            case COMPLETED:
                return new PaymentStatus[]{REFUNDED};
            case FAILED:
            case CANCELLED:
            case REFUNDED:
            default:
                return new PaymentStatus[0]; // Terminal states
        }
    }
    
    /**
     * Validates if transition to target status is allowed
     */
    public boolean canTransitionTo(PaymentStatus targetStatus) {
        PaymentStatus[] possibleStatuses = getNextPossibleStatuses();
        for (PaymentStatus status : possibleStatuses) {
            if (status == targetStatus) {
                return true;
            }
        }
        return false;
    }
}
