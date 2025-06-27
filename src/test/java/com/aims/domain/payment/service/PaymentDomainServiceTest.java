package com.aims.domain.payment.service;

import com.aims.domain.payment.dto.*;
import com.aims.domain.payment.entity.PaymentStatus;
import com.aims.domain.payment.exception.PaymentDomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PaymentDomainService interface and related domain objects.
 * Validates that all payment domain concepts are properly defined and accessible.
 * 
 * This test serves as validation for Task 1.1 completion.
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 1 - Foundation
 */
class PaymentDomainServiceTest {

    @Test
    @DisplayName("Should validate PaymentRequest domain object structure")
    void testPaymentRequestStructure() {
        // Given: A valid payment request
        PaymentRequest request = new PaymentRequest();
        request.setOrderId("ORDER123");
        request.setAmount(100000.0);
        request.setPaymentMethod("VNPAY");
        request.setCustomerId("CUST001");
        request.setCustomerName("John Doe");
        request.setCustomerEmail("john.doe@example.com");
        
        // When: Validating the request
        boolean isValid = request.isValid();
        
        // Then: Request should be valid
        assertTrue(isValid, "PaymentRequest should be valid with all required fields");
        assertEquals("ORDER123", request.getOrderId());
        assertEquals(100000.0, request.getAmount());
        assertEquals("VNPAY", request.getPaymentMethod());
        assertEquals(10000000L, request.getAmountInCents());
        assertFalse(request.isHighValueTransaction(), "100k VND should not be high value");
        
        // Test high value transaction
        request.setAmount(15000000.0);
        assertTrue(request.isHighValueTransaction(), "15M VND should be high value");
    }

    @Test
    @DisplayName("Should validate PaymentStatus enum functionality")
    void testPaymentStatusEnum() {
        // Given: Different payment statuses
        PaymentStatus pending = PaymentStatus.PENDING;
        PaymentStatus authorized = PaymentStatus.AUTHORIZED;
        PaymentStatus completed = PaymentStatus.COMPLETED;
        PaymentStatus failed = PaymentStatus.FAILED;
        PaymentStatus cancelled = PaymentStatus.CANCELLED;
        PaymentStatus refunded = PaymentStatus.REFUNDED;
        
        // When & Then: Test status properties
        assertTrue(pending.isCancellable(), "PENDING payments should be cancellable");
        assertTrue(authorized.isCancellable(), "AUTHORIZED payments should be cancellable");
        assertFalse(completed.isCancellable(), "COMPLETED payments should not be cancellable");
        
        assertTrue(completed.isRefundable(), "COMPLETED payments should be refundable");
        assertFalse(pending.isRefundable(), "PENDING payments should not be refundable");
        
        assertTrue(completed.isSuccessful(), "COMPLETED status indicates success");
        assertFalse(failed.isSuccessful(), "FAILED status does not indicate success");
        
        assertTrue(failed.isFailed(), "FAILED status indicates failure");
        assertTrue(cancelled.isFailed(), "CANCELLED status indicates failure");
        assertFalse(completed.isFailed(), "COMPLETED status does not indicate failure");
        
        assertTrue(completed.isTerminal(), "COMPLETED is a terminal status");
        assertTrue(failed.isTerminal(), "FAILED is a terminal status");
        assertFalse(pending.isTerminal(), "PENDING is not a terminal status");
        
        // Test valid transitions
        assertTrue(pending.canTransitionTo(authorized), "PENDING should transition to AUTHORIZED");
        assertTrue(pending.canTransitionTo(failed), "PENDING should transition to FAILED");
        assertTrue(completed.canTransitionTo(refunded), "COMPLETED should transition to REFUNDED");
        assertFalse(failed.canTransitionTo(completed), "FAILED should not transition to COMPLETED");
    }

    @Test
    @DisplayName("Should validate PaymentDomainException functionality")
    void testPaymentDomainException() {
        // Given: Different exception scenarios
        String transactionId = "TXN123";
        String paymentMethod = "UNKNOWN_METHOD";
        
        // When: Creating specific exceptions
        PaymentDomainException notFoundEx = PaymentDomainException.paymentNotFound(transactionId);
        PaymentDomainException notRefundableEx = PaymentDomainException.paymentNotRefundable(transactionId, "FAILED");
        PaymentDomainException methodNotSupportedEx = PaymentDomainException.paymentMethodNotSupported(paymentMethod);
        PaymentDomainException fraudEx = PaymentDomainException.fraudDetected(transactionId, "Suspicious activity");
        
        // Then: Exceptions should have proper error codes and messages
        assertEquals("PAYMENT_NOT_FOUND", notFoundEx.getErrorCode());
        assertTrue(notFoundEx.getMessage().contains(transactionId));
        
        assertEquals("PAYMENT_NOT_REFUNDABLE", notRefundableEx.getErrorCode());
        assertTrue(notRefundableEx.getMessage().contains("FAILED"));
        
        assertEquals("PAYMENT_METHOD_NOT_SUPPORTED", methodNotSupportedEx.getErrorCode());
        assertTrue(methodNotSupportedEx.getMessage().contains(paymentMethod));
        
        assertEquals("FRAUD_DETECTED", fraudEx.getErrorCode());
        assertTrue(fraudEx.getMessage().contains("Suspicious activity"));
    }

    @Test
    @DisplayName("Should validate PaymentResult domain object")
    void testPaymentResultStructure() {
        // Given: Successful payment result
        PaymentResult successResult = PaymentResult.success("TXN123", "https://payment.example.com");
        
        // When & Then: Test successful result
        assertTrue(successResult.isSuccessful(), "Result should be successful");
        assertTrue(successResult.requiresCustomerAction(), "Result should require customer action");
        assertEquals("TXN123", successResult.getTransactionId());
        assertEquals("https://payment.example.com", successResult.getPaymentUrl());
        assertEquals(PaymentStatus.PENDING, successResult.getStatus());
        
        // Given: Failed payment result
        PaymentResult failureResult = PaymentResult.failure("INVALID_AMOUNT", "Amount must be positive");
        
        // When & Then: Test failed result
        assertFalse(failureResult.isSuccessful(), "Result should not be successful");
        assertFalse(failureResult.requiresCustomerAction(), "Failed result should not require customer action");
        assertEquals("INVALID_AMOUNT", failureResult.getErrorCode());
        assertEquals("Amount must be positive", failureResult.getErrorMessage());
        assertEquals(PaymentStatus.FAILED, failureResult.getStatus());
    }

    @Test
    @DisplayName("Should validate RefundRequest and RefundResult")
    void testRefundDomainObjects() {
        // Given: A refund request
        RefundRequest refundRequest = new RefundRequest("TXN123", 50000.0, "Customer requested");
        
        // When & Then: Test refund request
        assertTrue(refundRequest.isValid(), "RefundRequest should be valid with required fields");
        assertTrue(refundRequest.isFullRefund(), "Default refund type should be FULL");
        assertFalse(refundRequest.isPartialRefund(), "Default should not be partial refund");
        
        // Test partial refund
        refundRequest.setRefundType("PARTIAL");
        assertFalse(refundRequest.isFullRefund(), "Should not be full refund when set to PARTIAL");
        assertTrue(refundRequest.isPartialRefund(), "Should be partial refund when set to PARTIAL");
        
        // Given: Successful refund result
        RefundResult successResult = RefundResult.success("REFUND123", 50000.0);
        successResult.setOriginalAmount(100000.0);
        
        // When & Then: Test refund result
        assertTrue(successResult.isSuccessful(), "Refund result should be successful");
        assertFalse(successResult.isFullRefund(), "50k out of 100k should not be full refund");
        assertTrue(successResult.isPartialRefund(), "50k out of 100k should be partial refund");
        assertEquals(50.0, successResult.getRefundPercentage(), "Should be 50% refund");
    }

    @Test
    @DisplayName("Should validate domain service interface contract")
    void testDomainServiceInterfaceContract() {
        // This test validates that our interface is properly structured
        Class<PaymentDomainService> serviceClass = PaymentDomainService.class;
        
        // Verify key methods exist
        assertDoesNotThrow(() -> {
            serviceClass.getMethod("processPayment", PaymentRequest.class);
            serviceClass.getMethod("getPaymentStatus", String.class);
            serviceClass.getMethod("processRefund", RefundRequest.class);
            serviceClass.getMethod("validateTransaction", String.class);
        }, "All core PaymentDomainService methods should be defined");
        
        // Verify interface is properly structured
        assertTrue(serviceClass.isInterface(), "PaymentDomainService should be an interface");
        assertEquals("com.aims.domain.payment.service", serviceClass.getPackageName());
    }
}
