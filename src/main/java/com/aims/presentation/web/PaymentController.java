package com.aims.presentation.web;

import com.aims.application.dto.PaymentInitiationRequest;
import com.aims.application.dto.PaymentCallbackRequest;
import com.aims.application.services.PaymentApplicationService;
import com.aims.presentation.dto.payment.PaymentResponse;
import com.aims.presentation.dto.payment.PaymentStatusResponse;
import com.aims.presentation.dto.payment.RefundRequest;
import com.aims.presentation.dto.payment.RefundResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Payment Controller
 * 
 * Refactored controller following Clean Architecture principles.
 * Thin controller that delegates to PaymentApplicationService.
 * Handles all payment-related HTTP endpoints.
 */
@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "http://localhost:3000")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentApplicationService paymentApplicationService;

    @Autowired
    public PaymentController(PaymentApplicationService paymentApplicationService) {
        this.paymentApplicationService = paymentApplicationService;
    }

    /**
     * Initiate payment for an order
     * Based on "Pay Order" workflow from sequence diagrams
     */
    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponse> initiatePayment(
            @Valid @RequestBody PaymentInitiationRequest request,
            HttpServletRequest servletRequest) {
        
        logger.info("Payment initiation request received for order: {}", request.getOrderId());
        
        try {
            PaymentResponse response = paymentApplicationService.initiatePayment(request, servletRequest);
            
            if (response.isSuccess()) {
                logger.info("Payment initiated successfully for order: {}", request.getOrderId());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Payment initiation failed for order: {} - {}", 
                    request.getOrderId(), response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error initiating payment for order: {}", request.getOrderId(), e);
            PaymentResponse errorResponse = PaymentResponse.failure("99", "Internal server error");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Handle payment callback/notification from payment provider
     * Processes IPN (Instant Payment Notification)
     */
    @PostMapping("/callback")
    public ResponseEntity<Map<String, Object>> handlePaymentCallback(
            @RequestParam Map<String, String> params,
            HttpServletRequest servletRequest) {
        
        String transactionId = params.get("vnp_TxnRef");
        logger.info("Payment callback received for transaction: {}", transactionId);
        
        try {
            PaymentCallbackRequest callbackRequest = new PaymentCallbackRequest(transactionId, params);
            callbackRequest.setIpAddress(servletRequest.getRemoteAddr());
            
            paymentApplicationService.handlePaymentCallback(callbackRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("RspCode", "00");
            response.put("Message", "Success");
            
            logger.info("Payment callback processed successfully for transaction: {}", transactionId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing payment callback for transaction: {}", transactionId, e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("RspCode", "99");
            response.put("Message", "Error processing callback");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Query payment status
     * Check the current status of a payment transaction
     */
    @GetMapping("/status/{transactionId}")
    public ResponseEntity<PaymentStatusResponse> queryPaymentStatus(
            @PathVariable String transactionId,
            @RequestParam String transactionDate,
            HttpServletRequest servletRequest) {
        
        logger.info("Payment status query for transaction: {}", transactionId);
        
        try {
            PaymentStatusResponse response = paymentApplicationService.queryPaymentStatus(
                transactionId, transactionDate, servletRequest);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error querying payment status for transaction: {}", transactionId, e);
            PaymentStatusResponse errorResponse = PaymentStatusResponse.failure("99", "Status query failed");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Process refund request
     * Used for cancelled orders
     */
    @PostMapping("/refund")
    public ResponseEntity<RefundResponse> processRefund(
            @Valid @RequestBody RefundRequest request,
            HttpServletRequest servletRequest) {
        
        logger.info("Refund request received for order: {}", request.getOrderId());
        
        try {
            RefundResponse response = paymentApplicationService.processRefund(request, servletRequest);
            
            if (response.isSuccess()) {
                logger.info("Refund processed successfully for order: {}", request.getOrderId());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Refund processing failed for order: {} - {}", 
                    request.getOrderId(), response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Error processing refund for order: {}", request.getOrderId(), e);
            RefundResponse errorResponse = RefundResponse.failure("99", "Internal server error");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Legacy endpoint for backward compatibility
     * Maps to new payment initiation endpoint
     */
    @PostMapping("/payment")
    public ResponseEntity<PaymentResponse> createPaymentLegacy(
            @RequestBody Map<String, Object> paymentData,
            HttpServletRequest servletRequest) {
        
        logger.info("Legacy payment request received");
        
        try {
            // Convert legacy request to new format
            PaymentInitiationRequest request = convertLegacyRequest(paymentData);
            
            return initiatePayment(request, servletRequest);
            
        } catch (Exception e) {
            logger.error("Error processing legacy payment request", e);
            PaymentResponse errorResponse = PaymentResponse.failure("99", "Invalid request format");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Private helper methods
    private PaymentInitiationRequest convertLegacyRequest(Map<String, Object> paymentData) {
        PaymentInitiationRequest request = new PaymentInitiationRequest();
        
        // Extract data from legacy format
        String amount = paymentData.getOrDefault("amount", "").toString();
        String language = paymentData.getOrDefault("language", "vn").toString();
        String orderId = paymentData.getOrDefault("orderId", "").toString();
        String bankCode = paymentData.getOrDefault("bankCode", "").toString();
        
        request.setOrderId(orderId);
        request.setAmount(Long.parseLong(amount));
        request.setLanguage(language);
        request.setBankCode(bankCode);
        request.setOrderInfo("Payment for order: " + orderId);
        
        return request;
    }
}
