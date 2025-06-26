package com.aims.application.services;

import com.aims.presentation.boundary.PaymentBoundary;
import com.aims.presentation.dto.payment.PaymentRequest;
import com.aims.presentation.dto.payment.PaymentResponse;
import com.aims.presentation.dto.payment.PaymentStatusResponse;
import com.aims.presentation.dto.payment.RefundRequest;
import com.aims.presentation.dto.payment.RefundResponse;
import com.aims.application.dto.PaymentCallbackRequest;
import com.aims.application.dto.PaymentInitiationRequest;
import com.aims.domain.payment.entity.PaymentTransaction;
import com.aims.domain.payment.repository.PaymentTransactionRepository;
import com.aims.domain.order.service.OrderService;
import com.aims.presentation.boundary.NotificationBoundary;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Payment Application Service
 * 
 * Orchestrates payment flows according to sequence diagrams.
 * Handles payment initiation, callback processing, and refunds.
 * Integrates with Order and Notification services.
 */
@Service
@Transactional
public class PaymentApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentApplicationService.class);

    private final PaymentBoundary paymentBoundary;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OrderService orderService;
    private final NotificationBoundary notificationBoundary;

    @Autowired
    public PaymentApplicationService(
            @Qualifier("vnpayBoundary") PaymentBoundary paymentBoundary,
            PaymentTransactionRepository paymentTransactionRepository,
            @Qualifier("orderServiceImpl") OrderService orderService,
            NotificationBoundary notificationBoundary) {
        this.paymentBoundary = paymentBoundary;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.orderService = orderService;
        this.notificationBoundary = notificationBoundary;
    }

    /**
     * Initiates a payment transaction
     * Based on "Pay Order" workflow from sequence diagrams
     */
    public PaymentResponse initiatePayment(PaymentInitiationRequest request, HttpServletRequest servletRequest) {
        logger.info("Initiating payment for order: {}", request.getOrderId());
        
        try {
            // 1. Validate request
            if (request.getAmount() == null || request.getAmount() <= 0) {
                throw new IllegalArgumentException("Amount must be positive");
            }
            
            // 2. Validate order exists and can be paid
            if (!orderService.canProcessPayment(request.getOrderId())) {
                logger.warn("Order cannot be paid: {}", request.getOrderId());
                return PaymentResponse.failure("01", "Order cannot be paid");
            }

            // 3. Create payment transaction record
            PaymentTransaction transaction = createPaymentTransaction(request);
            paymentTransactionRepository.save(transaction);

            // 4. Convert to boundary request
            PaymentRequest paymentRequest = mapToPaymentRequest(request);

            // 5. Call payment boundary (VNPay)
            PaymentResponse response = paymentBoundary.initiatePayment(paymentRequest, servletRequest);

            // 6. Update transaction with response details
            if (response.isSuccess()) {
                // Store a simple success message instead of the long payment URL
                transaction.setGatewayResponseMessage("Payment initiated successfully");
                transaction.setTransactionStatus(PaymentTransaction.TransactionStatus.PENDING);
                paymentTransactionRepository.save(transaction);
                
                logger.info("Payment initiated successfully for order: {}", request.getOrderId());
            } else {
                transaction.setTransactionStatus(PaymentTransaction.TransactionStatus.FAILED);
                transaction.setGatewayResponseMessage(response.getMessage());
                paymentTransactionRepository.save(transaction);
                
                logger.warn("Payment initiation failed for order: {} - {}", 
                    request.getOrderId(), response.getMessage());
            }

            return response;

        } catch (IllegalArgumentException e) {
            // Re-throw validation exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Error initiating payment for order: {}", request.getOrderId(), e);
            return PaymentResponse.failure("99", "Payment initiation failed");
        }
    }

    /**
     * Handles payment callback from payment provider
     * Processes IPN (Instant Payment Notification) 
     */
    public void handlePaymentCallback(PaymentCallbackRequest callbackRequest) {
        logger.info("Processing payment callback for transaction: {}", callbackRequest.getTransactionId());
        
        try {
            // 1. Validate callback authenticity
            if (!paymentBoundary.validatePaymentCallback(callbackRequest.getParams())) {
                logger.warn("Invalid payment callback for transaction: {}", callbackRequest.getTransactionId());
                return;
            }

            // 2. Find existing transaction
            PaymentTransaction transaction = paymentTransactionRepository
                .findByOrderId(callbackRequest.getTransactionId())
                .orElse(null);

            if (transaction == null) {
                logger.warn("Payment transaction not found: {}", callbackRequest.getTransactionId());
                return;
            }

            // 3. Update transaction status
            String responseCode = callbackRequest.getParams().get("vnp_ResponseCode");
            if ("00".equals(responseCode)) {
                // Payment successful
                String vnpayTransactionNo = callbackRequest.getParams().get("vnp_TransactionNo");
                String payDate = callbackRequest.getParams().get("vnp_PayDate");
                transaction.markAsSuccessful(vnpayTransactionNo, payDate);
                
                // Set additional fields
                transaction.setBankCode(callbackRequest.getParams().get("vnp_BankCode"));

                // Update order status
                orderService.markOrderAsPaid(callbackRequest.getTransactionId());

                // Send success notification
                notificationBoundary.sendPaymentSuccessNotification(
                    callbackRequest.getTransactionId(), 
                    transaction.getAmount()
                );

                logger.info("Payment completed successfully for transaction: {}", callbackRequest.getTransactionId());

            } else {
                // Payment failed
                transaction.markAsFailed(responseCode, "Payment failed with code: " + responseCode);

                // Update order status
                orderService.markOrderPaymentFailed(callbackRequest.getTransactionId());

                // Send failure notification
                notificationBoundary.sendPaymentFailureNotification(
                    callbackRequest.getTransactionId(),
                    responseCode
                );

                logger.warn("Payment failed for transaction: {} with code: {}", 
                    callbackRequest.getTransactionId(), responseCode);
            }

            paymentTransactionRepository.save(transaction);

        } catch (Exception e) {
            logger.error("Error processing payment callback for transaction: {}", 
                callbackRequest.getTransactionId(), e);
        }
    }

    /**
     * Processes a refund request
     * Used for cancelled orders
     */
    public RefundResponse processRefund(RefundRequest request, HttpServletRequest servletRequest) {
        logger.info("Processing refund for order: {}", request.getOrderId());
        
        try {
            // 1. Find original payment transaction
            PaymentTransaction originalTransaction = paymentTransactionRepository
                .findByOrderId(request.getOrderId())
                .orElse(null);

            if (originalTransaction == null || !originalTransaction.isSuccessful()) {
                logger.warn("Original payment transaction not found or not successful: {}", request.getOrderId());
                return RefundResponse.failure("01", "Original payment not found or not successful");
            }

            // 2. Check if already refunded
            if (originalTransaction.getTransactionStatus() == PaymentTransaction.TransactionStatus.REFUNDED) {
                logger.warn("Payment already refunded for order: {}", request.getOrderId());
                return RefundResponse.failure("02", "Payment already refunded");
            }

            // 3. Process refund through payment boundary
            RefundResponse response = paymentBoundary.processRefund(request, servletRequest);

            // 4. Update transaction status
            if (response.isSuccess()) {
                originalTransaction.markAsRefunded();
                paymentTransactionRepository.save(originalTransaction);

                // Update order status
                orderService.markOrderAsRefunded(request.getOrderId());

                // Send refund notification
                notificationBoundary.sendRefundNotification(
                    request.getOrderId(),
                    request.getAmount()
                );

                logger.info("Refund processed successfully for order: {}", request.getOrderId());
            } else {
                logger.warn("Refund processing failed for order: {} - {}", 
                    request.getOrderId(), response.getMessage());
            }

            return response;

        } catch (Exception e) {
            logger.error("Error processing refund for order: {}", request.getOrderId(), e);
            return RefundResponse.failure("99", "Refund processing failed");
        }
    }

    /**
     * Queries payment status
     */
    public PaymentStatusResponse queryPaymentStatus(String transactionId, String transactionDate, 
                                                   HttpServletRequest servletRequest) {
        logger.info("Querying payment status for transaction: {}", transactionId);
        
        try {
            // Call payment boundary for status
            PaymentStatusResponse response = paymentBoundary.checkPaymentStatus(
                transactionId, transactionDate, servletRequest);

            // Update local transaction record if needed
            if (response.isSuccess()) {
                paymentTransactionRepository.findByOrderId(transactionId)
                    .ifPresent(transaction -> {
                        if (!transaction.isSuccessful() && 
                            "00".equals(response.getTransactionStatus())) {
                            transaction.markAsSuccessful(
                                response.getVnpayTransactionNo(),
                                response.getPayDate()
                            );
                            transaction.setBankCode(response.getBankCode());
                            paymentTransactionRepository.save(transaction);

                            // Update order status
                            orderService.markOrderAsPaid(transactionId);
                        }
                    });
            }

            return response;

        } catch (Exception e) {
            logger.error("Error querying payment status for transaction: {}", transactionId, e);
            return PaymentStatusResponse.failure("99", "Status query failed");
        }
    }

    // Private helper methods
    private PaymentTransaction createPaymentTransaction(PaymentInitiationRequest request) {
        return PaymentTransaction.builder()
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .bankCode(request.getBankCode())
                .paymentMethod(paymentBoundary.getProviderName())
                .transactionStatus(PaymentTransaction.TransactionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private PaymentRequest mapToPaymentRequest(PaymentInitiationRequest request) {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId(request.getOrderId());
        paymentRequest.setAmount(request.getAmount());
        paymentRequest.setOrderInfo(request.getOrderInfo());
        paymentRequest.setBankCode(request.getBankCode());
        paymentRequest.setLanguage(request.getLanguage());
        paymentRequest.setReturnUrl(request.getReturnUrl());
        return paymentRequest;
    }
}
