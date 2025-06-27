package com.aims.domain.payment.factory;

import com.aims.domain.payment.service.PaymentDomainService;
import com.aims.domain.payment.model.PaymentMethod;
import com.aims.domain.payment.exception.UnsupportedPaymentMethodException;

import org.springframework.stereotype.Component;

/**
 * Global payment service factory
 * 
 * Creates payment services for international payment methods
 * such as credit cards and bank transfers.
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 3 - Advanced Patterns
 */
@Component
public class GlobalPaymentServiceFactory extends AbstractPaymentServiceFactory {
    
    @Override
    public PaymentDomainService createPaymentService(PaymentMethod paymentMethod) {
        switch (paymentMethod) {
            case CREDIT_CARD:
                throw new UnsupportedPaymentMethodException(
                    "Credit card payment service not yet implemented");
            case BANK_TRANSFER:
                throw new UnsupportedPaymentMethodException(
                    "Bank transfer payment service not yet implemented");
            default:
                throw new UnsupportedPaymentMethodException(
                    "Payment method not supported globally: " + paymentMethod);
        }
    }
    
    @Override
    public boolean supports(PaymentMethod paymentMethod) {
        return paymentMethod == PaymentMethod.CREDIT_CARD || 
               paymentMethod == PaymentMethod.BANK_TRANSFER;
    }
    
    @Override
    public String getRegion() {
        return "GLOBAL";
    }
    
    @Override
    public PaymentMethod[] getSupportedPaymentMethods() {
        return new PaymentMethod[] {
            PaymentMethod.CREDIT_CARD,
            PaymentMethod.BANK_TRANSFER
        };
    }
}
