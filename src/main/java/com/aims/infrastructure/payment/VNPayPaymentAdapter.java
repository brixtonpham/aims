package com.aims.infrastructure.payment;

import com.aims.domain.payment.service.PaymentDomainService;
import com.aims.domain.payment.model.*;

// Import existing VNPay services - DO NOT MODIFY VNPAY FOLDER
import com.aims.vnpay.common.service.PaymentService;
import com.aims.vnpay.common.dto.PaymentRequest;
import com.aims.vnpay.common.dto.QueryRequest;
import com.aims.vnpay.common.dto.RefundRequest;
import com.aims.vnpay.common.service.VNPayService.PaymentResponse;
import com.aims.vnpay.common.service.VNPayService.QueryResponse;
import com.aims.vnpay.common.service.VNPayService.RefundResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter that bridges Domain layer to existing VNPay infrastructure
 * CRITICAL: This adapter NEVER modifies vnpay folder - only delegates to existing services
 * 
 * Implements Adapter Pattern:
 * - Domain layer expects PaymentDomainService interface
 * - Infrastructure layer has existing VNPay services
 * - This adapter translates between them without modification
 */
@Component("vnpayPaymentAdapter")
public class VNPayPaymentAdapter implements PaymentDomainService {
    
    private static final Logger logger = LoggerFactory.getLogger(VNPayPaymentAdapter.class);
    
    // Inject EXISTING VNPay service - DO NOT MODIFY VNPAY FOLDER
    private final PaymentService vnpayService;
    
    @Autowired
    public VNPayPaymentAdapter(@Qualifier("vnpayService") PaymentService vnpayService) {
        this.vnpayService = vnpayService;
        logger.info("VNPay Payment Adapter initialized with existing VNPay service");
    }
    
    @Override
    public PaymentResult processPayment(DomainPaymentRequest request) {
        logger.info("Processing payment for order: {}, amount: {}", 
            request.getOrderId(), request.getAmount());
        
        try {
            // Adapter pattern: Convert domain request to VNPay format
            PaymentRequest vnpayRequest = adaptToVNPayRequest(request);
            
            // Delegate to existing VNPay service - NO MODIFICATION TO VNPAY
            PaymentResponse vnpayResponse = vnpayService.createPayment(
                vnpayRequest, 
                request.getHttpRequest()
            );
            
            // Convert VNPay response back to domain format
            return adaptVNPayResponseToDomain(vnpayResponse);
            
        } catch (Exception e) {
            logger.error("Payment processing failed for order: {}", request.getOrderId(), e);
            return PaymentResult.failure(
                "Payment processing failed: " + e.getMessage(), 
                "PAYMENT_ERROR"
            );
        }
    }
    
    @Override
    public PaymentStatus getPaymentStatus(String transactionId) {
        logger.debug("Querying payment status for transaction: {}", transactionId);
        
        try {
            // Create query request for existing VNPay service
            QueryRequest queryRequest = new QueryRequest();
            queryRequest.setOrderId(transactionId);
            
            // Delegate to existing VNPay service
            QueryResponse queryResponse = vnpayService.queryTransaction(
                queryRequest, 
                null // HttpRequest not needed for query
            );
            
            // Convert VNPay response to domain status
            return adaptVNPayStatusToDomain(queryResponse);
            
        } catch (Exception e) {
            logger.error("Failed to query payment status for transaction: {}", transactionId, e);
            return PaymentStatus.FAILED;
        }
    }
    
    @Override
    public RefundResult processRefund(DomainRefundRequest request) {
        logger.info("Processing refund for order: {}, amount: {}", 
            request.getOrderId(), request.getAmount());
        
        try {
            // Adapter pattern: Convert domain refund request to VNPay format
            RefundRequest vnpayRefundRequest = adaptToVNPayRefundRequest(request);
            
            // Delegate to existing VNPay service - NO MODIFICATION TO VNPAY
            RefundResponse vnpayRefundResponse = vnpayService.refundTransaction(
                vnpayRefundRequest, 
                request.getHttpRequest()
            );
            
            // Convert VNPay refund response back to domain format
            return adaptVNPayRefundResponseToDomain(vnpayRefundResponse, request.getAmount());
            
        } catch (Exception e) {
            logger.error("Refund processing failed for order: {}", request.getOrderId(), e);
            return RefundResult.failure("Refund processing failed: " + e.getMessage());
        }
    }
    
    @Override
    public boolean validateTransaction(String transactionId) {
        logger.debug("Validating transaction: {}", transactionId);
        
        try {
            PaymentStatus status = getPaymentStatus(transactionId);
            return status == PaymentStatus.SUCCESS;
        } catch (Exception e) {
            logger.error("Transaction validation failed for: {}", transactionId, e);
            return false;
        }
    }
    
    @Override
    public String getPaymentMethodName() {
        return "VNPAY";
    }
    
    // =====================================
    // ADAPTER CONVERSION METHODS
    // =====================================
    
    /**
     * Convert domain payment request to VNPay format
     * This preserves all VNPay functionality without modification
     */
    private PaymentRequest adaptToVNPayRequest(DomainPaymentRequest domainRequest) {
        PaymentRequest vnpayRequest = new PaymentRequest();
        
        // Convert amount to string as expected by VNPay
        vnpayRequest.setAmount(String.valueOf(domainRequest.getAmount()));
        
        // Set bank code if provided
        if (domainRequest.getBankCode() != null && !domainRequest.getBankCode().isEmpty()) {
            vnpayRequest.setBankCode(domainRequest.getBankCode());
        }
        
        // Set language (default to 'vn' if not provided)
        vnpayRequest.setLanguage(
            domainRequest.getLanguage() != null ? domainRequest.getLanguage() : "vn"
        );
        
        // Set VNPay version as expected by existing service
        vnpayRequest.setVnp_Version("2.1.0");
        
        logger.debug("Converted domain request to VNPay format: amount={}, language={}", 
            vnpayRequest.getAmount(), vnpayRequest.getLanguage());
        
        return vnpayRequest;
    }
    
    /**
     * Convert VNPay payment response to domain format
     */
    private PaymentResult adaptVNPayResponseToDomain(PaymentResponse vnpayResponse) {
        if (vnpayResponse == null) {
            return PaymentResult.failure("No response from VNPay service", "NO_RESPONSE");
        }
        
        // Determine success based on VNPay response
        boolean isSuccess = vnpayResponse.getCode() != null && 
                           (vnpayResponse.getCode().equals("00") || vnpayResponse.getCode().equals("0"));
        
        if (isSuccess) {
            return PaymentResult.success(
                vnpayResponse.getPaymentUrl(), 
                null // Transaction ID will be generated by VNPay after payment
            );
        } else {
            return PaymentResult.failure(
                vnpayResponse.getMessage() != null ? vnpayResponse.getMessage() : "Payment failed",
                vnpayResponse.getCode()
            );
        }
    }
    
    /**
     * Convert domain refund request to VNPay format
     */
    private RefundRequest adaptToVNPayRefundRequest(DomainRefundRequest domainRequest) {
        RefundRequest vnpayRefundRequest = new RefundRequest();
        
        // Set order ID
        vnpayRefundRequest.setOrderId(domainRequest.getOrderId());
        
        // Convert amount to int as expected by VNPay
        vnpayRefundRequest.setAmount(domainRequest.getAmount().intValue());
        
        // Set transaction date if available
        if (domainRequest.getTransactionDate() != null) {
            // Convert LocalDateTime to VNPay expected format (yyyyMMddHHmmss)
            java.time.format.DateTimeFormatter formatter = 
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            vnpayRefundRequest.setTransDate(domainRequest.getTransactionDate().format(formatter));
        }
        
        // Set refund type - full refund
        vnpayRefundRequest.setTranType("02");
        
        // Set user requesting refund
        vnpayRefundRequest.setUser(
            domainRequest.getRequestedBy() != null ? domainRequest.getRequestedBy() : "admin"
        );
        
        logger.debug("Converted domain refund request to VNPay format: orderId={}, amount={}", 
            vnpayRefundRequest.getOrderId(), vnpayRefundRequest.getAmount());
        
        return vnpayRefundRequest;
    }
    
    /**
     * Convert VNPay refund response to domain format
     */
    private RefundResult adaptVNPayRefundResponseToDomain(RefundResponse vnpayResponse, Long originalAmount) {
        if (vnpayResponse == null) {
            return RefundResult.failure("No response from VNPay refund service");
        }
        
        // Determine success based on VNPay response
        boolean isSuccess = vnpayResponse.getVnp_ResponseCode() != null && 
                           vnpayResponse.getVnp_ResponseCode().equals("00");
        
        if (isSuccess) {
            return RefundResult.success(
                originalAmount, 
                vnpayResponse.getVnp_TxnRef(),
                vnpayResponse.getVnp_TransactionNo()
            );
        } else {
            return RefundResult.failure(
                vnpayResponse.getVnp_Message() != null ? 
                vnpayResponse.getVnp_Message() : "Refund processing failed"
            );
        }
    }
    
    /**
     * Convert VNPay query response to domain payment status
     */
    private PaymentStatus adaptVNPayStatusToDomain(QueryResponse queryResponse) {
        if (queryResponse == null || queryResponse.getVnp_ResponseCode() == null) {
            return PaymentStatus.FAILED;
        }
        
        switch (queryResponse.getVnp_ResponseCode()) {
            case "00":
                return PaymentStatus.SUCCESS;
            case "01", "02":
                return PaymentStatus.PROCESSING;
            case "04":
                return PaymentStatus.REFUNDED;
            case "05", "06", "07":
                return PaymentStatus.FAILED;
            default:
                return PaymentStatus.PENDING;
        }
    }
}