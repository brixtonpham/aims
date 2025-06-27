package com.aims.domain.payment.factory;

import com.aims.domain.payment.service.PaymentDomainService;
import com.aims.domain.payment.model.PaymentMethod;
import com.aims.domain.payment.exception.UnsupportedPaymentMethodException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Central coordinator for payment service factories
 * 
 * This class coordinates multiple payment service factories
 * and selects the appropriate one based on payment method
 * and regional requirements.
 * 
 * Implements the Factory Pattern with multiple concrete factories.
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 3 - Advanced Patterns
 */
@Component("paymentServiceFactoryCoordinator")
public class PaymentServiceFactoryCoordinator {
    
    private final List<AbstractPaymentServiceFactory> factories;
    
    @Autowired
    public PaymentServiceFactoryCoordinator(List<AbstractPaymentServiceFactory> factories) {
        this.factories = factories;
    }
    
    /**
     * Gets a payment service for the specified payment method
     * 
     * Iterates through available factories to find one that supports
     * the requested payment method.
     * 
     * @param paymentMethod The payment method to get service for
     * @return PaymentDomainService implementation
     * @throws UnsupportedPaymentMethodException if no factory supports the method
     */
    public PaymentDomainService getPaymentService(PaymentMethod paymentMethod) {
        for (AbstractPaymentServiceFactory factory : factories) {
            if (factory.supports(paymentMethod)) {
                return factory.createPaymentService(paymentMethod);
            }
        }
        
        throw new UnsupportedPaymentMethodException(
            "No factory found for payment method: " + paymentMethod);
    }
    
    /**
     * Gets a payment service from a specific regional factory
     * 
     * @param paymentMethod The payment method
     * @param region The preferred region (e.g., "VIETNAM", "GLOBAL")
     * @return PaymentDomainService implementation
     * @throws UnsupportedPaymentMethodException if method/region not supported
     */
    public PaymentDomainService getPaymentService(PaymentMethod paymentMethod, String region) {
        for (AbstractPaymentServiceFactory factory : factories) {
            if (factory.getRegion().equals(region) && factory.supports(paymentMethod)) {
                return factory.createPaymentService(paymentMethod);
            }
        }
        
        throw new UnsupportedPaymentMethodException(
            "No factory found for payment method: " + paymentMethod + " in region: " + region);
    }
    
    /**
     * Checks if any factory supports the given payment method
     * 
     * @param paymentMethod The payment method to check
     * @return true if at least one factory supports the method
     */
    public boolean isSupported(PaymentMethod paymentMethod) {
        return factories.stream()
            .anyMatch(factory -> factory.supports(paymentMethod));
    }
    
    /**
     * Gets all available factories
     * 
     * @return List of available payment service factories
     */
    public List<AbstractPaymentServiceFactory> getAvailableFactories() {
        return List.copyOf(factories);
    }
}
