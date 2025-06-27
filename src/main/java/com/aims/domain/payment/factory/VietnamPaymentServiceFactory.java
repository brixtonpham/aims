package com.aims.domain.payment.factory;

import com.aims.domain.payment.service.PaymentDomainService;
import com.aims.domain.payment.model.PaymentMethod;
import com.aims.domain.payment.exception.UnsupportedPaymentMethodException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Vietnam-specific payment service factory
 * 
 * Creates payment services tailored for the Vietnamese market.
 * Primarily supports VNPay as the main online payment method.
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 3 - Advanced Patterns
 */
@Component
public class VietnamPaymentServiceFactory extends AbstractPaymentServiceFactory {
    
    private final PaymentDomainService vnpayService;
    
    @Autowired
    public VietnamPaymentServiceFactory(
            @Qualifier("vnpayPaymentAdapter") PaymentDomainService vnpayService) {
        this.vnpayService = vnpayService;
    }
    
    @Override
    public PaymentDomainService createPaymentService(PaymentMethod paymentMethod) {
        switch (paymentMethod) {
            case VNPAY:
                return vnpayService;
            case COD:
                // COD doesn't need a complex service, but we could return a simple implementation
                throw new UnsupportedPaymentMethodException(
                    "COD payment service not implemented in Vietnam factory");
            default:
                throw new UnsupportedPaymentMethodException(
                    "Payment method not supported in Vietnam: " + paymentMethod);
        }
    }
    
    @Override
    public boolean supports(PaymentMethod paymentMethod) {
        return paymentMethod == PaymentMethod.VNPAY || 
               paymentMethod == PaymentMethod.COD;
    }
    
    @Override
    public String getRegion() {
        return "VIETNAM";
    }
    
    @Override
    public PaymentMethod[] getSupportedPaymentMethods() {
        return new PaymentMethod[] {
            PaymentMethod.VNPAY,
            PaymentMethod.COD
        };
    }
}
