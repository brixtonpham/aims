/**
 * VNPay Payment Flow Complete Test
 * Demonstrates the complete 8-step business flow
 */
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class VNPayCompleteFlowTest {
    
    // VNPay Sandbox Configuration
    private static final String VNP_TMN_CODE = "YFW5M6GN";
    private static final String VNP_SECRET_KEY = "3RCPI4281FRSY2W6P3E9QD3JZJICJB5M";
    private static final String VNP_PAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private static final String VNP_RETURN_URL = "http://localhost:8080/api/payment/vnpay/return";
    
    public static void main(String[] args) {
        System.out.println("🏦 VNPay Complete Payment Flow Test");
        System.out.println("=====================================\n");
        
        // Test the complete 8-step flow
        testCompletePaymentFlow();
        
        // Test callback scenarios
        System.out.println("\n" + "=".repeat(50));
        testCallbackScenarios();
    }
    
    public static void testCompletePaymentFlow() {
        System.out.println("📋 Step-by-Step Business Flow:");
        System.out.println("------------------------------");
        
        // Step 1: Get Order Information (Order ID, Amount)
        System.out.println("✅ Step 1: Get Order Information");
        String orderId = "ORDER_" + System.currentTimeMillis();
        long amount = 150000; // 150,000 VND
        System.out.println("   - Order ID: " + orderId);
        System.out.println("   - Amount: " + amount + " VND");
        
        // Step 2: Create VNPay Payment Request
        System.out.println("\n✅ Step 2: Create VNPay Payment Request");
        Map<String, String> paymentParams = createPaymentRequest(orderId, amount, "NCB", "vn");
        System.out.println("   - Payment request created successfully");
        System.out.println("   - Bank Code: NCB (National Citizen Bank)");
        System.out.println("   - Language: Vietnamese");
        
        // Step 3: Generate Payment URL
        System.out.println("\n✅ Step 3: Generate Payment URL");
        String paymentUrl = generatePaymentUrl(paymentParams);
        System.out.println("   - Payment URL generated successfully");
        System.out.println("   - URL Length: " + paymentUrl.length() + " characters");
        
        // Display the complete payment URL
        System.out.println("\n🔗 Generated Payment URL:");
        System.out.println("-".repeat(50));
        System.out.println(paymentUrl);
        System.out.println("-".repeat(50));
        
        // Step 4: Redirect User to VNPay Gateway
        System.out.println("\n🔄 Step 4: Redirect User to VNPay Gateway");
        System.out.println("   - User will be redirected to VNPay's payment page");
        System.out.println("   - VNPay URL: " + VNP_PAY_URL);
        System.out.println("   - User sees payment form with amount: " + amount + " VND");
        
        // Step 5: User Completes Payment on VNPay
        System.out.println("\n⏳ Step 5: User Completes Payment on VNPay");
        System.out.println("   - User selects payment method (NCB Bank)");
        System.out.println("   - User enters banking credentials");
        System.out.println("   - User confirms payment");
        
        // Step 6: VNPay Callback with Result
        System.out.println("\n⏳ Step 6: VNPay Callback with Result");
        System.out.println("   - VNPay processes payment");
        System.out.println("   - VNPay redirects to: " + VNP_RETURN_URL);
        System.out.println("   - Callback includes payment result parameters");
        
        // Analyze the payment URL parameters
        analyzePaymentUrl(paymentParams);
    }
    
    public static void testCallbackScenarios() {
        System.out.println("🧪 Testing Callback Scenarios:");
        System.out.println("-------------------------------");
        
        String orderId = "ORDER_TEST_" + System.currentTimeMillis();
        
        // Test Success Callback (Step 7-8)
        System.out.println("\n✅ Success Callback Simulation:");
        Map<String, String> successParams = new HashMap<>();
        successParams.put("vnp_Amount", "15000000");
        successParams.put("vnp_BankCode", "NCB");
        successParams.put("vnp_OrderInfo", "Payment for order " + orderId);
        successParams.put("vnp_ResponseCode", "00");
        successParams.put("vnp_TxnRef", orderId);
        successParams.put("vnp_TransactionNo", "14123456");
        successParams.put("vnp_PayDate", "20250627182000");
        successParams.put("vnp_TransactionStatus", "00");
        
        processPaymentCallback(successParams);
        
        // Test Failed Callback (Step 7-8)
        System.out.println("\n❌ Failed Callback Simulation:");
        Map<String, String> failedParams = new HashMap<>();
        failedParams.put("vnp_Amount", "15000000");
        failedParams.put("vnp_BankCode", "NCB");
        failedParams.put("vnp_OrderInfo", "Payment for order " + orderId);
        failedParams.put("vnp_ResponseCode", "24"); // User cancelled
        failedParams.put("vnp_TxnRef", orderId);
        
        processPaymentCallback(failedParams);
    }
    
    public static Map<String, String> createPaymentRequest(String orderId, long amount, String bankCode, String language) {
        Map<String, String> params = new LinkedHashMap<>();
        
        // VNPay required parameters
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", VNP_TMN_CODE);
        params.put("vnp_Amount", String.valueOf(amount * 100)); // Convert to smallest unit
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", orderId);
        params.put("vnp_OrderInfo", "Payment for order " + orderId);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", language);
        params.put("vnp_ReturnUrl", VNP_RETURN_URL);
        params.put("vnp_IpAddr", "127.0.0.1");
        
        if (bankCode != null && !bankCode.isEmpty()) {
            params.put("vnp_BankCode", bankCode);
        }
        
        // Add timestamps
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        
        String createDate = formatter.format(calendar.getTime());
        params.put("vnp_CreateDate", createDate);
        
        calendar.add(Calendar.MINUTE, 15);
        String expireDate = formatter.format(calendar.getTime());
        params.put("vnp_ExpireDate", expireDate);
        
        return params;
    }
    
    public static String generatePaymentUrl(Map<String, String> params) {
        try {
            // Step 1: Sort parameters
            List<String> fieldNames = new ArrayList<>(params.keySet());
            Collections.sort(fieldNames);
            
            // Step 2: Create hash data (without URL encoding)
            StringBuilder hashData = new StringBuilder();
            for (int i = 0; i < fieldNames.size(); i++) {
                String key = fieldNames.get(i);
                String value = params.get(key);
                if (value != null && !value.isEmpty()) {
                    hashData.append(key).append("=").append(value);
                    if (i < fieldNames.size() - 1) {
                        hashData.append("&");
                    }
                }
            }
            
            // Step 3: Generate secure hash
            String secureHash = hmacSHA512(VNP_SECRET_KEY, hashData.toString());
            
            // Step 4: Create query string (with URL encoding)
            StringBuilder query = new StringBuilder();
            for (int i = 0; i < fieldNames.size(); i++) {
                String key = fieldNames.get(i);
                String value = params.get(key);
                if (value != null && !value.isEmpty()) {
                    query.append(URLEncoder.encode(key, StandardCharsets.UTF_8))
                         .append("=")
                         .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
                    if (i < fieldNames.size() - 1) {
                        query.append("&");
                    }
                }
            }
            
            // Add secure hash
            query.append("&vnp_SecureHash=").append(secureHash);
            
            return VNP_PAY_URL + "?" + query.toString();
            
        } catch (Exception e) {
            System.err.println("Error generating payment URL: " + e.getMessage());
            return null;
        }
    }
    
    public static void processPaymentCallback(Map<String, String> callbackParams) {
        String responseCode = callbackParams.get("vnp_ResponseCode");
        String orderId = callbackParams.get("vnp_TxnRef");
        String amount = callbackParams.get("vnp_Amount");
        
        // Step 7: Process Payment Result
        if ("00".equals(responseCode)) {
            System.out.println("   ✅ Step 7: Payment Result - SUCCESS");
            System.out.println("      - Response Code: " + responseCode + " (Success)");
            System.out.println("      - Order ID: " + orderId);
            System.out.println("      - Amount: " + (Long.parseLong(amount) / 100) + " VND");
            System.out.println("      - Transaction ID: " + callbackParams.get("vnp_TransactionNo"));
            
            // Step 8: Update Order Status + Save Transaction Info
            System.out.println("   ✅ Step 8: Update Order Status");
            System.out.println("      - Order status changed: PENDING → PAID");
            System.out.println("      - Transaction info saved to database");
            System.out.println("      - Customer notification sent");
            System.out.println("      - Inventory updated");
            
        } else {
            System.out.println("   ❌ Step 7: Payment Result - FAILED");
            System.out.println("      - Response Code: " + responseCode + " (" + getErrorMessage(responseCode) + ")");
            System.out.println("      - Order ID: " + orderId);
            
            // Step 8: Keep Order Status as PENDING
            System.out.println("   ⚠️ Step 8: Keep Order Status");
            System.out.println("      - Order status remains: PENDING");
            System.out.println("      - Customer can retry payment");
            System.out.println("      - Failure reason logged");
        }
    }
    
    public static void analyzePaymentUrl(Map<String, String> params) {
        System.out.println("\n📊 Payment URL Parameter Analysis:");
        System.out.println("-----------------------------------");
        
        System.out.printf("%-20s | %-30s | %s%n", "Parameter", "Value", "Description");
        System.out.println("-".repeat(80));
        
        params.forEach((key, value) -> {
            String description = getParameterDescription(key);
            System.out.printf("%-20s | %-30s | %s%n", key, 
                value.length() > 30 ? value.substring(0, 27) + "..." : value, 
                description);
        });
        
        System.out.println("-".repeat(80));
        System.out.println("Total Parameters: " + params.size());
    }
    
    public static String getParameterDescription(String param) {
        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("vnp_Version", "API Version");
        descriptions.put("vnp_Command", "Command Type");
        descriptions.put("vnp_TmnCode", "Terminal Code");
        descriptions.put("vnp_Amount", "Amount (VND × 100)");
        descriptions.put("vnp_CurrCode", "Currency Code");
        descriptions.put("vnp_TxnRef", "Transaction Reference");
        descriptions.put("vnp_OrderInfo", "Order Information");
        descriptions.put("vnp_OrderType", "Order Type");
        descriptions.put("vnp_Locale", "Language");
        descriptions.put("vnp_ReturnUrl", "Return URL");
        descriptions.put("vnp_IpAddr", "IP Address");
        descriptions.put("vnp_CreateDate", "Creation Date");
        descriptions.put("vnp_ExpireDate", "Expiration Date");
        descriptions.put("vnp_BankCode", "Bank Code");
        
        return descriptions.getOrDefault(param, "Unknown parameter");
    }
    
    public static String getErrorMessage(String responseCode) {
        Map<String, String> errorMessages = new HashMap<>();
        errorMessages.put("00", "Success");
        errorMessages.put("07", "Money deducted successfully");
        errorMessages.put("09", "Card not registered for online payment");
        errorMessages.put("10", "Authentication failed");
        errorMessages.put("11", "Transaction timeout");
        errorMessages.put("12", "Card/account locked");
        errorMessages.put("13", "Wrong OTP");
        errorMessages.put("24", "Transaction cancelled by user");
        errorMessages.put("51", "Insufficient balance");
        errorMessages.put("65", "Daily limit exceeded");
        errorMessages.put("75", "Bank under maintenance");
        errorMessages.put("79", "Password attempts exceeded");
        errorMessages.put("99", "Other error");
        
        return errorMessages.getOrDefault(responseCode, "Unknown error code");
    }
    
    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(secretKey);
            byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC SHA512", e);
        }
    }
}
