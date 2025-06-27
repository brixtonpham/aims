package com.aims.vnpay;

import com.aims.vnpay.common.config.VNPayConfig;
import com.aims.vnpay.common.dto.PaymentRequest;
import com.aims.vnpay.common.service.VNPayService;
import com.aims.vnpay.common.service.VNPayService.PaymentResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for VNPay payment flow
 * Tests the complete business flow from payment request to URL generation
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class VNPayFlowTest {

    private VNPayService vnPayService;
    private VNPayConfig vnPayConfig;

    @Mock
    private HttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup VNPay configuration with test values
        vnPayConfig = new VNPayConfig();
        vnPayConfig.setTmnCode("YFW5M6GN");
        vnPayConfig.setSecretKey("3RCPI4281FRSY2W6P3E9QD3JZJICJB5M");
        vnPayConfig.setPayUrl("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
        vnPayConfig.setReturnUrl("http://localhost:8080/api/payment/vnpay/return");
        vnPayConfig.setApiUrl("https://sandbox.vnpayment.vn/merchant_webapi/api/transaction");
        
        vnPayService = new VNPayService(vnPayConfig);
        
        // Mock HTTP request
        when(mockRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(mockRequest.getHeader("X-Forwarded-For")).thenReturn(null);
    }

    // Step 1: Test Order Information Creation
    @Test
    void testStep1_OrderInformationCreation() {
        System.out.println("=== Step 1: Order Information Creation ===");
        
        String orderId = "12345678";
        String amount = "100000"; // 100,000 VND
        
        // Validate order information
        assertNotNull(orderId);
        assertFalse(orderId.isEmpty());
        assertTrue(orderId.matches("\\d+"));
        
        assertNotNull(amount);
        assertTrue(Long.parseLong(amount) > 0);
        
        System.out.println("✓ Order ID: " + orderId);
        System.out.println("✓ Amount: " + amount + " VND");
        System.out.println("✓ Order information validation passed");
    }
    
    // Step 2: Test VNPay Payment Request Creation
    @Test
    void testStep2_CreateVNPayPaymentRequest() {
        System.out.println("=== Step 2: Create VNPay Payment Request ===");
        
        String amount = "100000";
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(amount);
        paymentRequest.setBankCode(""); // Optional
        paymentRequest.setLanguage("vn");
        paymentRequest.setVnp_Version("2.1.0");
        
        // Validate payment request
        assertNotNull(paymentRequest);
        assertEquals(amount, paymentRequest.getAmount());
        assertEquals("vn", paymentRequest.getLanguage());
        assertEquals("2.1.0", paymentRequest.getVnp_Version());
        
        System.out.println("✓ Payment Request created successfully");
        System.out.println("✓ Amount: " + paymentRequest.getAmount());
        System.out.println("✓ Language: " + paymentRequest.getLanguage());
        System.out.println("✓ Version: " + paymentRequest.getVnp_Version());
    }
    
    // Step 3: Test Payment URL Generation
    @Test
    void testStep3_GeneratePaymentURL() {
        System.out.println("=== Step 3: Generate Payment URL ===");
        
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount("100000");
        paymentRequest.setBankCode("");
        paymentRequest.setLanguage("vn");
        paymentRequest.setVnp_Version("2.1.0");
        
        PaymentResponse response = vnPayService.createPayment(paymentRequest, mockRequest);
        
        // Validate response
        assertNotNull(response);
        assertEquals("00", response.getCode());
        assertEquals("success", response.getMessage());
        assertNotNull(response.getPaymentUrl());
        
        System.out.println("✓ Payment URL generated successfully");
        System.out.println("✓ Response Code: " + response.getCode());
        System.out.println("✓ Response Message: " + response.getMessage());
        System.out.println("✓ Payment URL: " + response.getPaymentUrl());
    }
    
    // Step 4: Test Payment URL Structure Validation
    @Test
    void testStep4_ValidatePaymentURLStructure() {
        System.out.println("=== Step 4: Validate Payment URL Structure ===");
        
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount("100000");
        paymentRequest.setLanguage("vn");
        paymentRequest.setVnp_Version("2.1.0");
        
        PaymentResponse response = vnPayService.createPayment(paymentRequest, mockRequest);
        String paymentUrl = response.getPaymentUrl();
        
        // Validate URL structure
        assertTrue(paymentUrl.startsWith("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?"));
        assertTrue(paymentUrl.contains("vnp_Version=2.1.0"));
        assertTrue(paymentUrl.contains("vnp_Command=pay"));
        assertTrue(paymentUrl.contains("vnp_TmnCode=YFW5M6GN"));
        assertTrue(paymentUrl.contains("vnp_Amount=10000000")); // 100,000 * 100
        assertTrue(paymentUrl.contains("vnp_CurrCode=VND"));
        assertTrue(paymentUrl.contains("vnp_ReturnUrl="));
        assertTrue(paymentUrl.contains("vnp_SecureHash="));
        
        System.out.println("✓ URL starts with correct VNPay domain");
        System.out.println("✓ Contains required vnp_Version parameter");
        System.out.println("✓ Contains required vnp_Command parameter");
        System.out.println("✓ Contains correct vnp_TmnCode");
        System.out.println("✓ Contains correct amount (converted to smallest unit)");
        System.out.println("✓ Contains currency code (VND)");
        System.out.println("✓ Contains return URL");
        System.out.println("✓ Contains secure hash for verification");
    }
    
    // Step 5: Test Payment URL Parameters Extraction
    @Test
    void testStep5_ExtractPaymentURLParameters() {
        System.out.println("=== Step 5: Extract Payment URL Parameters ===");
        
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount("250000");
        paymentRequest.setLanguage("vn");
        paymentRequest.setVnp_Version("2.1.0");
        
        PaymentResponse response = vnPayService.createPayment(paymentRequest, mockRequest);
        String paymentUrl = response.getPaymentUrl();
        
        // Extract and validate key parameters
        String[] urlParts = paymentUrl.split("\\?")[1].split("&");
        boolean hasTxnRef = false;
        boolean hasAmount = false;
        boolean hasCreateDate = false;
        
        for (String param : urlParts) {
            if (param.startsWith("vnp_TxnRef=")) {
                hasTxnRef = true;
                System.out.println("✓ Transaction Reference: " + param.split("=")[1]);
            } else if (param.startsWith("vnp_Amount=")) {
                hasAmount = true;
                String amountParam = param.split("=")[1];
                System.out.println("✓ Amount (VND * 100): " + amountParam);
                assertEquals("25000000", amountParam); // 250,000 * 100
            } else if (param.startsWith("vnp_CreateDate=")) {
                hasCreateDate = true;
                System.out.println("✓ Create Date: " + param.split("=")[1]);
            }
        }
        
        assertTrue(hasTxnRef, "Transaction reference should be present");
        assertTrue(hasAmount, "Amount should be present");
        assertTrue(hasCreateDate, "Create date should be present");
    }
    
    // Step 6: Test Different Amount Values
    @Test
    void testStep6_TestDifferentAmounts() {
        System.out.println("=== Step 6: Test Different Amount Values ===");
        
        String[] testAmounts = {"50000", "100000", "500000", "1000000"};
        
        for (String amount : testAmounts) {
            PaymentRequest request = new PaymentRequest();
            request.setAmount(amount);
            request.setLanguage("vn");
            request.setVnp_Version("2.1.0");
            
            PaymentResponse response = vnPayService.createPayment(request, mockRequest);
            
            assertNotNull(response);
            assertEquals("00", response.getCode());
            
            // Verify the amount is correctly multiplied by 100
            String expectedAmount = String.valueOf(Long.parseLong(amount) * 100);
            assertTrue(response.getPaymentUrl().contains("vnp_Amount=" + expectedAmount));
            
            System.out.println("✓ Amount: " + amount + " VND -> URL contains: " + expectedAmount);
        }
    }
    
    // Step 7: Test Bank Code Integration
    @Test
    void testStep7_TestBankCodeIntegration() {
        System.out.println("=== Step 7: Test Bank Code Integration ===");
        
        String[] bankCodes = {"", "NCB", "BIDV", "VCB", "TECHCOMBANK"};
        
        for (String bankCode : bankCodes) {
            PaymentRequest request = new PaymentRequest();
            request.setAmount("200000");
            request.setBankCode(bankCode);
            request.setLanguage("vn");
            request.setVnp_Version("2.1.0");
            
            PaymentResponse response = vnPayService.createPayment(request, mockRequest);
            
            assertNotNull(response);
            assertEquals("00", response.getCode());
            
            if (!bankCode.isEmpty()) {
                assertTrue(response.getPaymentUrl().contains("vnp_BankCode=" + bankCode));
                System.out.println("✓ Bank code '" + bankCode + "' correctly included in URL");
            } else {
                System.out.println("✓ No bank code specified - general payment gateway");
            }
        }
    }
    
    // Step 8: Test Complete Flow Integration
    @Test
    void testStep8_CompleteFlowIntegration() {
        System.out.println("=== Step 8: Complete Flow Integration Test ===");
        
        // Step 1: Order Information
        String orderId = "ORDER_" + System.currentTimeMillis();
        String amount = "150000";
        
        // Step 2: Create Payment Request
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(amount);
        paymentRequest.setBankCode("NCB");
        paymentRequest.setLanguage("vn");
        paymentRequest.setVnp_Version("2.1.0");
        
        // Step 3: Generate Payment URL
        PaymentResponse response = vnPayService.createPayment(paymentRequest, mockRequest);
        
        // Step 4: Validate Complete Response
        assertNotNull(response);
        assertEquals("00", response.getCode());
        assertEquals("success", response.getMessage());
        assertNotNull(response.getPaymentUrl());
        assertNotNull(response.getIpAddress());
        
        String paymentUrl = response.getPaymentUrl();
        
        System.out.println("✓ Complete flow executed successfully");
        System.out.println("✓ Order ID: " + orderId);
        System.out.println("✓ Amount: " + amount + " VND");
        System.out.println("✓ Generated Payment URL:");
        System.out.println("  " + paymentUrl);
        
        // Final validation
        assertTrue(paymentUrl.contains("vnp_Amount=15000000")); // 150,000 * 100
        assertTrue(paymentUrl.contains("vnp_BankCode=NCB"));
        assertTrue(paymentUrl.contains("vnp_ReturnUrl="));
        
        System.out.println("\n--- Next Steps (Manual) ---");
        System.out.println("1. Copy the payment URL above");
        System.out.println("2. Open it in a browser to test the payment");
        System.out.println("3. Complete payment on VNPay sandbox");
        System.out.println("4. Check callback at: " + vnPayConfig.getReturnUrl());
    }
    
    
    // Quick Test: Generate a Payment URL
    @Test
    void testQuickPaymentURLGeneration() {
        System.out.println("=== Quick Payment URL Generation ===");
        
        PaymentRequest request = new PaymentRequest();
        request.setAmount("100000"); // 100,000 VND
        request.setLanguage("vn");
        request.setVnp_Version("2.1.0");
        
        PaymentResponse response = vnPayService.createPayment(request, mockRequest);
        
        System.out.println("Generated Payment URL:");
        System.out.println(response.getPaymentUrl());
        System.out.println("\nCopy this URL and paste it in your browser to test the payment flow!");
        
        assertNotNull(response.getPaymentUrl());
        assertTrue(response.getPaymentUrl().contains("sandbox.vnpayment.vn"));
    }
}
