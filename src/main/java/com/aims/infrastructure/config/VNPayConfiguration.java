package com.aims.infrastructure.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aims.vnpay.common.service.PaymentService;
import com.aims.vnpay.common.service.VNPayService;
import com.aims.vnpay.common.service.VNPayServiceImpl;

/**
 * Configuration class to resolve VNPay service bean naming conflicts.
 * 
 * The wrapper VNPayServiceImpl is excluded from automatic component scanning
 * in the main application class and manually registered here with a different
 * bean name to avoid conflicts with the main implementation in the impl package.
 * 
 * Entity and repository scanning for VNPay is handled in the main application class
 * to ensure all JPA entities are managed by a single Hibernate context.
 */
@Configuration
public class VNPayConfiguration {

    /**
     * Manually register the excluded VNPayServiceImpl wrapper as a bean.
     * This provides an alternative implementation if needed elsewhere in the application.
     * 
     * @param vnpayService The main VNPayService implementation
     * @return The wrapper service registered as "vnpayServiceWrapper"
     */
    @Bean("vnpayServiceWrapper")
    public PaymentService vnpayServiceWrapper(@Autowired VNPayService vnpayService) {
        return new VNPayServiceImpl(vnpayService);
    }
}
