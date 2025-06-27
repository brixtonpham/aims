package com.aims.domain.payment.dto;

import java.util.List;

/**
 * DTO for payment method availability information.
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 1 - Foundation
 */
public class PaymentMethodAvailability {
    
    private final List<PaymentMethodInfo> availableMethods;
    private final String customerId;
    private final Double amount;
    private final String currency;
    
    public PaymentMethodAvailability(List<PaymentMethodInfo> availableMethods, 
                                   String customerId, Double amount, String currency) {
        this.availableMethods = availableMethods;
        this.customerId = customerId;
        this.amount = amount;
        this.currency = currency;
    }
    
    // Getters
    public List<PaymentMethodInfo> getAvailableMethods() { return availableMethods; }
    public String getCustomerId() { return customerId; }
    public Double getAmount() { return amount; }
    public String getCurrency() { return currency; }
    
    public static class PaymentMethodInfo {
        private final String methodCode;
        private final String methodName;
        private final String provider;
        private final boolean isAvailable;
        private final String unavailableReason;
        
        public PaymentMethodInfo(String methodCode, String methodName, String provider, 
                               boolean isAvailable, String unavailableReason) {
            this.methodCode = methodCode;
            this.methodName = methodName;
            this.provider = provider;
            this.isAvailable = isAvailable;
            this.unavailableReason = unavailableReason;
        }
        
        // Getters
        public String getMethodCode() { return methodCode; }
        public String getMethodName() { return methodName; }
        public String getProvider() { return provider; }
        public boolean isAvailable() { return isAvailable; }
        public String getUnavailableReason() { return unavailableReason; }
    }
}
