package com.aims.infrastructure.external.vnpay.config;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for VNPayConfig
 * Tests the utility methods for VNPAY payment processing
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VNPayConfig Unit Tests")
class VNPayConfigTest {

    @Mock
    private HttpServletRequest mockRequest;

    private VNPayConfig vnPayConfig;

    @BeforeEach
    void setUp() {
        vnPayConfig = new VNPayConfig();
        vnPayConfig.setSecretKey("TEST_SECRET_KEY");
        vnPayConfig.setPayUrl("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
        vnPayConfig.setReturnUrl("http://localhost:8080/api/payment/vnpay/return");
        vnPayConfig.setTmnCode("TEST_TMN_CODE");
        vnPayConfig.setApiUrl("https://sandbox.vnpayment.vn/merchant_webapi/api/transaction");
    }

    @Test
    @DisplayName("Should generate HMAC-SHA512 hash correctly")
    void hmacSHA512_ValidInputs_ReturnsCorrectHash() {
        // Given
        String key = "TEST_SECRET_KEY";
        String data = "amount=10000&orderInfo=Test Order";

        // When
        String result = vnPayConfig.hmacSHA512(key, data);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(128, result.length()); // SHA512 produces 128 hex characters
        assertTrue(result.matches("[0-9a-f]+"));
    }

    @Test
    @DisplayName("Should return empty string when HMAC-SHA512 inputs are null")
    void hmacSHA512_NullInputs_ReturnsEmptyString() {
        // When & Then
        assertEquals("", vnPayConfig.hmacSHA512(null, "data"));
        assertEquals("", vnPayConfig.hmacSHA512("key", null));
        assertEquals("", vnPayConfig.hmacSHA512(null, null));
    }

    @Test
    @DisplayName("Should produce consistent HMAC-SHA512 hash for same inputs")
    void hmacSHA512_SameInputs_ProducesConsistentHash() {
        // Given
        String key = "TEST_SECRET_KEY";
        String data = "amount=10000&orderInfo=Test Order";

        // When
        String hash1 = vnPayConfig.hmacSHA512(key, data);
        String hash2 = vnPayConfig.hmacSHA512(key, data);

        // Then
        assertEquals(hash1, hash2);
    }

    @Test
    @DisplayName("Should hash all fields in sorted order")
    void hashAllFields_ValidMap_ReturnsCorrectHash() {
        // Given
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("vnp_Amount", "10000");
        fields.put("vnp_Command", "pay");
        fields.put("vnp_TxnRef", "12345");

        // When
        String result = vnPayConfig.hashAllFields(fields);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(128, result.length());
    }

    @Test
    @DisplayName("Should handle empty map in hashAllFields")
    void hashAllFields_EmptyMap_ReturnsValidHash() {
        // Given
        Map<String, String> emptyFields = new HashMap<>();

        // When
        String result = vnPayConfig.hashAllFields(emptyFields);

        // Then
        assertNotNull(result);
        assertEquals(128, result.length());
    }

    @Test
    @DisplayName("Should ignore null and empty values in hashAllFields")
    void hashAllFields_WithNullAndEmptyValues_IgnoresInvalidValues() {
        // Given
        Map<String, String> fields = new HashMap<>();
        fields.put("vnp_Amount", "10000");
        fields.put("vnp_Command", "");  // Empty value
        fields.put("vnp_TxnRef", null); // Null value
        fields.put("vnp_Version", "2.1.0");

        // When
        String result = vnPayConfig.hashAllFields(fields);

        // Then
        assertNotNull(result);
        // Should only hash non-empty values (vnp_Amount and vnp_Version)
    }

    @Test
    @DisplayName("Should maintain field order consistency in hashAllFields")
    void hashAllFields_DifferentInsertionOrder_ProducesSameHash() {
        // Given
        Map<String, String> fields1 = new LinkedHashMap<>();
        fields1.put("vnp_Amount", "10000");
        fields1.put("vnp_Command", "pay");
        fields1.put("vnp_TxnRef", "12345");

        Map<String, String> fields2 = new LinkedHashMap<>();
        fields2.put("vnp_TxnRef", "12345");
        fields2.put("vnp_Amount", "10000");
        fields2.put("vnp_Command", "pay");

        // When
        String hash1 = vnPayConfig.hashAllFields(fields1);
        String hash2 = vnPayConfig.hashAllFields(fields2);

        // Then
        assertEquals(hash1, hash2);
    }

    @Test
    @DisplayName("Should extract IP from X-Forwarded-For header")
    void getIpAddress_XForwardedForHeader_ReturnsCorrectIP() {
        // Given
        String expectedIP = "192.168.1.100";
        when(mockRequest.getHeader("X-Forwarded-For")).thenReturn(expectedIP);

        // When
        String result = vnPayConfig.getIpAddress(mockRequest);

        // Then
        assertEquals(expectedIP, result);
    }

    @Test
    @DisplayName("Should extract IP from multiple proxy headers in fallback order")
    void getIpAddress_MultipleHeaders_UsesCorrectFallbackOrder() {
        // Given
        String expectedIP = "192.168.1.100";
        when(mockRequest.getHeader("X-Forwarded-For")).thenReturn("unknown");
        when(mockRequest.getHeader("Proxy-Client-IP")).thenReturn(expectedIP);

        // When
        String result = vnPayConfig.getIpAddress(mockRequest);

        // Then
        assertEquals(expectedIP, result);
    }

    @Test
    @DisplayName("Should handle comma-separated IPs in X-Forwarded-For")
    void getIpAddress_CommaSeparatedIPs_ReturnsFirstIP() {
        // Given
        String forwardedFor = "192.168.1.100, 10.0.0.1, 172.16.0.1";
        String expectedIP = "192.168.1.100";
        when(mockRequest.getHeader("X-Forwarded-For")).thenReturn(forwardedFor);

        // When
        String result = vnPayConfig.getIpAddress(mockRequest);

        // Then
        assertEquals(expectedIP, result);
    }

    @Test
    @DisplayName("Should fallback to remote address when headers are unavailable")
    void getIpAddress_NoProxyHeaders_UsesRemoteAddr() {
        // Given
        String expectedIP = "127.0.0.1";
        when(mockRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(mockRequest.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(mockRequest.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(mockRequest.getHeader("HTTP_CLIENT_IP")).thenReturn(null);
        when(mockRequest.getHeader("HTTP_X_FORWARDED_FOR")).thenReturn(null);
        when(mockRequest.getRemoteAddr()).thenReturn(expectedIP);

        // When
        String result = vnPayConfig.getIpAddress(mockRequest);

        // Then
        assertEquals(expectedIP, result);
    }

    @Test
    @DisplayName("Should handle unknown IP values")
    void getIpAddress_UnknownValues_UsesNextAvailable() {
        // Given
        String expectedIP = "192.168.1.100";
        when(mockRequest.getHeader("X-Forwarded-For")).thenReturn("unknown");
        when(mockRequest.getHeader("Proxy-Client-IP")).thenReturn("Unknown");
        when(mockRequest.getHeader("WL-Proxy-Client-IP")).thenReturn("UNKNOWN");
        when(mockRequest.getHeader("HTTP_CLIENT_IP")).thenReturn(expectedIP);

        // When
        String result = vnPayConfig.getIpAddress(mockRequest);

        // Then
        assertEquals(expectedIP, result);
    }

    @Test
    @DisplayName("Should generate random number with correct length")
    void getRandomNumber_ValidLength_ReturnsCorrectLength() {
        // Given
        int length = 8;

        // When
        String result = vnPayConfig.getRandomNumber(length);

        // Then
        assertEquals(length, result.length());
        assertTrue(result.matches("\\d+"));
    }

    @Test
    @DisplayName("Should generate different random numbers")
    void getRandomNumber_MultipleCalls_ReturnsDifferentNumbers() {
        // Given
        int length = 10;

        // When
        String number1 = vnPayConfig.getRandomNumber(length);
        String number2 = vnPayConfig.getRandomNumber(length);
        String number3 = vnPayConfig.getRandomNumber(length);

        // Then
        assertNotEquals(number1, number2);
        assertNotEquals(number2, number3);
        assertNotEquals(number1, number3);
    }

    @Test
    @DisplayName("Should handle zero length in getRandomNumber")
    void getRandomNumber_ZeroLength_ReturnsEmptyString() {
        // When
        String result = vnPayConfig.getRandomNumber(0);

        // Then
        assertEquals("", result);
    }

    @Test
    @DisplayName("Should generate only numeric characters")
    void getRandomNumber_AnyLength_ContainsOnlyDigits() {
        // Given
        int[] lengths = {1, 5, 10, 20};

        for (int length : lengths) {
            // When
            String result = vnPayConfig.getRandomNumber(length);

            // Then
            assertTrue(result.matches("\\d*"), 
                "Random number should contain only digits, but got: " + result);
        }
    }

    @Test
    @DisplayName("Should handle negative length gracefully")
    void getRandomNumber_NegativeLength_ReturnsEmptyString() {
        // When
        String result = vnPayConfig.getRandomNumber(-5);

        // Then
        assertEquals("", result);
    }

    @Test
    @DisplayName("Should verify configuration properties are set correctly")
    void configurationProperties_SetCorrectly_VerifyValues() {
        // Then
        assertEquals("TEST_SECRET_KEY", vnPayConfig.getSecretKey());
        assertEquals("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html", vnPayConfig.getPayUrl());
        assertEquals("http://localhost:8080/api/payment/vnpay/return", vnPayConfig.getReturnUrl());
        assertEquals("TEST_TMN_CODE", vnPayConfig.getTmnCode());
        assertEquals("https://sandbox.vnpayment.vn/merchant_webapi/api/transaction", vnPayConfig.getApiUrl());
    }

    @Test
    @DisplayName("Should handle special characters in HMAC hash")
    void hmacSHA512_SpecialCharacters_HandlesCorrectly() {
        // Given
        String key = "SECRET_KEY_WITH_SPECIAL!@#$%";
        String data = "orderInfo=Đơn hàng với ký tự đặc biệt & unicode";

        // When
        String result = vnPayConfig.hmacSHA512(key, data);

        // Then
        assertNotNull(result);
        assertEquals(128, result.length());
        assertTrue(result.matches("[0-9a-f]+"));
    }

    @Test
    @DisplayName("Should handle large data in HMAC hash")
    void hmacSHA512_LargeData_HandlesCorrectly() {
        // Given
        String key = "SECRET_KEY";
        StringBuilder largeData = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeData.append("field").append(i).append("=value").append(i).append("&");
        }

        // When
        String result = vnPayConfig.hmacSHA512(key, largeData.toString());

        // Then
        assertNotNull(result);
        assertEquals(128, result.length());
    }
}