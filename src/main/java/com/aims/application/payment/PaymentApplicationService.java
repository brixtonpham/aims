package com.aims.application.payment;

import com.aims.domain.payment.service.PaymentDomainService;
import com.aims.domain.payment.factory.PaymentServiceFactory;
import com.aims.domain.payment.model.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application service that orchestrates payment operations
 * Uses the VNPay adapter (which preserves existing VNPay functionality)
 */
@Service
@Transactional
public class PaymentApplicationService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentApplicationService.class);
    
    private final PaymentServiceFactory paymentServiceFactory;
    
    @Autowired
    public PaymentApplicationService(PaymentServiceFactory paymentServiceFactory) {
        this.paymentServiceFactory = paymentServiceFactory;
    }
    
    /**
     * Process payment using appropriate payment service
     * Example usage of VNPay adapter
     */
    public PaymentResult processOrderPayment(String orderId, Long amount, PaymentMethod method, 
                                           String customerId, jakarta.servlet.http.HttpServletRequest request) {
        
        logger.info("Processing payment for order: {}, amount: {}, method: {}", 
            orderId, amount, method);
        
        try {
            // Get appropriate payment service (e.g., VNPay adapter)
            PaymentDomainService paymentService = paymentServiceFactory.getPaymentService(method);
            
            // Create domain payment request
            DomainPaymentRequest paymentRequest = DomainPaymentRequest.builder()
                .orderId(orderId)
                .amount(amount)
                .currency("VND")
                .customerId(customerId)
                .language("vn")
                .orderDescription("Payment for order " + orderId)
                .httpRequest(request)
                .build();
            
            // Process payment (delegates to VNPay adapter, which preserves existing VNPay)
            PaymentResult result = paymentService.processPayment(paymentRequest);
            
            logger.info("Payment processing completed for order: {}, success: {}", 
                orderId, result.isSuccess());
            
            return result;
            
        } catch (Exception e) {
            logger.error("Payment processing failed for order: {}", orderId, e);
            return PaymentResult.failure("Payment processing failed", "INTERNAL_ERROR");
        }
    }
    
    /**
     * Process refund using appropriate payment service
     * Example usage of VNPay adapter for refunds
     */
    public RefundResult processOrderRefund(String orderId, String transactionId, Long amount,
                                         String reason, jakarta.servlet.http.HttpServletRequest request) {
        
        logger.info("Processing refund for order: {}, transaction: {}, amount: {}", 
            orderId, transactionId, amount);
        
        try {
            // For this example, assuming VNPay (in production, determine from order data)
            PaymentDomainService paymentService = paymentServiceFactory.getPaymentService(PaymentMethod.VNPAY);
            
            // Create domain refund request
            DomainRefundRequest refundRequest = DomainRefundRequest.builder()
                .orderId(orderId)
                .transactionId(transactionId)
                .amount(amount)
                .reason(reason)
                .requestedBy("admin") // or get from security context
                .httpRequest(request)
                .build();
            
            // Process refund (delegates to VNPay adapter, which preserves existing VNPay)
            RefundResult result = paymentService.processRefund(refundRequest);
            
            logger.info("Refund processing completed for order: {}, success: {}", 
                orderId, result.isSuccess());
            
            return result;
            
        } catch (Exception e) {
            logger.error("Refund processing failed for order: {}", orderId, e);
            return RefundResult.failure("Refund processing failed");
        }
    }
}