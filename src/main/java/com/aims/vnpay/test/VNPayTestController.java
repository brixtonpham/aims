package com.aims.vnpay.test;

import com.aims.vnpay.common.dto.PaymentRequest;
import com.aims.vnpay.common.service.VNPayService;
import com.aims.vnpay.common.service.VNPayService.PaymentResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Test controller for VNPay payment flow testing
 * Provides endpoints to test the payment flow manually
 */
@RestController
@RequestMapping("/api/test/vnpay")
@CrossOrigin(origins = "http://localhost:3000")
public class VNPayTestController {

    private static final Logger logger = LoggerFactory.getLogger(VNPayTestController.class);
    private static final String SUCCESS_KEY = "success";
    private static final String AMOUNT_KEY = "amount";
    private static final String ORDER_ID_KEY = "orderId";
    
    private final VNPayService vnPayService;

    @Autowired
    public VNPayTestController(VNPayService vnPayService) {
        this.vnPayService = vnPayService;
    }

    /**
     * Generate a payment URL for testing
     * 
     * @param amount Payment amount in VND
     * @param bankCode Optional bank code for direct payment
     * @param language Interface language (vn/en)
     * @param request HTTP request
     * @return Payment response with URL
     */
    @GetMapping("/generate-payment-url")
    public Map<String, Object> generatePaymentUrl(
            @RequestParam(defaultValue = "100000") String amount,
            @RequestParam(required = false) String bankCode,
            @RequestParam(defaultValue = "vn") String language,
            HttpServletRequest request) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Step 1: Create payment request
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setAmount(amount);
            paymentRequest.setBankCode(bankCode != null ? bankCode : "");
            paymentRequest.setLanguage(language);
            paymentRequest.setVnp_Version("2.1.0");
            
            // Step 2: Generate payment URL
            PaymentResponse response = vnPayService.createPayment(paymentRequest, request);
            
            // Step 3: Return detailed response
            result.put(SUCCESS_KEY, true);
            result.put("paymentUrl", response.getPaymentUrl());
            result.put("responseCode", response.getCode());
            result.put("responseMessage", response.getMessage());
            result.put("ipAddress", response.getIpAddress());
            result.put(AMOUNT_KEY, amount);
            result.put("amountInSmallestUnit", Long.parseLong(amount) * 100);
            result.put("bankCode", bankCode);
            result.put("language", language);
            
            // Step 4: Add flow instructions
            result.put("instructions", Map.of(
                "step1", "Use the payment URL to redirect user to VNPay",
                "step2", "User will complete payment on VNPay gateway",
                "step3", "VNPay will redirect back to your return URL",
                "step4", "Process the callback and update order status"
            ));
            
        } catch (Exception e) {
            result.put(SUCCESS_KEY, false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * Generate payment URL with specific order details
     */
    @PostMapping("/generate-payment")
    public Map<String, Object> generatePayment(@RequestBody Map<String, Object> orderData, 
                                             HttpServletRequest request) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Extract order information
            String orderId = (String) orderData.get(ORDER_ID_KEY);
            String amount = (String) orderData.get(AMOUNT_KEY);
            String bankCode = (String) orderData.getOrDefault("bankCode", "");
            String language = (String) orderData.getOrDefault("language", "vn");
            
            logger.info("=== VNPay Payment Generation ===");
            logger.info("Order ID: {}", orderId);
            logger.info("Amount: {} VND", amount);
            logger.info("Bank Code: {}", bankCode.isEmpty() ? "Not specified" : bankCode);
            logger.info("Language: {}", language);
            
            // Create payment request
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setAmount(amount);
            paymentRequest.setBankCode(bankCode);
            paymentRequest.setLanguage(language);
            paymentRequest.setVnp_Version("2.1.0");
            
            // Generate payment URL
            PaymentResponse response = vnPayService.createPayment(paymentRequest, request);
            
            logger.info("Generated Payment URL: {}", response.getPaymentUrl());
            logger.info("=== End Payment Generation ===");
            
            result.put(SUCCESS_KEY, true);
            result.put(ORDER_ID_KEY, orderId);
            result.put("paymentUrl", response.getPaymentUrl());
            result.put("responseCode", response.getCode());
            result.put("responseMessage", response.getMessage());
            result.put("flowSteps", Map.of(
                "current", "Payment URL generated",
                "next", "Redirect user to payment URL",
                "then", "User completes payment on VNPay",
                "finally", "Process callback and update order"
            ));
            
        } catch (Exception e) {
            result.put(SUCCESS_KEY, false);
            result.put("error", e.getMessage());
            logger.error("Error generating payment", e);
        }
        
        return result;
    }

    /**
     * Simulate payment return processing
     */
    @GetMapping("/simulate-return")
    public Map<String, Object> simulateReturn(@RequestParam Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        
        logger.info("=== Simulating VNPay Return ===");
        logger.info("Received parameters:");
        params.forEach((key, value) -> logger.info("  {} = {}", key, value));
        
        // Check if this is a successful payment
        String responseCode = params.getOrDefault("vnp_ResponseCode", "99");
        String amount = params.getOrDefault("vnp_Amount", "0");
        String orderId = params.getOrDefault("vnp_TxnRef", "unknown");
        
        if ("00".equals(responseCode)) {
            result.put("status", "SUCCESS");
            result.put("message", "Payment completed successfully");
            result.put(ORDER_ID_KEY, orderId);
            result.put(AMOUNT_KEY, Long.parseLong(amount) / 100); // Convert back to VND
            logger.info("Payment Status: SUCCESS");
        } else {
            result.put("status", "FAILED");
            result.put("message", "Payment failed with code: " + responseCode);
            result.put(ORDER_ID_KEY, orderId);
            logger.info("Payment Status: FAILED ({})", responseCode);
        }
        
        logger.info("=== End Return Simulation ===");
        
        return result;
    }

    /**
     * Get payment flow documentation
     */
    @GetMapping("/flow-documentation")
    public Map<String, Object> getFlowDocumentation() {
        Map<String, Object> flow = new HashMap<>();
        
        flow.put("title", "VNPay Payment Flow");
        flow.put("steps", Map.of(
            "1", "Get Order Information (Order ID, Amount)",
            "2", "Create VNPay Payment Request",
            "3", "Generate Payment URL",
            "4", "Redirect User to VNPay Gateway",
            "5", "User Completes Payment on VNPay",
            "6", "VNPay Callback with Result",
            "7", "Process Payment Result (Success/Failed)",
            "8", "Update Order Status + Save Transaction Info"
        ));
        
        flow.put("testEndpoints", Map.of(
            "generateUrl", "GET /api/test/vnpay/generate-payment-url?amount=100000&language=vn",
            "generatePayment", "POST /api/test/vnpay/generate-payment",
            "simulateReturn", "GET /api/test/vnpay/simulate-return?vnp_ResponseCode=00&vnp_Amount=10000000&vnp_TxnRef=12345"
        ));
        
        flow.put("configuration", Map.of(
            "payUrl", "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html",
            "returnUrl", "http://localhost:8080/api/payment/vnpay/return",
            "environment", "sandbox"
        ));
        
        return flow;
    }
}
