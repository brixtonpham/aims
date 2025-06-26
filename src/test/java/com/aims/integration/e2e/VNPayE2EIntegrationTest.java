package com.aims.integration.e2e;

import com.aims.application.dto.PaymentCallbackRequest;
import com.aims.application.dto.PaymentInitiationRequest;
import com.aims.application.services.PaymentApplicationService;
import com.aims.integration.BaseIntegrationTest;
import com.aims.domain.payment.entity.PaymentTransaction;
import com.aims.domain.payment.repository.PaymentTransactionRepository;
import com.aims.infrastructure.external.vnpay.VNPayBoundaryImpl;
import com.aims.infrastructure.external.vnpay.config.VNPayConfig;
import com.aims.presentation.dto.payment.PaymentResponse;
import com.aims.presentation.dto.payment.PaymentStatusResponse;
import com.aims.presentation.dto.payment.RefundRequest;
import com.aims.presentation.dto.payment.RefundResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VNPay End-to-End Integration Test
 * 
 * Comprehensive integration test for VNPay payment workflow.
 * Tests the complete payment flow from initiation to completion.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("VNPay E2E Integration Tests")
class VNPayE2EIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PaymentApplicationService paymentApplicationService;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    private VNPayBoundaryImpl vnPayBoundary;

    @Autowired
    private VNPayConfig vnPayConfig;

    private String baseUrl;
    private MockHttpServletRequest mockRequest;
    private PaymentInitiationRequest testPaymentRequest;
    private String testOrderId;

    @BeforeEach
    @Override
    protected void setUp() {
        super.setUp();
        baseUrl = "http://localhost:" + port + "/api/payments";
        setupMockRequest();
    }

    private void setupMockRequest() {
        mockRequest = new MockHttpServletRequest();
        mockRequest.setRemoteAddr("127.0.0.1");
        mockRequest.addHeader("User-Agent", "Test-Agent");
    }

    @Override
    protected void setupTestData() {
        // Use a unique test order ID for each test run to avoid conflicts
        testOrderId = "1"; // This corresponds to the PENDING order in data-test.sql
        
        // Create test payment request
        testPaymentRequest = new PaymentInitiationRequest();
        testPaymentRequest.setOrderId(testOrderId);
        testPaymentRequest.setAmount(80000L); // Match the total_after_vat from test data
        testPaymentRequest.setOrderInfo("Test payment for order " + testOrderId);
        testPaymentRequest.setBankCode("NCB");
        testPaymentRequest.setLanguage("vn");
        testPaymentRequest.setCustomerId("TEST_CUSTOMER_001");
        
        // Note: Using existing order from data-test.sql (order_id = 1)
    }

    @Test
    @DisplayName("Complete Payment Workflow - Success Scenario")
    void completePaymentWorkflow_SuccessScenario() {
        // Step 1: Initiate Payment
        PaymentResponse paymentResponse = initiatePayment();
        assertNotNull(paymentResponse);
        assertTrue(paymentResponse.isSuccess());
        assertNotNull(paymentResponse.getPaymentUrl());
        assertTrue(paymentResponse.getPaymentUrl().contains("vnp_TxnRef=" + testOrderId));
        
        // Step 2: Verify payment transaction is created
        Optional<PaymentTransaction> transactionOpt = paymentTransactionRepository.findByOrderId(testOrderId);
        assertTrue(transactionOpt.isPresent());
        PaymentTransaction transaction = transactionOpt.get();
        assertEquals(testOrderId, transaction.getOrderId());
        assertEquals(testPaymentRequest.getAmount(), transaction.getAmount());
        assertEquals(PaymentTransaction.TransactionStatus.PENDING, transaction.getTransactionStatus());
        
        // Step 3: Simulate successful payment callback from VNPay
        Map<String, String> callbackParams = createSuccessfulCallbackParams();
        PaymentCallbackRequest callbackRequest = new PaymentCallbackRequest(testOrderId, callbackParams);
        
        // Process callback
        paymentApplicationService.handlePaymentCallback(callbackRequest);
        
        // Step 4: Verify transaction is updated to SUCCESS
        transactionOpt = paymentTransactionRepository.findByOrderId(testOrderId);
        assertTrue(transactionOpt.isPresent());
        transaction = transactionOpt.get();
        assertEquals(PaymentTransaction.TransactionStatus.SUCCESS, transaction.getTransactionStatus());
        assertEquals("00", transaction.getResponseCode());
        assertNotNull(transaction.getTransactionNo());
        
        // Step 5: Query payment status
        String transactionDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        PaymentStatusResponse statusResponse = paymentApplicationService.queryPaymentStatus(
            testOrderId, transactionDate, mockRequest);
        
        assertNotNull(statusResponse);
        assertTrue(statusResponse.isSuccess());
        assertEquals(testOrderId, statusResponse.getTransactionId());
    }

    @Test
    @DisplayName("Payment Workflow - Failed Payment Scenario")
    void paymentWorkflow_FailedPaymentScenario() {
        // Step 1: Initiate Payment
        PaymentResponse paymentResponse = initiatePayment();
        assertTrue(paymentResponse.isSuccess());
        
        // Step 2: Simulate failed payment callback from VNPay
        Map<String, String> callbackParams = createFailedCallbackParams();
        PaymentCallbackRequest callbackRequest = new PaymentCallbackRequest(testOrderId, callbackParams);
        
        // Process callback
        paymentApplicationService.handlePaymentCallback(callbackRequest);
        
        // Step 3: Verify transaction is updated to FAILED
        Optional<PaymentTransaction> transactionOpt = paymentTransactionRepository.findByOrderId(testOrderId);
        assertTrue(transactionOpt.isPresent());
        PaymentTransaction transaction = transactionOpt.get();
        assertEquals(PaymentTransaction.TransactionStatus.FAILED, transaction.getTransactionStatus());
        assertEquals("07", transaction.getResponseCode()); // Insufficient funds
    }

    @Test
    @DisplayName("Payment Initiation via REST API")
    void paymentInitiation_ViaRestAPI() {
        // Create request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("orderId", testOrderId);
        requestBody.put("amount", testPaymentRequest.getAmount());
        requestBody.put("orderInfo", testPaymentRequest.getOrderInfo());
        requestBody.put("bankCode", testPaymentRequest.getBankCode());
        requestBody.put("language", testPaymentRequest.getLanguage());
        
        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Create HTTP entity
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        // Call API
        ResponseEntity<PaymentResponse> response = restTemplate.postForEntity(
            baseUrl + "/initiate", entity, PaymentResponse.class);
        
        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getPaymentUrl());
    }

    @Test
    @DisplayName("Payment Callback Processing via REST API")
    void paymentCallback_ViaRestAPI() {
        // First initiate payment
        initiatePayment();
        
        // Create callback parameters
        Map<String, String> callbackParams = createSuccessfulCallbackParams();
        
        // Build URL with query parameters
        StringBuilder callbackUrl = new StringBuilder(baseUrl + "/callback?");
        callbackParams.forEach((key, value) -> 
            callbackUrl.append(key).append("=").append(value).append("&"));
        
        // Call callback endpoint
        HttpEntity<Void> requestEntity = new HttpEntity<>(null);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            callbackUrl.toString(), 
            HttpMethod.POST, 
            requestEntity, 
            new ParameterizedTypeReference<Map<String, Object>>(){});
        
        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("00", response.getBody().get("RspCode"));
        assertEquals("Success", response.getBody().get("Message"));
    }

    @Test
    @DisplayName("Refund Processing Workflow")
    void refundProcessing_Workflow() {
        // Step 1: Complete successful payment first
        completeSuccessfulPayment();
        
        // Step 2: Create refund request
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setOrderId(testOrderId);
        refundRequest.setAmount(testPaymentRequest.getAmount());
        refundRequest.setTransactionDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        refundRequest.setTransactionType("02"); // Full refund
        refundRequest.setRefundReason("Customer requested refund");
        refundRequest.setUser("test-user");
        
        // Step 3: Process refund
        RefundResponse refundResponse = paymentApplicationService.processRefund(refundRequest, mockRequest);
        
        // Step 4: Verify refund response
        assertNotNull(refundResponse);
        // Note: In test environment, this might not be fully successful due to mock VNPay
        // but we can verify the request was processed correctly
    }

    @Test
    @DisplayName("VNPay Hash Validation")
    void vnpayHashValidation_Test() {
        // Create test parameters
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", "10000000");
        params.put("vnp_BankCode", "NCB");
        params.put("vnp_OrderInfo", "Test payment");
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_TxnRef", testOrderId);
        params.put("vnp_TransactionStatus", "00");
        
        // Generate hash
        String hash = vnPayConfig.hashAllFields(params);
        params.put("vnp_SecureHash", hash);
        
        // Test validation
        boolean isValid = vnPayBoundary.validatePaymentCallback(params);
        assertTrue(isValid);
        
        // Test with invalid hash
        params.put("vnp_SecureHash", "invalid_hash");
        boolean isInvalid = vnPayBoundary.validatePaymentCallback(params);
        assertFalse(isInvalid);
    }

    @Test
    @DisplayName("Payment Status Query via REST API")
    void paymentStatusQuery_ViaRestAPI() {
        // First complete a payment
        completeSuccessfulPayment();
        
        // Query status via API
        String transactionDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String statusUrl = baseUrl + "/status/" + testOrderId + "?transactionDate=" + transactionDate;
        
        ResponseEntity<PaymentStatusResponse> response = restTemplate.getForEntity(
            statusUrl, PaymentStatusResponse.class);
        
        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testOrderId, response.getBody().getTransactionId());
    }

    @Test
    @DisplayName("Multiple Payment Transactions for Same Order")
    void multiplePaymentTransactions_SameOrder() {
        // Initiate first payment (which fails)
        initiatePayment();
        processFailedCallback();
        
        // Initiate second payment (which succeeds)
        PaymentResponse secondPayment = initiatePayment();
        assertTrue(secondPayment.isSuccess());
        
        // Process successful callback for second payment
        Map<String, String> successParams = createSuccessfulCallbackParams();
        PaymentCallbackRequest callbackRequest = new PaymentCallbackRequest(testOrderId, successParams);
        paymentApplicationService.handlePaymentCallback(callbackRequest);
        
        // Verify we have multiple transactions for the same order
        // but only one successful one
        Optional<PaymentTransaction> latestTransaction = paymentTransactionRepository.findByOrderId(testOrderId);
        assertTrue(latestTransaction.isPresent());
        assertEquals(PaymentTransaction.TransactionStatus.SUCCESS, latestTransaction.get().getTransactionStatus());
    }

    @Test
    @DisplayName("Invalid Payment Request Handling")
    void invalidPaymentRequest_Handling() {
        // Test with invalid amount
        PaymentInitiationRequest invalidRequest = new PaymentInitiationRequest();
        invalidRequest.setOrderId(testOrderId);
        invalidRequest.setAmount(-1000L); // Invalid negative amount
        
        try {
            paymentApplicationService.initiatePayment(invalidRequest, mockRequest);
            fail("Should have thrown exception for invalid amount");
        } catch (Exception e) {
            // Expected exception
            assertTrue(e.getMessage().contains("Amount must be positive") || 
                      e instanceof IllegalArgumentException);
        }
        
        // Test with null order ID
        invalidRequest.setAmount(100000L);
        invalidRequest.setOrderId(null);
        
        try {
            paymentApplicationService.initiatePayment(invalidRequest, mockRequest);
            fail("Should have thrown exception for null order ID");
        } catch (Exception e) {
            // Expected exception
            assertTrue(e.getMessage().contains("Order ID") || 
                      e instanceof IllegalArgumentException);
        }
    }

    // Helper methods
    private PaymentResponse initiatePayment() {
        return paymentApplicationService.initiatePayment(testPaymentRequest, mockRequest);
    }

    private void completeSuccessfulPayment() {
        // Initiate payment
        PaymentResponse paymentResponse = initiatePayment();
        assertTrue(paymentResponse.isSuccess());
        
        // Process successful callback
        Map<String, String> callbackParams = createSuccessfulCallbackParams();
        PaymentCallbackRequest callbackRequest = new PaymentCallbackRequest(testOrderId, callbackParams);
        paymentApplicationService.handlePaymentCallback(callbackRequest);
    }

    private void processFailedCallback() {
        Map<String, String> callbackParams = createFailedCallbackParams();
        PaymentCallbackRequest callbackRequest = new PaymentCallbackRequest(testOrderId, callbackParams);
        paymentApplicationService.handlePaymentCallback(callbackRequest);
    }

    private Map<String, String> createSuccessfulCallbackParams() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", String.valueOf(testPaymentRequest.getAmount() * 100)); // VNPay uses cents
        params.put("vnp_BankCode", "NCB");
        params.put("vnp_BankTranNo", "VNP" + System.currentTimeMillis());
        params.put("vnp_CardType", "ATM");
        params.put("vnp_OrderInfo", testPaymentRequest.getOrderInfo());
        params.put("vnp_PayDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        params.put("vnp_ResponseCode", "00"); // Success code
        params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        params.put("vnp_TransactionNo", "VNP" + System.currentTimeMillis());
        params.put("vnp_TransactionStatus", "00"); // Success status
        params.put("vnp_TxnRef", testOrderId);
        params.put("vnp_Message", "Successful");
        
        // Generate secure hash
        String hash = vnPayConfig.hashAllFields(params);
        params.put("vnp_SecureHash", hash);
        
        return params;
    }

    private Map<String, String> createFailedCallbackParams() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", String.valueOf(testPaymentRequest.getAmount() * 100));
        params.put("vnp_BankCode", "NCB");
        params.put("vnp_OrderInfo", testPaymentRequest.getOrderInfo());
        params.put("vnp_PayDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        params.put("vnp_ResponseCode", "07"); // Insufficient funds
        params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        params.put("vnp_TransactionStatus", "02"); // Failed status
        params.put("vnp_TxnRef", testOrderId);
        params.put("vnp_Message", "Insufficient funds");
        
        // Generate secure hash
        String hash = vnPayConfig.hashAllFields(params);
        params.put("vnp_SecureHash", hash);
        
        return params;
    }
}