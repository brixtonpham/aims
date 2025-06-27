package com.aims.application.payment;

import com.aims.application.commands.PaymentCommand;
import com.aims.application.commands.RefundCommand;
import com.aims.application.dto.payment.PaymentResult;
import com.aims.application.dto.payment.RefundResult;
import com.aims.domain.notification.service.NotificationService;
import com.aims.domain.payment.service.PaymentDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PaymentApplicationService.
 * Tests the application service layer orchestration and cross-cutting concerns.
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 1 - Foundation
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentApplicationService Unit Tests")
class PaymentApplicationServiceTest {
    
    @Mock
    private PaymentDomainService paymentDomainService;
    
    @Mock
    private NotificationService notificationService;
    
    @InjectMocks
    private PaymentApplicationService paymentApplicationService;
    
    private PaymentCommand paymentCommand;
    private RefundCommand refundCommand;
    
    @BeforeEach
    void setUp() {
        paymentCommand = PaymentCommand.builder()
                .orderId("ORD-001")
                .customerId("CUST-001")
                .amount(150000.0)
                .currency("VND")
                .paymentMethod("VNPAY")
                .returnUrl("http://localhost:3000/payment/return")
                .customerIpAddress("192.168.1.100")
                .description("Payment for order ORD-001")
                .metadata("test-metadata")
                .build();
        
        refundCommand = RefundCommand.builder()
                .transactionId("TXN-001")
                .orderId("ORD-001")
                .customerId("CUST-001")
                .refundAmount(150000.0)
                .currency("VND")
                .refundReason("Customer requested refund")
                .additionalNotes("Test refund")
                .build();
    }
    
    @Test
    @DisplayName("Should successfully process VNPAY payment")
    void shouldProcessVNPayPaymentSuccessfully() {
        // When
        PaymentResult result = paymentApplicationService.processPayment(paymentCommand);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getTransactionId());
        assertTrue(result.getTransactionId().startsWith("TXN-"));
        assertEquals("ORD-001", result.getOrderId());
        assertEquals("PENDING", result.getPaymentStatus());
        assertEquals(150000.0, result.getAmount());
        assertEquals("VND", result.getCurrency());
        assertNotNull(result.getPaymentUrl());
        assertTrue(result.getPaymentUrl().contains("vnpayment.vn"));
        assertEquals("Payment processed successfully", result.getMessage());
    }
    
    @Test
    @DisplayName("Should successfully process COD payment")
    void shouldProcessCODPaymentSuccessfully() {
        // Given
        paymentCommand.setPaymentMethod("COD");
        
        // When
        PaymentResult result = paymentApplicationService.processPayment(paymentCommand);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getTransactionId());
        assertTrue(result.getTransactionId().startsWith("TXN-"));
        assertEquals("ORD-001", result.getOrderId());
        assertEquals("PENDING", result.getPaymentStatus());
        assertEquals(150000.0, result.getAmount());
        assertEquals("VND", result.getCurrency());
        assertNull(result.getPaymentUrl()); // No payment URL for COD
        assertEquals("Payment processed successfully", result.getMessage());
    }
    
    @Test
    @DisplayName("Should handle payment processing failure gracefully")
    void shouldHandlePaymentProcessingFailure() {
        // Given - invalid amount to trigger validation failure
        paymentCommand.setAmount(-100.0);
        
        // When
        PaymentResult result = paymentApplicationService.processPayment(paymentCommand);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("PAYMENT_PROCESSING_FAILED", result.getErrorCode());
        assertNotNull(result.getMessage());
        assertTrue(result.getMessage().contains("Failed to process payment"));
    }
    
    @Test
    @DisplayName("Should successfully process refund")
    void shouldProcessRefundSuccessfully() {
        // When
        RefundResult result = paymentApplicationService.processRefund(refundCommand);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getRefundTransactionId());
        assertTrue(result.getRefundTransactionId().startsWith("RFD-"));
        assertEquals("TXN-001", result.getOriginalTransactionId());
        assertEquals("ORD-001", result.getOrderId());
        assertEquals("PENDING", result.getRefundStatus());
        assertEquals(150000.0, result.getRefundAmount());
        assertEquals("VND", result.getCurrency());
        assertNotNull(result.getProcessedAt());
        assertNotNull(result.getExpectedCompletion());
        assertEquals("Refund processed successfully", result.getMessage());
    }
    
    @Test
    @DisplayName("Should handle refund processing failure gracefully")
    void shouldHandleRefundProcessingFailure() {
        // Given - invalid refund amount to trigger validation failure
        refundCommand.setRefundAmount(-50.0);
        
        // When
        RefundResult result = paymentApplicationService.processRefund(refundCommand);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("REFUND_PROCESSING_FAILED", result.getErrorCode());
        assertNotNull(result.getMessage());
        assertTrue(result.getMessage().contains("Failed to process refund"));
    }
    
    @Test
    @DisplayName("Should successfully get payment status")
    void shouldGetPaymentStatusSuccessfully() {
        // Given
        String transactionId = "TXN-001";
        
        // When
        PaymentResult result = paymentApplicationService.getPaymentStatus(transactionId);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(transactionId, result.getTransactionId());
        assertEquals("COMPLETED", result.getPaymentStatus());
        assertNotNull(result.getAmount());
        assertNotNull(result.getCurrency());
    }
    
    @Test
    @DisplayName("Should handle payment status retrieval failure gracefully")
    void shouldHandlePaymentStatusRetrievalFailure() {
        // Given - null transaction ID to trigger failure
        String transactionId = null;
        
        // When
        PaymentResult result = paymentApplicationService.getPaymentStatus(transactionId);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("PAYMENT_STATUS_FAILED", result.getErrorCode());
        assertNotNull(result.getMessage());
        assertTrue(result.getMessage().contains("Failed to get payment status"));
    }
    
    @Test
    @DisplayName("Should validate payment command properly")
    void shouldValidatePaymentCommand() {
        // Test positive amount validation
        paymentCommand.setAmount(-100.0);
        PaymentResult result = paymentApplicationService.processPayment(paymentCommand);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Payment amount must be positive"));
        
        // Test currency validation
        paymentCommand.setAmount(100000.0);
        paymentCommand.setCurrency("INVALID");
        result = paymentApplicationService.processPayment(paymentCommand);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Unsupported currency"));
    }
    
    @Test
    @DisplayName("Should validate refund command properly")
    void shouldValidateRefundCommand() {
        // Test positive refund amount validation
        refundCommand.setRefundAmount(-50.0);
        RefundResult result = paymentApplicationService.processRefund(refundCommand);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Refund amount must be positive"));
    }
    
    @Test
    @DisplayName("Should generate payment URL correctly for VNPAY")
    void shouldGeneratePaymentUrlForVNPay() {
        // Given
        paymentCommand.setPaymentMethod("VNPAY");
        paymentCommand.setOrderId("ORD-123");
        paymentCommand.setAmount(100000.0);
        
        // When
        PaymentResult result = paymentApplicationService.processPayment(paymentCommand);
        
        // Then
        assertNotNull(result.getPaymentUrl());
        assertTrue(result.getPaymentUrl().contains("sandbox.vnpayment.vn"));
        assertTrue(result.getPaymentUrl().contains("ORD-123"));
        assertTrue(result.getPaymentUrl().contains("10000000")); // 100000 * 100
    }
    
    @Test
    @DisplayName("Should handle supported currencies correctly")
    void shouldHandleSupportedCurrencies() {
        // Test VND currency
        paymentCommand.setCurrency("VND");
        PaymentResult vndResult = paymentApplicationService.processPayment(paymentCommand);
        assertTrue(vndResult.isSuccess());
        
        // Test USD currency
        paymentCommand.setCurrency("USD");
        PaymentResult usdResult = paymentApplicationService.processPayment(paymentCommand);
        assertTrue(usdResult.isSuccess());
        
        // Test unsupported currency
        paymentCommand.setCurrency("EUR");
        PaymentResult eurResult = paymentApplicationService.processPayment(paymentCommand);
        assertFalse(eurResult.isSuccess());
        assertTrue(eurResult.getMessage().contains("Unsupported currency"));
    }
}
