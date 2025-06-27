package com.aims.domain.payment.factory;

import com.aims.domain.payment.service.PaymentDomainService;
import com.aims.domain.payment.model.PaymentMethod;
import com.aims.domain.payment.exception.UnsupportedPaymentMethodException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Factory for creating payment services based on payment method
 * Implements Factory Pattern for payment method selection
 */
@Component
public class PaymentServiceFactory {
    
    private final Map<PaymentMethod, PaymentDomainService> paymentServices;
    
    @Autowired
    public PaymentServiceFactory(
        @Qualifier("vnpayPaymentAdapter") PaymentDomainService vnpayAdapter
        // Add other payment services here as needed
    ) {
        
        this.paymentServices = Map.of(
            PaymentMethod.VNPAY, vnpayAdapter
            // PaymentMethod.COD, codPaymentService,
            // PaymentMethod.BANK_TRANSFER, bankTransferService
        );
    }
    
    /**
     * Get payment service for specified payment method
     * @param method Payment method
     * @return PaymentDomainService implementation
     * @throws UnsupportedPaymentMethodException if method not supported
     */
    public PaymentDomainService getPaymentService(PaymentMethod method) {
        PaymentDomainService service = paymentServices.get(method);
        if (service == null) {
            throw new UnsupportedPaymentMethodException(
                "Payment method not supported: " + method
            );
        }
        return service;
    }
    
    /**
     * Check if payment method is supported
     * @param method Payment method to check
     * @return true if supported
     */
    public boolean isPaymentMethodSupported(PaymentMethod method) {
        return paymentServices.containsKey(method);
    }
}