package com.aims.presentation.web;

import com.aims.application.payment.PaymentApplicationService;
import com.aims.domain.payment.model.PaymentMethod;
import com.aims.domain.payment.model.PaymentResult;
import com.aims.domain.payment.model.RefundResult;
import com.aims.presentation.dto.ApiResponse;
import com.aims.vnpay.common.entity.TransactionInfo;
import com.aims.vnpay.common.repository.TransactionRepository;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * REST Controller for Payment operations
 * Unified controller following RESTful design principles and Clean Architecture
 * Delegates to PaymentApplicationService and VNPayController to preserve existing VNPay functionality
 * 
 * Phase 4: Controller Migration & Testing
 * Following Task 4.1 requirements for new Clean Architecture controllers
 */
@RestController
@RequestMapping("/api/v2/payments")
@CrossOrigin(origins = "http://localhost:3000")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    private static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    private static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    
    // Request field constants
    private static final String ORDER_ID = "orderId";
    private static final String AMOUNT = "amount";
    private static final String CUSTOMER_ID = "customerId";
    private static final String TRANSACTION_ID = "transactionId";
    private static final String MESSAGE = "message";
    private static final String REASON = "reason";
    private static final String STATUS = "status";

    private final PaymentApplicationService paymentApplicationService;
    private final TransactionRepository transactionRepository;
    
    // Inject VNPayController for delegation to preserve existing functionality
    private final com.aims.vnpay.common.controller.VNPayController vnpayController;

    @Autowired
    public PaymentController(PaymentApplicationService paymentApplicationService,
                           TransactionRepository transactionRepository,
                           com.aims.vnpay.common.controller.VNPayController vnpayController) {
        this.paymentApplicationService = paymentApplicationService;
        this.transactionRepository = transactionRepository;
        this.vnpayController = vnpayController;
    }

    /**
     * Create VNPay payment
     * POST /api/v2/payments/vnpay
     */
    @PostMapping("/vnpay")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createVNPayPayment(
            @RequestBody Map<String, Object> paymentRequest,
            HttpServletRequest request) {
        try {
            // Validate request
            validateVNPayPaymentRequest(paymentRequest);

            // Extract parameters
            String orderId = (String) paymentRequest.get(ORDER_ID);
            Long amount = paymentRequest.get(AMOUNT) instanceof Integer integer ? 
                         integer.longValue() : 
                         (Long) paymentRequest.get(AMOUNT);
            String customerId = (String) paymentRequest.get(CUSTOMER_ID);

            // Process payment through application service (which uses VNPay adapter)
            PaymentResult result = paymentApplicationService.processOrderPayment(
                orderId, amount, PaymentMethod.VNPAY, customerId, request);

            if (result.isSuccess()) {
                Map<String, Object> response = Map.of(
                    "success", true,
                    "paymentUrl", result.getPaymentUrl(),
                    TRANSACTION_ID, result.getTransactionId(),
                    MESSAGE, result.getMessage()
                );
                
                return ResponseEntity.ok(ApiResponse.success("Payment URL created successfully", response));
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(result.getMessage(), result.getErrorCode()));
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), VALIDATION_ERROR));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to create payment", INTERNAL_ERROR));
        }
    }

    /**
     * Handle VNPay callback
     * POST /api/v2/payments/vnpay/callback
     * Delegates to existing VNPayController to preserve functionality
     */
    @PostMapping("/vnpay/callback")
    public ResponseEntity<Map<String, Object>> handleVNPayCallback(
            @RequestParam Map<String, String> params,
            HttpServletRequest request) {
        try {
            // Delegate to existing VNPayController to preserve functionality
            return vnpayController.returnPage(params, request);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    STATUS, "ERROR",
                    MESSAGE, "Error processing VNPay callback",
                    "error", e.getMessage()
                ));
        }
    }

    /**
     * Process refund
     * POST /api/v2/payments/refund
     */
    @PostMapping("/refund")
    public ResponseEntity<ApiResponse<Map<String, Object>>> processRefund(
            @RequestBody Map<String, Object> refundRequest,
            HttpServletRequest request) {
        try {
            // Validate request
            validateRefundRequest(refundRequest);

            // Extract parameters
            String orderId = (String) refundRequest.get(ORDER_ID);
            String transactionId = (String) refundRequest.get(TRANSACTION_ID);
            Long amount = refundRequest.get(AMOUNT) instanceof Integer integer ? 
                         integer.longValue() : 
                         (Long) refundRequest.get(AMOUNT);
            String reason = (String) refundRequest.getOrDefault(REASON, "Customer request");

            // Process refund through application service
            RefundResult result = paymentApplicationService.processOrderRefund(
                orderId, transactionId, amount, reason, request);

            if (result.isSuccess()) {
                Map<String, Object> response = Map.of(
                    "success", true,
                    "refundId", result.getRefundId(),
                    TRANSACTION_ID, result.getTransactionId(),
                    AMOUNT, result.getAmount(),
                    MESSAGE, result.getMessage()
                );
                
                return ResponseEntity.ok(ApiResponse.success("Refund processed successfully", response));
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(result.getMessage(), "REFUND_FAILED"));
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), VALIDATION_ERROR));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to process refund", INTERNAL_ERROR));
        }
    }

    /**
     * Get payment status
     * GET /api/v2/payments/{transactionId}/status
     */
    @GetMapping("/{transactionId}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPaymentStatus(
            @PathVariable("transactionId") String transactionId) {
        try {
            // Validate transaction ID
            if (transactionId == null || transactionId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Transaction ID is required", VALIDATION_ERROR));
            }

            // Try to get real payment status from the transaction repository
            Map<String, Object> statusInfo = getPaymentStatusFromRepository(transactionId);
            
            return ResponseEntity.ok(ApiResponse.success("Payment status retrieved successfully", statusInfo));

        } catch (Exception e) {
            log.error("Failed to get payment status for transaction: {}", transactionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to get payment status", INTERNAL_ERROR));
        }
    }
    
    /**
     * Create mock payment status for Phase 4 implementation
     * This will be replaced with real implementation in future phases
     */
    private Map<String, Object> getPaymentStatusFromRepository(String transactionId) {
        // First try to find real transaction data
        TransactionInfo transaction = transactionRepository.findByOrderId(transactionId);
        
        Map<String, Object> statusInfo = new java.util.HashMap<>();
        
        if (transaction != null) {
            // Return real transaction information
            statusInfo.put(TRANSACTION_ID, transactionId);
            statusInfo.put(STATUS, "COMPLETED"); // Assuming completed if found
            statusInfo.put("statusDescription", "Payment completed successfully");
            statusInfo.put(AMOUNT, transaction.getAmount());
            statusInfo.put("currency", "VND");
            statusInfo.put("createdAt", transaction.getCreatedAt() != null ? 
                transaction.getCreatedAt().toString() : java.time.LocalDateTime.now().toString());
            statusInfo.put(ORDER_ID, transaction.getOrderId());
            statusInfo.put("paymentMethod", "VNPAY");
            statusInfo.put("transactionNo", transaction.getTransactionNo());
            statusInfo.put("responseCode", transaction.getResponseCode());
            statusInfo.put("transactionStatus", transaction.getTransactionStatus());
        } else {
            // Return default status if transaction not found
            statusInfo.put(TRANSACTION_ID, transactionId);
            statusInfo.put(STATUS, "NOT_FOUND");
            statusInfo.put("statusDescription", "Transaction not found or pending");
            statusInfo.put(AMOUNT, 0L);
            statusInfo.put("currency", "VND");
            statusInfo.put("createdAt", java.time.LocalDateTime.now().toString());
            statusInfo.put(ORDER_ID, "ORD-" + transactionId.substring(Math.max(0, transactionId.length() - 6)));
        }
        
        return statusInfo;
    }

    // Private validation methods

    private void validateVNPayPaymentRequest(Map<String, Object> request) {
        if (request.get(ORDER_ID) == null || ((String) request.get(ORDER_ID)).trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID is required");
        }
        if (request.get(AMOUNT) == null) {
            throw new IllegalArgumentException("Amount is required");
        }
        if (request.get(CUSTOMER_ID) == null || ((String) request.get(CUSTOMER_ID)).trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID is required");
        }
    }

    private void validateRefundRequest(Map<String, Object> request) {
        if (request.get(ORDER_ID) == null || ((String) request.get(ORDER_ID)).trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID is required");
        }
        if (request.get(TRANSACTION_ID) == null || ((String) request.get(TRANSACTION_ID)).trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction ID is required");
        }
        if (request.get(AMOUNT) == null) {
            throw new IllegalArgumentException("Refund amount is required");
        }
    }
}
