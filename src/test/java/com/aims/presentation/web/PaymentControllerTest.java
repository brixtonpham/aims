package com.aims.presentation.web;

import com.aims.application.payment.PaymentApplicationService;
import com.aims.domain.payment.model.PaymentResult;
import com.aims.domain.payment.model.RefundResult;
import com.aims.domain.payment.model.PaymentMethod;
import com.aims.presentation.dto.ApiResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentController
 * Tests the REST endpoints and request/response handling
 * 
 * Phase 4: Controller Migration & Testing
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentController Unit Tests")
class PaymentControllerTest {

    @Mock
    private PaymentApplicationService paymentApplicationService;

    @Mock
    private com.aims.vnpay.common.controller.VNPayController vnpayController;

    @InjectMocks
    private PaymentController paymentController;

    private MockHttpServletRequest mockRequest;
    private Map<String, Object> validVNPayRequest;
    private Map<String, Object> validRefundRequest;
    private PaymentResult successfulPaymentResult;
    private RefundResult successfulRefundResult;

    @BeforeEach
    void setUp() {
        mockRequest = new MockHttpServletRequest();
        
        // Setup valid VNPay payment request
        validVNPayRequest = new HashMap<>();
        validVNPayRequest.put("orderId", "ORD-123");
        validVNPayRequest.put("amount", 150000L);
        validVNPayRequest.put("customerId", "CUST-001");

        // Setup valid refund request
        validRefundRequest = new HashMap<>();
        validRefundRequest.put("orderId", "ORD-123");
        validRefundRequest.put("transactionId", "TXN-456");
        validRefundRequest.put("amount", 100000L);
        validRefundRequest.put("reason", "Customer request");

        // Setup successful results
        successfulPaymentResult = PaymentResult.success("TXN-789", "http://vnpay.vn/payment/123");
        successfulPaymentResult.setMessage("Payment created successfully");

        successfulRefundResult = RefundResult.success("REF-101", "TXN-456");
        successfulRefundResult.setMessage("Refund processed successfully");
    }

    @Test
    @DisplayName("Should successfully create VNPay payment")
    void shouldCreateVNPayPaymentSuccessfully() {
        // Given
        when(paymentApplicationService.processOrderPayment(
            anyString(), anyLong(), eq(PaymentMethod.VNPAY), anyString(), any()))
            .thenReturn(successfulPaymentResult);

        // When
        ResponseEntity<ApiResponse<Map<String, Object>>> response = 
            paymentController.createVNPayPayment(validVNPayRequest, mockRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Payment URL created successfully", response.getBody().getMessage());
        
        Map<String, Object> responseData = response.getBody().getData();
        assertNotNull(responseData);
        assertTrue((Boolean) responseData.get("success"));
        assertEquals("http://vnpay.vn/payment/123", responseData.get("paymentUrl"));
        assertEquals("TXN-789", responseData.get("transactionId"));
        assertEquals("Payment created successfully", responseData.get("message"));

        verify(paymentApplicationService).processOrderPayment(
            eq("ORD-123"), eq(150000L), eq(PaymentMethod.VNPAY), eq("CUST-001"), eq(mockRequest));
    }

    @Test
    @DisplayName("Should reject VNPay payment with missing order ID")
    void shouldRejectVNPayPaymentWithMissingOrderId() {
        // Given
        validVNPayRequest.remove("orderId");

        // When
        ResponseEntity<ApiResponse<Map<String, Object>>> response = 
            paymentController.createVNPayPayment(validVNPayRequest, mockRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
        assertTrue(response.getBody().getMessage().contains("Order ID is required"));

        verify(paymentApplicationService, never()).processOrderPayment(
            anyString(), anyLong(), any(PaymentMethod.class), anyString(), any());
    }

    @Test
    @DisplayName("Should reject VNPay payment with missing amount")
    void shouldRejectVNPayPaymentWithMissingAmount() {
        // Given
        validVNPayRequest.remove("amount");

        // When
        ResponseEntity<ApiResponse<Map<String, Object>>> response = 
            paymentController.createVNPayPayment(validVNPayRequest, mockRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
        assertTrue(response.getBody().getMessage().contains("Amount is required"));

        verify(paymentApplicationService, never()).processOrderPayment(
            anyString(), anyLong(), any(PaymentMethod.class), anyString(), any());
    }

    @Test
    @DisplayName("Should reject VNPay payment with missing customer ID")
    void shouldRejectVNPayPaymentWithMissingCustomerId() {
        // Given
        validVNPayRequest.remove("customerId");

        // When
        ResponseEntity<ApiResponse<Map<String, Object>>> response = 
            paymentController.createVNPayPayment(validVNPayRequest, mockRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
        assertTrue(response.getBody().getMessage().contains("Customer ID is required"));

        verify(paymentApplicationService, never()).processOrderPayment(
            anyString(), anyLong(), any(PaymentMethod.class), anyString(), any());
    }

    @Test
    @DisplayName("Should handle VNPay payment failure")
    void shouldHandleVNPayPaymentFailure() {
        // Given
        PaymentResult failedResult = PaymentResult.failure("PAYMENT_FAILED", "Insufficient funds");
        when(paymentApplicationService.processOrderPayment(
            anyString(), anyLong(), eq(PaymentMethod.VNPAY), anyString(), any()))
            .thenReturn(failedResult);

        // When
        ResponseEntity<ApiResponse<Map<String, Object>>> response = 
            paymentController.createVNPayPayment(validVNPayRequest, mockRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("PAYMENT_FAILED", response.getBody().getErrorCode());
        assertEquals("Insufficient funds", response.getBody().getMessage());

        verify(paymentApplicationService).processOrderPayment(
            eq("ORD-123"), eq(150000L), eq(PaymentMethod.VNPAY), eq("CUST-001"), eq(mockRequest));
    }

    @Test
    @DisplayName("Should successfully delegate VNPay callback")
    void shouldDelegateVNPayCallbackSuccessfully() {
        // Given
        Map<String, String> callbackParams = new HashMap<>();
        callbackParams.put("vnp_ResponseCode", "00");
        callbackParams.put("vnp_TxnRef", "ORD-123");
        
        Map<String, Object> mockResponse = Map.of("status", "SUCCESS", "message", "Payment confirmed");
        ResponseEntity<Map<String, Object>> mockResponseEntity = ResponseEntity.ok(mockResponse);
        
        when(vnpayController.returnPage(eq(callbackParams), eq(mockRequest)))
            .thenReturn(mockResponseEntity);

        // When
        ResponseEntity<Map<String, Object>> response = 
            paymentController.handleVNPayCallback(callbackParams, mockRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SUCCESS", response.getBody().get("status"));
        assertEquals("Payment confirmed", response.getBody().get("message"));

        verify(vnpayController).returnPage(eq(callbackParams), eq(mockRequest));
    }

    @Test
    @DisplayName("Should handle VNPay callback delegation failure")
    void shouldHandleVNPayCallbackDelegationFailure() {
        // Given
        Map<String, String> callbackParams = new HashMap<>();
        when(vnpayController.returnPage(eq(callbackParams), eq(mockRequest)))
            .thenThrow(new RuntimeException("VNPay service unavailable"));

        // When
        ResponseEntity<Map<String, Object>> response = 
            paymentController.handleVNPayCallback(callbackParams, mockRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERROR", response.getBody().get("status"));
        assertEquals("Error processing VNPay callback", response.getBody().get("message"));
        assertTrue(response.getBody().get("error").toString().contains("VNPay service unavailable"));

        verify(vnpayController).returnPage(eq(callbackParams), eq(mockRequest));
    }

    @Test
    @DisplayName("Should successfully process refund")
    void shouldProcessRefundSuccessfully() {
        // Given
        when(paymentApplicationService.processOrderRefund(
            anyString(), anyString(), anyLong(), anyString(), any()))
            .thenReturn(successfulRefundResult);

        // When
        ResponseEntity<ApiResponse<Map<String, Object>>> response = 
            paymentController.processRefund(validRefundRequest, mockRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Refund processed successfully", response.getBody().getMessage());
        
        Map<String, Object> responseData = response.getBody().getData();
        assertNotNull(responseData);
        assertTrue((Boolean) responseData.get("success"));
        assertEquals("REF-101", responseData.get("refundId"));
        assertEquals("TXN-456", responseData.get("transactionId"));

        verify(paymentApplicationService).processOrderRefund(
            eq("ORD-123"), eq("TXN-456"), eq(100000L), eq("Customer request"), eq(mockRequest));
    }

    @Test
    @DisplayName("Should reject refund with missing transaction ID")
    void shouldRejectRefundWithMissingTransactionId() {
        // Given
        validRefundRequest.remove("transactionId");

        // When
        ResponseEntity<ApiResponse<Map<String, Object>>> response = 
            paymentController.processRefund(validRefundRequest, mockRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
        assertTrue(response.getBody().getMessage().contains("Transaction ID is required"));

        verify(paymentApplicationService, never()).processOrderRefund(
            anyString(), anyString(), anyLong(), anyString(), any());
    }

    @Test
    @DisplayName("Should successfully get payment status")
    void shouldGetPaymentStatusSuccessfully() {
        // Given
        String transactionId = "TXN-123";

        // When
        ResponseEntity<ApiResponse<Map<String, Object>>> response = 
            paymentController.getPaymentStatus(transactionId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Payment status retrieved successfully", response.getBody().getMessage());
        
        Map<String, Object> statusInfo = response.getBody().getData();
        assertNotNull(statusInfo);
        assertEquals(transactionId, statusInfo.get("transactionId"));
        assertEquals("PENDING", statusInfo.get("status"));
        assertEquals("Payment is being processed", statusInfo.get("statusDescription"));
        assertEquals(150000L, statusInfo.get("amount"));
        assertEquals("VND", statusInfo.get("currency"));
        assertNotNull(statusInfo.get("createdAt"));
        assertNotNull(statusInfo.get("orderId"));
    }

    @Test
    @DisplayName("Should reject get payment status with empty transaction ID")
    void shouldRejectGetPaymentStatusWithEmptyTransactionId() {
        // When
        ResponseEntity<ApiResponse<Map<String, Object>>> response = 
            paymentController.getPaymentStatus("");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
        assertTrue(response.getBody().getMessage().contains("Transaction ID is required"));
    }

    @Test
    @DisplayName("Should handle payment application service exception")
    void shouldHandlePaymentApplicationServiceException() {
        // Given
        when(paymentApplicationService.processOrderPayment(
            anyString(), anyLong(), eq(PaymentMethod.VNPAY), anyString(), any()))
            .thenThrow(new RuntimeException("Payment gateway unavailable"));

        // When
        ResponseEntity<ApiResponse<Map<String, Object>>> response = 
            paymentController.createVNPayPayment(validVNPayRequest, mockRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("INTERNAL_ERROR", response.getBody().getErrorCode());
        assertEquals("Failed to create payment", response.getBody().getMessage());

        verify(paymentApplicationService).processOrderPayment(
            eq("ORD-123"), eq(150000L), eq(PaymentMethod.VNPAY), eq("CUST-001"), eq(mockRequest));
    }

    @Test
    @DisplayName("Should handle refund processing failure")
    void shouldHandleRefundProcessingFailure() {
        // Given
        RefundResult failedResult = RefundResult.failure("Transaction already refunded");
        when(paymentApplicationService.processOrderRefund(
            anyString(), anyString(), anyLong(), anyString(), any()))
            .thenReturn(failedResult);

        // When
        ResponseEntity<ApiResponse<Map<String, Object>>> response = 
            paymentController.processRefund(validRefundRequest, mockRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("REFUND_FAILED", response.getBody().getErrorCode());
        assertEquals("Transaction already refunded", response.getBody().getMessage());

        verify(paymentApplicationService).processOrderRefund(
            eq("ORD-123"), eq("TXN-456"), eq(100000L), eq("Customer request"), eq(mockRequest));
    }
}
