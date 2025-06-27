package com.aims.domain.payment.service.temp;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.HashMap;

/**
 * TEMPORARY FLATTENED SERVICE - Business logic extracted from PayOrder module
 * 
 * Source: PayOrder/PayOrderController.java
 * Business Logic: Payment processing, VNPay integration adapter pattern
 * 
 * PHASE 1 TASK 1.3: Flatten existing payment business logic
 */
@Service
public class PaymentProcessingService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentProcessingService.class);
    
    /**
     * Create payment request - BUSINESS LOGIC EXTRACTED
     * 
     * Original Business Rules from PayOrder/PayOrderController:
     * - Set VNPay version to 2.1.0
     * - Default language to Vietnamese ("vn")
     * - Amount validation and formatting
     * - Request structure for VNPay integration
     */
    public Map<String, Object> createPaymentRequest(String amount, String language) {
        logger.info("Creating payment request for amount: {}", amount);
        
        // Validate amount (business rule from original)
        if (amount == null || amount.trim().isEmpty()) {
            throw new IllegalArgumentException("Amount is required");
        }
        
        // Default values from original PayOrder logic
        String vnpVersion = "2.1.0";
        String requestLanguage = (language != null && !language.trim().isEmpty()) ? language : "vn";
        
        // Build payment request (flattened from original controller)
        Map<String, Object> paymentRequest = new HashMap<>();
        paymentRequest.put("amount", amount);
        paymentRequest.put("language", requestLanguage);
        paymentRequest.put("vnp_Version", vnpVersion);
        
        // Business rule: Don't set bankCode to let user choose on VNPay interface
        // This preserves the original PayOrder behavior
        
        logger.info("Payment request created with version: {}, language: {}", vnpVersion, requestLanguage);
        return paymentRequest;
    }
    
    /**
     * Validate payment amount - BUSINESS LOGIC EXTRACTED
     * 
     * Business Rules:
     * - Amount must be positive
     * - Amount must be numeric
     * - Minimum amount validation
     */
    public void validatePaymentAmount(String amount) {
        if (amount == null || amount.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment amount is required");
        }
        
        try {
            long amountValue = Long.parseLong(amount);
            if (amountValue <= 0) {
                throw new IllegalArgumentException("Payment amount must be positive");
            }
            
            // Business rule: Minimum payment amount (from original system)
            if (amountValue < 1000) {
                throw new IllegalArgumentException("Minimum payment amount is 1,000 VND");
            }
            
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Payment amount must be a valid number");
        }
    }
    
    /**
     * Format payment response - BUSINESS LOGIC EXTRACTED
     * 
     * Original Business Rules: Standardize response format for frontend consumption
     */
    public Map<String, Object> formatPaymentResponse(Object paymentServiceResponse) {
        Map<String, Object> response = new HashMap<>();
        
        // This will be enhanced in Phase 2 when VNPay adapter is created
        // For now, preserve the original response structure
        response.put("payment_data", paymentServiceResponse);
        response.put("status", "success");
        
        logger.info("Payment response formatted successfully");
        return response;
    }
    
    /**
     * Get supported payment methods - BUSINESS LOGIC EXTRACTED
     * 
     * Business Rules from original system:
     * - VNPay is primary payment method
     * - Future: COD support
     */
    public String[] getSupportedPaymentMethods() {
        return new String[]{"VNPAY"}; // From original PayOrder implementation
    }
}
