package com.aims.domain.payment.factory;

import com.aims.domain.payment.service.PaymentDomainService;
import com.aims.domain.payment.model.PaymentMethod;

/**
 * Abstract Factory for payment services
 * 
 * Defines the contract for creating payment services.
 * This follows the Abstract Factory Pattern by providing
 * a family of related payment service creation methods.
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 3 - Advanced Patterns
 */
public abstract class AbstractPaymentServiceFactory {
    
    /**
     * Creates a payment service for the specified payment method
     * 
     * @param paymentMethod The payment method to create service for
     * @return PaymentDomainService implementation
     */
    public abstract PaymentDomainService createPaymentService(PaymentMethod paymentMethod);
    
    /**
     * Checks if this factory can create services for the given payment method
     * 
     * @param paymentMethod The payment method to check
     * @return true if this factory supports the payment method
     */
    public abstract boolean supports(PaymentMethod paymentMethod);
    
    /**
     * Gets the region this factory is designed for
     * 
     * @return Region identifier (e.g., "VIETNAM", "GLOBAL")
     */
    public abstract String getRegion();
    
    /**
     * Gets all payment methods supported by this factory
     * 
     * @return Array of supported payment methods
     */
    public abstract PaymentMethod[] getSupportedPaymentMethods();
}
