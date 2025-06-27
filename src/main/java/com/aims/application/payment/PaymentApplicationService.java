package com.aims.application.payment;

import com.aims.application.command.CommandBus;
import com.aims.application.command.payment.ProcessPaymentCommand;
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
    private final CommandBus commandBus;
    
    @Autowired
    public PaymentApplicationService(PaymentServiceFactory paymentServiceFactory, CommandBus commandBus) {
        this.paymentServiceFactory = paymentServiceFactory;
        this.commandBus = commandBus;
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
    
    /**
     * Process payment using the new Command Pattern (Phase 3)
     * This method uses the CommandBus to execute ProcessPaymentCommand
     * 
     * @param request Payment processing request
     * @return Payment result
     */
    public com.aims.application.command.payment.PaymentResult processPaymentWithCommandBus(
            PaymentProcessingRequest request) {
        
        logger.info("Processing payment using CommandBus for order: {}", request.getOrderId());
        
        try {
            ProcessPaymentCommand command = ProcessPaymentCommand.builder()
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "VND")
                .paymentMethod(request.getPaymentMethod())
                .customerId(request.getCustomerId())
                .language(request.getLanguage() != null ? request.getLanguage() : "vn")
                .bankCode(request.getBankCode())
                .httpRequest(request.getHttpRequest())
                .build();
            
            return commandBus.execute(command);
            
        } catch (Exception e) {
            logger.error("Payment processing failed using CommandBus for order: {}", request.getOrderId(), e);
            throw new com.aims.application.command.CommandExecutionException("Failed to process payment", e);
        }
    }
    
    /**
     * Request object for payment processing
     */
    public static class PaymentProcessingRequest {
        private String orderId;
        private Long amount;
        private PaymentMethod paymentMethod;
        private String customerId;
        private String currency;
        private String language;
        private String bankCode;
        private jakarta.servlet.http.HttpServletRequest httpRequest;
        
        // Constructor
        public PaymentProcessingRequest(String orderId, Long amount, PaymentMethod paymentMethod, 
                                      String customerId, jakarta.servlet.http.HttpServletRequest httpRequest) {
            this.orderId = orderId;
            this.amount = amount;
            this.paymentMethod = paymentMethod;
            this.customerId = customerId;
            this.httpRequest = httpRequest;
        }
        
        // Getters and setters
        public String getOrderId() { return orderId; }
        public Long getAmount() { return amount; }
        public PaymentMethod getPaymentMethod() { return paymentMethod; }
        public String getCustomerId() { return customerId; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        public String getBankCode() { return bankCode; }
        public void setBankCode(String bankCode) { this.bankCode = bankCode; }
        public jakarta.servlet.http.HttpServletRequest getHttpRequest() { return httpRequest; }
    }
}