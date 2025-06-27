package com.aims.vnpay.test;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Test callback handler for VNPay payment return
 * This simulates processing the callback after payment completion
 */
@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class VNPayCallbackTestController {

    private static final Logger logger = LoggerFactory.getLogger(VNPayCallbackTestController.class);

    /**
     * Handle VNPay return callback for testing
     * This endpoint simulates processing the payment result
     */
    @GetMapping("/vnpay-return")
    public Map<String, Object> handleVNPayReturn(@RequestParam Map<String, String> params, 
                                                HttpServletRequest request) {
        
        logger.info("=== VNPay Return Callback Test ===");
        logger.info("Callback received from: {}", request.getRemoteAddr());
        
        Map<String, Object> result = new HashMap<>();
        
        // Log all received parameters
        logger.info("Received parameters:");
        params.forEach((key, value) -> logger.info("  {} = {}", key, value));
        
        // Extract key parameters
        String responseCode = params.getOrDefault("vnp_ResponseCode", "99");
        String amount = params.getOrDefault("vnp_Amount", "0");
        String orderId = params.getOrDefault("vnp_TxnRef", "unknown");
        String transactionId = params.getOrDefault("vnp_TransactionNo", "");
        String bankCode = params.getOrDefault("vnp_BankCode", "");
        String payDate = params.getOrDefault("vnp_PayDate", "");
        
        // Process based on response code
        if ("00".equals(responseCode)) {
            // Step 7: Process Payment Result (Success)
            logger.info("Payment Status: SUCCESS");
            result.put("status", "SUCCESS");
            result.put("message", "Payment completed successfully");
            result.put("orderId", orderId);
            result.put("amount", Long.parseLong(amount) / 100); // Convert back to VND
            result.put("transactionId", transactionId);
            result.put("bankCode", bankCode);
            result.put("payDate", payDate);
            
            // Step 8: Update Order Status + Save Transaction Info
            logger.info("Step 8: Updating order status to PAID for order: {}", orderId);
            result.put("orderStatus", "PAID");
            result.put("nextAction", "Order has been marked as PAID and transaction info saved");
            
        } else {
            // Step 7: Process Payment Result (Failed)
            logger.info("Payment Status: FAILED ({})", responseCode);
            result.put("status", "FAILED");
            result.put("message", "Payment failed with code: " + responseCode + " - " + getResponseMessage(responseCode));
            result.put("orderId", orderId);
            result.put("responseCode", responseCode);
            
            // Step 8: Keep order status as PENDING
            logger.info("Step 8: Keeping order status as PENDING for order: {}", orderId);
            result.put("orderStatus", "PENDING");
            result.put("nextAction", "Order remains PENDING - customer can retry payment");
        }
        
        // Add flow completion info
        result.put("flowStep", "Steps 7-8 Completed");
        result.put("flowDescription", "Payment result processed and order status updated");
        result.put("receivedParams", params);
        
        logger.info("=== End VNPay Return Processing ===");
        
        return result;
    }
    
    /**
     * Get human-readable message for VNPay response codes
     */
    private String getResponseMessage(String code) {
        Map<String, String> messages = new HashMap<>();
        messages.put("00", "Transaction successful");
        messages.put("07", "Successful transaction. Money will be deducted from account");
        messages.put("09", "Transaction failed: Customer's card/account not registered for InternetBanking service");
        messages.put("10", "Transaction failed: Customer's card/account authentication failed");
        messages.put("11", "Transaction failed: Timeout. Please try again");
        messages.put("12", "Transaction failed: Customer's card/account is locked");
        messages.put("13", "Transaction failed: Wrong OTP");
        messages.put("24", "Transaction failed: Customer cancelled transaction");
        messages.put("51", "Transaction failed: Insufficient account balance");
        messages.put("65", "Transaction failed: Daily transaction limit exceeded");
        messages.put("75", "Transaction failed: Bank is under maintenance");
        messages.put("79", "Transaction failed: Exceeded password entry limit");
        messages.put("99", "Other error");
        
        return messages.getOrDefault(code, "Unknown error code: " + code);
    }
    
    /**
     * Create a test HTML page for manual callback testing
     */
    @GetMapping("/callback-test-page")
    public String getCallbackTestPage() {
        return """
            <html>
            <head><title>VNPay Callback Test</title></head>
            <body>
                <h1>VNPay Callback Test</h1>
                <p>Test different callback scenarios:</p>
                
                <h3>Success Callback</h3>
                <a href="/api/test/vnpay-return?vnp_Amount=15000000&vnp_BankCode=NCB&vnp_OrderInfo=Payment+for+order&vnp_ResponseCode=00&vnp_TxnRef=ORDER_12345&vnp_TransactionNo=14123456&vnp_PayDate=20250627182000&vnp_SecureHash=test">
                    Test Success Callback
                </a>
                
                <h3>Failed Callback (Cancelled)</h3>
                <a href="/api/test/vnpay-return?vnp_Amount=15000000&vnp_BankCode=NCB&vnp_OrderInfo=Payment+for+order&vnp_ResponseCode=24&vnp_TxnRef=ORDER_12345&vnp_SecureHash=test">
                    Test Failed Callback (Cancelled)
                </a>
                
                <h3>Failed Callback (Insufficient Funds)</h3>
                <a href="/api/test/vnpay-return?vnp_Amount=15000000&vnp_BankCode=NCB&vnp_OrderInfo=Payment+for+order&vnp_ResponseCode=51&vnp_TxnRef=ORDER_12345&vnp_SecureHash=test">
                    Test Failed Callback (Insufficient Funds)
                </a>
            </body>
            </html>
            """;
    }
}
