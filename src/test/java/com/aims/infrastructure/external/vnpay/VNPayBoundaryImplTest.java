package com.aims.infrastructure.external.vnpay;

import com.aims.infrastructure.external.vnpay.config.VNPayConfig;
import com.aims.infrastructure.external.vnpay.service.HashService;
import com.aims.infrastructure.external.vnpay.service.HttpClientService;
import com.aims.presentation.dto.payment.PaymentRequest;
import com.aims.presentation.dto.payment.PaymentResponse;
import com.aims.presentation.dto.payment.PaymentStatusResponse;
import com.aims.presentation.dto.payment.RefundRequest;
import com.aims.presentation.dto.payment.RefundResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("VNPayBoundaryImpl Unit Tests")
class VNPayBoundaryImplTest {

    @Mock
    private VNPayConfig vnPayConfig;

    @Mock
    private HashService hashService;

    @Mock
    private HttpClientService httpClientService;

    @Mock
    private HttpServletRequest servletRequest;

    @InjectMocks
    private VNPayBoundaryImpl vnPayBoundary;

    private PaymentRequest paymentRequest;
    private RefundRequest refundRequest;

    @BeforeEach
    void setUp() {
        // Setup test payment request
        paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId("TEST_ORDER_123");
        paymentRequest.setAmount(100000L); // 100,000 VND
        paymentRequest.setOrderInfo("Test payment");
        paymentRequest.setBankCode("NCB");
        paymentRequest.setLanguage("vn");

        // Setup test refund request
        refundRequest = new RefundRequest();
        refundRequest.setOrderId("TEST_ORDER_123");
        refundRequest.setAmount(50000L); // 50,000 VND refund
        refundRequest.setTransactionDate("20231215140000");
        refundRequest.setTransactionType("02");
        refundRequest.setUser("test_user");

        // Setup default mock behaviors
        lenient().when(vnPayConfig.getPayUrl()).thenReturn("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
        lenient().when(vnPayConfig.getTmnCode()).thenReturn("TEST_TMN_CODE");
        lenient().when(vnPayConfig.getSecretKey()).thenReturn("TEST_SECRET_KEY");
        lenient().when(vnPayConfig.getReturnUrl()).thenReturn("http://localhost:8080/return");
        lenient().when(vnPayConfig.getApiUrl()).thenReturn("https://sandbox.vnpayment.vn/api");
        lenient().when(vnPayConfig.getIpAddress(any())).thenReturn("127.0.0.1");
        lenient().when(vnPayConfig.getRandomNumber(anyInt())).thenReturn("12345678");
    }

    @Test
    @DisplayName("Should successfully initiate payment")
    void initiatePayment_ValidRequest_ReturnsSuccessResponse() {
        // Given
        when(hashService.hmacSHA512(anyString(), anyString())).thenReturn("test_hash");

        // When
        PaymentResponse response = vnPayBoundary.initiatePayment(paymentRequest, servletRequest);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("00", response.getCode());
        assertEquals("Success", response.getMessage());
        assertNotNull(response.getPaymentUrl());
        assertTrue(response.getPaymentUrl().contains("sandbox.vnpayment.vn"));
        assertEquals("TEST_ORDER_123", response.getTransactionId());

        // Verify VNPayConfig methods were called
        verify(vnPayConfig).getPayUrl();
        verify(vnPayConfig).getTmnCode();
        verify(vnPayConfig).getSecretKey();
        verify(vnPayConfig).getIpAddress(servletRequest);
        verify(hashService).hmacSHA512(anyString(), anyString());
    }

    @Test
    @DisplayName("Should handle payment initiation failure")
    void initiatePayment_Exception_ReturnsFailureResponse() {
        // Given
        when(hashService.hmacSHA512(anyString(), anyString())).thenThrow(new RuntimeException("Hash generation failed"));

        // When
        PaymentResponse response = vnPayBoundary.initiatePayment(paymentRequest, servletRequest);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("99", response.getCode());
        assertTrue(response.getMessage().contains("Payment initiation failed"));
    }

    @Test
    @DisplayName("Should successfully check payment status")
    void checkPaymentStatus_ValidRequest_ReturnsSuccessResponse() {
        // Given
        VNPayBoundaryImpl.VNPayQueryResponse mockResponse = new VNPayBoundaryImpl.VNPayQueryResponse();
        mockResponse.setVnp_ResponseCode("00");
        mockResponse.setVnp_TransactionNo("VNP_123456789");
        mockResponse.setVnp_Amount("10000000"); // 100,000 VND in cents
        mockResponse.setVnp_TransactionStatus("00");
        mockResponse.setVnp_BankCode("NCB");
        mockResponse.setVnp_PayDate("20231215140530");

        when(hashService.hmacSHA512(anyString(), anyString())).thenReturn("test_hash");
        when(httpClientService.callApi(anyString(), any(Map.class), eq(VNPayBoundaryImpl.VNPayQueryResponse.class)))
            .thenReturn(mockResponse);

        // When
        PaymentStatusResponse response = vnPayBoundary.checkPaymentStatus("TEST_ORDER_123", "20231215140000", servletRequest);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("TEST_ORDER_123", response.getTransactionId());
        assertEquals("VNP_123456789", response.getVnpayTransactionNo());
        assertEquals(10000000L, response.getAmount());
        assertEquals("00", response.getTransactionStatus());
        assertEquals("NCB", response.getBankCode());
        assertEquals("20231215140530", response.getPayDate());

        verify(httpClientService).callApi(anyString(), any(Map.class), eq(VNPayBoundaryImpl.VNPayQueryResponse.class));
        verify(hashService).hmacSHA512(anyString(), anyString());
    }

    @Test
    @DisplayName("Should handle payment status query failure")
    void checkPaymentStatus_FailureResponse_ReturnsFailureResponse() {
        // Given
        VNPayBoundaryImpl.VNPayQueryResponse mockResponse = new VNPayBoundaryImpl.VNPayQueryResponse();
        mockResponse.setVnp_ResponseCode("91");
        mockResponse.setVnp_Message("Transaction not found");

        when(hashService.hmacSHA512(anyString(), anyString())).thenReturn("test_hash");
        when(httpClientService.callApi(anyString(), any(Map.class), eq(VNPayBoundaryImpl.VNPayQueryResponse.class)))
            .thenReturn(mockResponse);

        // When
        PaymentStatusResponse response = vnPayBoundary.checkPaymentStatus("INVALID_ORDER", "20231215140000", servletRequest);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("91", response.getResponseCode());
        assertEquals("Transaction not found", response.getMessage());
    }

    @Test
    @DisplayName("Should handle payment status query exception")
    void checkPaymentStatus_Exception_ReturnsFailureResponse() {
        // Given
        when(hashService.hmacSHA512(anyString(), anyString())).thenReturn("test_hash");
        when(httpClientService.callApi(anyString(), any(Map.class), eq(VNPayBoundaryImpl.VNPayQueryResponse.class)))
            .thenThrow(new RuntimeException("Network error"));

        // When
        PaymentStatusResponse response = vnPayBoundary.checkPaymentStatus("TEST_ORDER_123", "20231215140000", servletRequest);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("99", response.getResponseCode());
        assertTrue(response.getMessage().contains("Status check failed"));
    }

    @Test
    @DisplayName("Should successfully process refund")
    void processRefund_ValidRequest_ReturnsSuccessResponse() {
        // Given
        VNPayBoundaryImpl.VNPayRefundResponse mockResponse = new VNPayBoundaryImpl.VNPayRefundResponse();
        mockResponse.setVnp_ResponseCode("00");
        mockResponse.setVnp_TxnRef("TEST_ORDER_123");
        mockResponse.setVnp_TransactionNo("VNP_123456789");
        mockResponse.setVnp_TransactionStatus("00");
        mockResponse.setVnp_BankCode("NCB");

        when(hashService.hmacSHA512(anyString(), anyString())).thenReturn("test_hash");
        when(httpClientService.callApi(anyString(), any(Map.class), eq(VNPayBoundaryImpl.VNPayRefundResponse.class)))
            .thenReturn(mockResponse);

        // When
        RefundResponse response = vnPayBoundary.processRefund(refundRequest, servletRequest);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("TEST_ORDER_123", response.getTransactionId());
        assertEquals("VNP_123456789", response.getVnpayTransactionNo());
        assertEquals(50000L, response.getAmount());
        assertEquals("00", response.getTransactionStatus());
        assertEquals("NCB", response.getBankCode());

        verify(httpClientService).callApi(anyString(), any(Map.class), eq(VNPayBoundaryImpl.VNPayRefundResponse.class));
        verify(hashService).hmacSHA512(anyString(), anyString());
    }

    @Test
    @DisplayName("Should handle refund processing failure")
    void processRefund_FailureResponse_ReturnsFailureResponse() {
        // Given
        VNPayBoundaryImpl.VNPayRefundResponse mockResponse = new VNPayBoundaryImpl.VNPayRefundResponse();
        mockResponse.setVnp_ResponseCode("04");
        mockResponse.setVnp_Message("Invalid amount");

        when(hashService.hmacSHA512(anyString(), anyString())).thenReturn("test_hash");
        when(httpClientService.callApi(anyString(), any(Map.class), eq(VNPayBoundaryImpl.VNPayRefundResponse.class)))
            .thenReturn(mockResponse);

        // When
        RefundResponse response = vnPayBoundary.processRefund(refundRequest, servletRequest);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("04", response.getResponseCode());
        assertEquals("Invalid amount", response.getMessage());
    }

    @Test
    @DisplayName("Should handle refund processing exception")
    void processRefund_Exception_ReturnsFailureResponse() {
        // Given
        when(hashService.hmacSHA512(anyString(), anyString())).thenReturn("test_hash");
        when(httpClientService.callApi(anyString(), any(Map.class), eq(VNPayBoundaryImpl.VNPayRefundResponse.class)))
            .thenThrow(new RuntimeException("Connection timeout"));

        // When
        RefundResponse response = vnPayBoundary.processRefund(refundRequest, servletRequest);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("99", response.getResponseCode());
        assertTrue(response.getMessage().contains("Refund processing failed"));
    }

    @Test
    @DisplayName("Should return correct provider name")
    void getProviderName_ReturnsVNPay() {
        // When
        String providerName = vnPayBoundary.getProviderName();

        // Then
        assertEquals("VNPay", providerName);
    }

    @Test
    @DisplayName("Should successfully validate payment callback")
    void validatePaymentCallback_ValidSignature_ReturnsTrue() {
        // Given
        Map<String, String> params = new HashMap<>();
        params.put("vnp_TxnRef", "TEST_ORDER_123");
        params.put("vnp_Amount", "10000000");
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_SecureHash", "valid_hash");

        when(hashService.hashAllFields(any(Map.class))).thenReturn("valid_hash");

        // When
        boolean isValid = vnPayBoundary.validatePaymentCallback(params);

        // Then
        assertTrue(isValid);
        verify(hashService).hashAllFields(any(Map.class));
    }

    @Test
    @DisplayName("Should fail validation for invalid signature")
    void validatePaymentCallback_InvalidSignature_ReturnsFalse() {
        // Given
        Map<String, String> params = new HashMap<>();
        params.put("vnp_TxnRef", "TEST_ORDER_123");
        params.put("vnp_Amount", "10000000");
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_SecureHash", "invalid_hash");

        when(hashService.hashAllFields(any(Map.class))).thenReturn("different_hash");

        // When
        boolean isValid = vnPayBoundary.validatePaymentCallback(params);

        // Then
        assertFalse(isValid);
        verify(hashService).hashAllFields(any(Map.class));
    }

    @Test
    @DisplayName("Should fail validation when no secure hash provided")
    void validatePaymentCallback_NoSecureHash_ReturnsFalse() {
        // Given
        Map<String, String> params = new HashMap<>();
        params.put("vnp_TxnRef", "TEST_ORDER_123");
        params.put("vnp_Amount", "10000000");
        params.put("vnp_ResponseCode", "00");
        // No vnp_SecureHash

        // When
        boolean isValid = vnPayBoundary.validatePaymentCallback(params);

        // Then
        assertFalse(isValid);
        verify(hashService, never()).hashAllFields(any());
    }

    @Test
    @DisplayName("Should handle validation exception gracefully")
    void validatePaymentCallback_Exception_ReturnsFalse() {
        // Given
        Map<String, String> params = new HashMap<>();
        params.put("vnp_TxnRef", "TEST_ORDER_123");
        params.put("vnp_SecureHash", "some_hash");

        when(hashService.hashAllFields(any(Map.class))).thenThrow(new RuntimeException("Hash error"));

        // When
        boolean isValid = vnPayBoundary.validatePaymentCallback(params);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should build payment URL with correct parameters")
    void initiatePayment_ChecksParameterBuilding() {
        // Given
        paymentRequest.setAmount(250000L); // 250,000 VND
        paymentRequest.setReturnUrl("http://custom-return.com");

        when(hashService.hmacSHA512(anyString(), anyString())).thenReturn("test_hash_value");

        // When
        PaymentResponse response = vnPayBoundary.initiatePayment(paymentRequest, servletRequest);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        
        String paymentUrl = response.getPaymentUrl();
        assertTrue(paymentUrl.contains("vnp_Amount=25000000")); // Amount in cents
        assertTrue(paymentUrl.contains("vnp_TxnRef=TEST_ORDER_123"));
        assertTrue(paymentUrl.contains("vnp_BankCode=NCB"));
        assertTrue(paymentUrl.contains("vnp_Locale=vn"));
        assertTrue(paymentUrl.contains("vnp_SecureHash=test_hash_value"));
    }

    @Test
    @DisplayName("Should handle null payment request")
    void initiatePayment_NullRequest_ReturnsFailureResponse() {
        // When
        PaymentResponse response = vnPayBoundary.initiatePayment(null, servletRequest);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("99", response.getCode());
        assertTrue(response.getMessage().contains("Payment initiation failed"));
    }

    @Test
    @DisplayName("Should handle payment request with minimum required fields")
    void initiatePayment_MinimalRequest_Success() {
        // Given
        PaymentRequest minimalRequest = new PaymentRequest();
        minimalRequest.setOrderId("MIN_ORDER");
        minimalRequest.setAmount(10000L);

        when(hashService.hmacSHA512(anyString(), anyString())).thenReturn("minimal_hash");

        // When
        PaymentResponse response = vnPayBoundary.initiatePayment(minimalRequest, servletRequest);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("MIN_ORDER", response.getTransactionId());
    }
}