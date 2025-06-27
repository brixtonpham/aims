package com.aims.domain.order.exception;

/**
 * Domain exception for order-related business rule violations.
 * 
 * This exception is thrown when:
 * - Business rules are violated during order operations
 * - Domain constraints are not met
 * - Invalid state transitions are attempted
 * - Business validation fails
 * 
 * Follows Clean Architecture by being part of the domain layer
 * with no infrastructure dependencies.
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 1 - Foundation
 */
public class OrderDomainException extends Exception {
    
    private final String errorCode;
    private final String businessReason;
    
    /**
     * Creates domain exception with message
     */
    public OrderDomainException(String message) {
        super(message);
        this.errorCode = "DOMAIN_ERROR";
        this.businessReason = message;
    }
    
    /**
     * Creates domain exception with message and cause
     */
    public OrderDomainException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "DOMAIN_ERROR";
        this.businessReason = message;
    }
    
    /**
     * Creates domain exception with error code and business reason
     */
    public OrderDomainException(String errorCode, String businessReason) {
        super(businessReason);
        this.errorCode = errorCode;
        this.businessReason = businessReason;
    }
    
    /**
     * Creates domain exception with error code, business reason and cause
     */
    public OrderDomainException(String errorCode, String businessReason, Throwable cause) {
        super(businessReason, cause);
        this.errorCode = errorCode;
        this.businessReason = businessReason;
    }
    
    /**
     * Gets the business error code
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * Gets the business reason for the exception
     */
    public String getBusinessReason() {
        return businessReason;
    }
    
    // Common factory methods for typical domain exceptions
    
    public static OrderDomainException orderNotFound(String orderId) {
        return new OrderDomainException("ORDER_NOT_FOUND", 
            String.format("Order with ID '%s' not found", orderId));
    }
    
    public static OrderDomainException orderNotCancellable(String orderId, String currentStatus) {
        return new OrderDomainException("ORDER_NOT_CANCELLABLE", 
            String.format("Order '%s' with status '%s' cannot be cancelled", orderId, currentStatus));
    }
    
    public static OrderDomainException invalidStatusTransition(String from, String to) {
        return new OrderDomainException("INVALID_STATUS_TRANSITION", 
            String.format("Invalid status transition from '%s' to '%s'", from, to));
    }
    
    public static OrderDomainException customerNotAuthorized(String customerId, String orderId) {
        return new OrderDomainException("CUSTOMER_NOT_AUTHORIZED", 
            String.format("Customer '%s' is not authorized to access order '%s'", customerId, orderId));
    }
    
    public static OrderDomainException insufficientInventory(String productId, int requested, int available) {
        return new OrderDomainException("INSUFFICIENT_INVENTORY", 
            String.format("Product '%s': requested %d, but only %d available", productId, requested, available));
    }
    
    public static OrderDomainException invalidOrderAmount(double amount, double minimum) {
        return new OrderDomainException("INVALID_ORDER_AMOUNT", 
            String.format("Order amount %.2f is below minimum required amount %.2f", amount, minimum));
    }
    
    public static OrderDomainException deliveryNotAvailable(String location) {
        return new OrderDomainException("DELIVERY_NOT_AVAILABLE", 
            String.format("Delivery is not available to location '%s'", location));
    }
    
    public static OrderDomainException validationFailed(String fieldName, String reason) {
        return new OrderDomainException("VALIDATION_FAILED", 
            String.format("Validation failed for field '%s': %s", fieldName, reason));
    }
}
