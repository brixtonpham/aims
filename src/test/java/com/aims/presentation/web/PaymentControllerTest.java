package com.aims.presentation.web;

import com.aims.application.dto.PaymentInitiationRequest;
import com.aims.application.dto.PaymentCallbackRequest;
import com.aims.application.services.PaymentApplicationService;
import com.aims.presentation.dto.payment.PaymentResponse;
import com.aims.presentation.dto.payment.PaymentStatusResponse;
import com.aims.presentation.dto.payment.RefundRequest;
import com.aims.presentation.dto.payment.RefundResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for PaymentController
 * Tests payment-related endpoints and business flows
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentController Unit Tests")
class PaymentControllerTest {

    @Mock
    private PaymentApplicationService paymentApplicationService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private PaymentController paymentController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private PaymentInitiationRequest testPaymentRequest;
    private PaymentResponse testPaymentResponse;
    private RefundRequest testRefundRequest;
    private RefundResponse testRefundResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
        objectMapper = new ObjectMapper();

        // Setup test data
        setupTestData();
    }

    private void setupTestData() {
        // Test payment initiation request
        testPaymentRequest = new PaymentInitiationRequest();
        testPaymentRequest.setOrderId("ORDER123");
        testPaymentRequest.setAmount(100000L);
        testPaymentRequest.setOrderInfo("Test payment");
        testPaymentRequest.setBankCode("NCB");
        testPaymentRequest.setLanguage("vn");

        // Test payment response
        testPaymentResponse = PaymentResponse.success(
            "https://vnpay.com/payment?orderId=ORDER123", 
            "ORDER123"
        );

        // Test refund request
        testRefundRequest = new RefundRequest();
        testRefundRequest.setOrderId("ORDER123");
        testRefundRequest.setAmount(100000L);
        testRefundRequest.setTransactionDate("20250627210115");
        testRefundRequest.setTransactionType("02");
        testRefundRequest.setUser("admin");

        // Test refund response
        testRefundResponse = RefundResponse.success("ORDER123", "VNP123456", 100000L);
    }

    @Test
    @DisplayName("Should initiate payment successfully")
    void initiatePayment_ValidRequest_ReturnsSuccessResponse() throws Exception {
        // Given
        when(paymentApplicationService.initiatePayment(any(PaymentInitiationRequest.class), any(HttpServletRequest.class)))
            .thenReturn(testPaymentResponse);

        // When & Then
        mockMvc.perform(post("/api/payments/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPaymentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("00"))
                .andExpect(jsonPath("$.paymentUrl").exists())
                .andExpect(jsonPath("$.transactionId").value("ORDER123"));

        verify(paymentApplicationService).initiatePayment(any(PaymentInitiationRequest.class), any(HttpServletRequest.class));
    }

    @Test
    @DisplayName("Should return bad request for invalid payment initiation")
    void initiatePayment_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Given
        PaymentResponse failureResponse = PaymentResponse.failure("01", "Invalid order");
        when(paymentApplicationService.initiatePayment(any(PaymentInitiationRequest.class), any(HttpServletRequest.class)))
            .thenReturn(failureResponse);

        // When & Then
        mockMvc.perform(post("/api/payments/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPaymentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("01"))
                .andExpect(jsonPath("$.message").value("Invalid order"));

        verify(paymentApplicationService).initiatePayment(any(PaymentInitiationRequest.class), any(HttpServletRequest.class));
    }

    @Test
    @DisplayName("Should handle payment initiation with missing required fields")
    void initiatePayment_MissingRequiredFields_ReturnsBadRequest() throws Exception {
        // Given
        PaymentInitiationRequest invalidRequest = new PaymentInitiationRequest();
        // Missing required fields

        // When & Then
        mockMvc.perform(post("/api/payments/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(paymentApplicationService, never()).initiatePayment(any(), any());
    }

    @Test
    @DisplayName("Should handle payment callback successfully")
    void handlePaymentCallback_ValidCallback_ReturnsSuccess() throws Exception {
        // Given
        Map<String, String> callbackParams = new HashMap<>();
        callbackParams.put("vnp_TxnRef", "ORDER123");
        callbackParams.put("vnp_ResponseCode", "00");
        callbackParams.put("vnp_Amount", "10000000");
        callbackParams.put("vnp_TransactionNo", "VNP123456");

        doNothing().when(paymentApplicationService).handlePaymentCallback(any(PaymentCallbackRequest.class));

        // When & Then
        mockMvc.perform(post("/api/payments/callback")
                .param("vnp_TxnRef", "ORDER123")
                .param("vnp_ResponseCode", "00")
                .param("vnp_Amount", "10000000")
                .param("vnp_TransactionNo", "VNP123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RspCode").value("00"))
                .andExpect(jsonPath("$.Message").value("Success"));

        verify(paymentApplicationService).handlePaymentCallback(any(PaymentCallbackRequest.class));
    }

    @Test
    @DisplayName("Should query payment status successfully")
    void queryPaymentStatus_ValidRequest_ReturnsStatus() throws Exception {
        // Given
        PaymentStatusResponse statusResponse = PaymentStatusResponse.success(
            "ORDER123", "VNP123456", 100000L, "00"
        );
        when(paymentApplicationService.queryPaymentStatus(anyString(), anyString(), any(HttpServletRequest.class)))
            .thenReturn(statusResponse);

        // When & Then
        mockMvc.perform(get("/api/payments/status/{transactionId}", "ORDER123")
                .param("transactionDate", "20250627210115"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.transactionId").value("ORDER123"))
                .andExpect(jsonPath("$.vnpayTransactionNo").value("VNP123456"))
                .andExpect(jsonPath("$.amount").value(100000));

        verify(paymentApplicationService).queryPaymentStatus(eq("ORDER123"), eq("20250627210115"), any(HttpServletRequest.class));
    }

    @Test
    @DisplayName("Should process refund successfully")
    void processRefund_ValidRequest_ReturnsSuccess() throws Exception {
        // Given
        when(paymentApplicationService.processRefund(any(RefundRequest.class), any(HttpServletRequest.class)))
            .thenReturn(testRefundResponse);

        // When & Then
        mockMvc.perform(post("/api/payments/refund")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRefundRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.transactionId").value("ORDER123"))
                .andExpect(jsonPath("$.amount").value(100000));

        verify(paymentApplicationService).processRefund(any(RefundRequest.class), any(HttpServletRequest.class));
    }

    @Test
    @DisplayName("Should return bad request for invalid refund")
    void processRefund_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Given
        RefundResponse failureResponse = RefundResponse.failure("01", "Order not found");
        when(paymentApplicationService.processRefund(any(RefundRequest.class), any(HttpServletRequest.class)))
            .thenReturn(failureResponse);

        // When & Then
        mockMvc.perform(post("/api/payments/refund")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRefundRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.responseCode").value("01"))
                .andExpect(jsonPath("$.message").value("Order not found"));

        verify(paymentApplicationService).processRefund(any(RefundRequest.class), any(HttpServletRequest.class));
    }

    @Test
    @DisplayName("Should handle legacy payment request")
    void createPaymentLegacy_ValidRequest_ReturnsSuccess() throws Exception {
        // Given
        Map<String, Object> legacyRequest = new HashMap<>();
        legacyRequest.put("amount", "100000");
        legacyRequest.put("language", "vn");
        legacyRequest.put("orderId", "ORDER123");
        legacyRequest.put("bankCode", "NCB");

        when(paymentApplicationService.initiatePayment(any(PaymentInitiationRequest.class), any(HttpServletRequest.class)))
            .thenReturn(testPaymentResponse);

        // When & Then
        mockMvc.perform(post("/api/payments/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(legacyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("00"));

        verify(paymentApplicationService).initiatePayment(any(PaymentInitiationRequest.class), any(HttpServletRequest.class));
    }

    @Test
    @DisplayName("Should handle exception during payment initiation")
    void initiatePayment_ServiceException_ReturnsInternalServerError() throws Exception {
        // Given
        when(paymentApplicationService.initiatePayment(any(PaymentInitiationRequest.class), any(HttpServletRequest.class)))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(post("/api/payments/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPaymentRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("99"))
                .andExpect(jsonPath("$.message").value("Internal server error"));

        verify(paymentApplicationService).initiatePayment(any(PaymentInitiationRequest.class), any(HttpServletRequest.class));
    }

    @Test
    @DisplayName("Should handle exception during payment callback")
    void handlePaymentCallback_ServiceException_ReturnsInternalServerError() throws Exception {
        // Given
        doThrow(new RuntimeException("Processing failed"))
            .when(paymentApplicationService).handlePaymentCallback(any(PaymentCallbackRequest.class));

        // When & Then
        mockMvc.perform(post("/api/payments/callback")
                .param("vnp_TxnRef", "ORDER123")
                .param("vnp_ResponseCode", "00"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.RspCode").value("99"))
                .andExpect(jsonPath("$.Message").value("Error processing callback"));

        verify(paymentApplicationService).handlePaymentCallback(any(PaymentCallbackRequest.class));
    }

    @Test
    @DisplayName("Should handle exception during status query")
    void queryPaymentStatus_ServiceException_ReturnsInternalServerError() throws Exception {
        // Given
        when(paymentApplicationService.queryPaymentStatus(anyString(), anyString(), any(HttpServletRequest.class)))
            .thenThrow(new RuntimeException("Service unavailable"));

        // When & Then
        mockMvc.perform(get("/api/payments/status/{transactionId}", "ORDER123")
                .param("transactionDate", "20250627210115"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.responseCode").value("99"))
                .andExpect(jsonPath("$.message").value("Status query failed"));

        verify(paymentApplicationService).queryPaymentStatus(eq("ORDER123"), eq("20250627210115"), any(HttpServletRequest.class));
    }

    @Test
    @DisplayName("Should handle exception during refund processing")
    void processRefund_ServiceException_ReturnsInternalServerError() throws Exception {
        // Given
        when(paymentApplicationService.processRefund(any(RefundRequest.class), any(HttpServletRequest.class)))
            .thenThrow(new RuntimeException("Refund service error"));

        // When & Then
        mockMvc.perform(post("/api/payments/refund")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRefundRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.responseCode").value("99"))
                .andExpect(jsonPath("$.message").value("Internal server error"));

        verify(paymentApplicationService).processRefund(any(RefundRequest.class), any(HttpServletRequest.class));
    }

    @Test
    @DisplayName("Should validate required fields in payment request")
    void initiatePayment_NullAmount_ReturnsBadRequest() throws Exception {
        // Given
        PaymentInitiationRequest invalidRequest = new PaymentInitiationRequest();
        invalidRequest.setOrderId("ORDER123");
        // amount is null

        // When & Then
        mockMvc.perform(post("/api/payments/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(paymentApplicationService, never()).initiatePayment(any(), any());
    }

    @Test
    @DisplayName("Should validate required fields in refund request")
    void processRefund_NullOrderId_ReturnsBadRequest() throws Exception {
        // Given
        RefundRequest invalidRequest = new RefundRequest();
        invalidRequest.setAmount(100000L);
        // orderId is null

        // When & Then
        mockMvc.perform(post("/api/payments/refund")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(paymentApplicationService, never()).processRefund(any(), any());
    }

    // Test direct controller methods (unit tests without MockMvc)
    @Test
    @DisplayName("Should initiate payment using direct controller method")
    void initiatePayment_DirectCall_ReturnsResponseEntity() {
        // Given
        when(paymentApplicationService.initiatePayment(any(PaymentInitiationRequest.class), any(HttpServletRequest.class)))
            .thenReturn(testPaymentResponse);

        // When
        ResponseEntity<PaymentResponse> result = paymentController.initiatePayment(testPaymentRequest, httpServletRequest);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());
        assertTrue(result.getBody().isSuccess());
        assertEquals("ORDER123", result.getBody().getTransactionId());

        verify(paymentApplicationService).initiatePayment(testPaymentRequest, httpServletRequest);
    }

    @Test
    @DisplayName("Should process refund using direct controller method")
    void processRefund_DirectCall_ReturnsResponseEntity() {
        // Given
        when(paymentApplicationService.processRefund(any(RefundRequest.class), any(HttpServletRequest.class)))
            .thenReturn(testRefundResponse);

        // When
        ResponseEntity<RefundResponse> result = paymentController.processRefund(testRefundRequest, httpServletRequest);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());
        assertTrue(result.getBody().isSuccess());
        assertEquals("ORDER123", result.getBody().getTransactionId());

        verify(paymentApplicationService).processRefund(testRefundRequest, httpServletRequest);
    }
}