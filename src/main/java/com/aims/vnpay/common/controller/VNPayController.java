/**
 * Controller handling VNPAY payment integration endpoints
 * Provides REST APIs for payment creation, confirmation, query and refund operations
 */
package com.aims.vnpay.common.controller;

import com.aims.vnpay.common.service.PaymentService;
import com.aims.vnpay.common.service.VNPayService.PaymentResponse;
import com.aims.vnpay.common.service.VNPayService.QueryResponse;
import com.aims.vnpay.common.service.VNPayService.RefundResponse;
import com.aims.vnpay.common.dto.IPNResponse;
import com.aims.vnpay.common.dto.PaymentRequest;
import com.aims.vnpay.common.dto.PaymentReturnResponse;
import com.aims.vnpay.common.service.OrderService;
import com.aims.vnpay.common.dto.QueryRequest;
import com.aims.vnpay.common.dto.RefundRequest;
import com.aims.vnpay.common.observer.PaymentSubject;
import com.aims.vnpay.common.service.HashService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Refactored VNPayController - sử dụng các service đã tách và Observer Pattern
 * Giải quyết vấn đề mixed concerns và low cohesion
 */
@Controller
@CrossOrigin(origins = "http://localhost:3000")
public class VNPayController {

    private static final Logger logger = LoggerFactory.getLogger(VNPayController.class);
    
    private final PaymentService paymentService;
    private final OrderService orderService;
    private final HashService hashService;
    private final PaymentSubject paymentSubject;

    @Autowired
    public VNPayController(
            @Qualifier("vnpayService") PaymentService paymentService, 
            OrderService orderService,
            HashService hashService,
            PaymentSubject paymentSubject) {
        this.paymentService = paymentService;
        this.orderService = orderService;
        this.hashService = hashService;
        this.paymentSubject = paymentSubject;
    }

    /**
     * API endpoint for application info
     * @return application information
     */
    @GetMapping("/")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> index() {
        Map<String, Object> info = new HashMap<>();
        info.put("application", "AIMS VNPay Integration API");
        info.put("status", "running");
        info.put("endpoints", Map.of(
            "payment", "/api/payment",
            "query", "/api/payment/query",
            "refund", "/api/payment/refund",
            "ipn", "/ipn",
            "return", "/return"
        ));
        return ResponseEntity.ok(info);
    }

    /**
     * Handles the return URL from VNPAY after payment
     * Refactored để tách validation logic và business logic
     */
    @GetMapping("/return")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> returnPage(
            @RequestParam Map<String, String> requestParams,
            HttpServletRequest request) {
        
        Map<String, Object> result = new HashMap<>();
        
        // Create copy of params for hash calculation
        Map<String, String> fields = new HashMap<>(requestParams);
        
        // Get and remove hash from param map before recalculating
        String vnp_SecureHash = fields.get("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

        // Validate hash using HashService
        String signValue = hashService.hashAllFields(fields);
        boolean isValidHash = signValue.equals(vnp_SecureHash);
        
        result.put("validHash", isValidHash);
        result.put("receivedParams", requestParams);

        if (isValidHash) {
            // Parse and validate required fields
            try {
                String orderId = fields.get("vnp_TxnRef");
                result.put("transactionId", orderId);
                result.put("amount", Long.parseLong(fields.getOrDefault("vnp_Amount", "0")));
                result.put("orderInfo", fields.get("vnp_OrderInfo"));
                result.put("responseCode", fields.get("vnp_ResponseCode"));
                result.put("vnpayTransactionId", fields.get("vnp_TransactionNo"));
                result.put("bankCode", fields.get("vnp_BankCode"));
                result.put("transactionStatus", fields.get("vnp_TransactionStatus"));
                result.put("payDate", fields.get("vnp_PayDate"));
                
                // Determine payment status
                String responseCode = fields.get("vnp_ResponseCode");
                if ("00".equals(responseCode)) {
                    result.put("status", "SUCCESS");
                    result.put("message", "Payment completed successfully");
                    
                    // Process successful payment
                    processSuccessfulPayment(orderId, fields);
                    
                } else {
                    result.put("status", "FAILED");
                    result.put("message", "Payment failed with code: " + responseCode);
                    
                    // Process failed payment
                    processFailedPayment(orderId, responseCode);
                }

            } catch (Exception e) {
                // Log the error
                logger.error("Error processing return URL parameters", e);
                result.put("status", "ERROR");
                result.put("message", "Error processing payment information");
                result.put("validHash", false);
            }
        } else {
            result.put("status", "INVALID");
            result.put("message", "Invalid signature");
        }

        // Log transaction details
        logger.info("Payment return - TxnId: {}, Amount: {}, Status: {}, ResponseCode: {}",
            result.get("transactionId"),
            result.get("amount"),
            result.get("transactionStatus"),
            result.get("responseCode")
        );

        return ResponseEntity.ok(result);
    }

    /**
     * Process successful payment using Observer Pattern
     */
    private void processSuccessfulPayment(String orderId, Map<String, String> fields) {
        // Log toàn bộ fields để debug key thực tế
        logger.info("[DEBUG] processSuccessfulPayment - orderId: {}, fields: {}", orderId, fields);
        // Kiểm tra các trường quan trọng
        if (fields.get("vnp_TransactionNo") == null || fields.get("vnp_Amount") == null) {
            logger.warn("[DEBUG] Các trường quan trọng bị null. Danh sách key thực tế: {}", fields.keySet());
        }
        // Update order status
        orderService.updateOrderStatus(orderId, "PAID");
        // Save transaction info
        orderService.saveTransactionInfo(orderId, fields);
        // Notify observers using Observer Pattern
        PaymentResponse response = PaymentResponse.builder()
                .code("00")
                .message("success")
                .paymentUrl("")
                .ipAddress("")
                .build();
        paymentSubject.notifyPaymentSuccess(orderId, response);
    }

    /**
     * Process failed payment using Observer Pattern
     */
    private void processFailedPayment(String orderId, String errorCode) {
        // Update order status
        orderService.updateOrderStatus(orderId, "FAILED");
        
        // Notify observers using Observer Pattern
        paymentSubject.notifyPaymentFailed(orderId, "Payment failed with code: " + errorCode);
    }

    /**
     * Handles the VNPay return URL (alternative path)
     * Redirects VNPay return calls to the main return handler
     */
    @GetMapping("/vnpay/return")
    public RedirectView vnpayReturnPage(
            @RequestParam Map<String, String> requestParams,
            HttpServletRequest request) {

        Map<String, Object> result = new HashMap<>();
        Map<String, String> fields = new HashMap<>(requestParams);

        String vnp_SecureHash = fields.get("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

        // Validate hash using HashService
        String signValue = hashService.hashAllFields(fields);
        boolean isValidHash = signValue.equals(vnp_SecureHash);

        result.put("validHash", isValidHash);
        result.put("receivedParams", requestParams);

        // Thêm log debug toàn bộ fields key-value
        logger.info("[DEBUG] /vnpay/return fields: {}", fields);
        for (String key : fields.keySet()) {
            logger.info("[DEBUG] field key: '{}', value: '{}'", key, fields.get(key));
        }

        if (isValidHash) {
            // Parse and validate required fields
            try {
                String orderId = fields.get("vnp_TxnRef");
                result.put("transactionId", orderId);
                result.put("amount", Long.parseLong(fields.getOrDefault("vnp_Amount", "0")));
                result.put("orderInfo", fields.get("vnp_OrderInfo"));
                result.put("responseCode", fields.get("vnp_ResponseCode"));
                result.put("vnpayTransactionId", fields.get("vnp_TransactionNo"));
                result.put("bankCode", fields.get("vnp_BankCode"));
                result.put("transactionStatus", fields.get("vnp_TransactionStatus"));
                result.put("payDate", fields.get("vnp_PayDate"));
                
                // Determine payment status
                String responseCode = fields.get("vnp_ResponseCode");
                if ("00".equals(responseCode)) {
                    result.put("status", "SUCCESS");
                    result.put("message", "Payment completed successfully");
                    processSuccessfulPayment(orderId, fields);
                  
                } else {
                    result.put("status", "FAILED");
                    result.put("message", "Payment failed with code: " + responseCode);
                    processFailedPayment(orderId, responseCode);
                }
            } catch (Exception e) {
                logger.error("Error processing return URL parameters", e);
                result.put("status", "ERROR");
                result.put("message", "Error processing payment information");
                result.put("validHash", false);
            }
        } else {
            result.put("status", "INVALID");
            result.put("message", "Invalid signature");
        }
        String responseCode = fields.get("vnp_ResponseCode");
        String orderId = fields.get("vnp_TxnRef");
        
        // Save transaction info regardless of success/failure
        if (orderId != null) {
            orderService.saveTransactionInfo(orderId, fields);
        }
        
        // Log transaction details
        logger.info("Payment return - TxnId: {}, Amount: {}, Status: {}, ResponseCode: {}",
            result.get("transactionId"),
            result.get("amount"),
            result.get("transactionStatus"),
            result.get("responseCode")
        );
        
        // Build redirect URL with parameters for payment result page
        StringBuilder redirectUrl = new StringBuilder("/payment-result?");
        for (Map.Entry<String, String> entry : requestParams.entrySet()) {
            redirectUrl.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        
        // Remove the trailing &
        if (redirectUrl.toString().endsWith("&")) {
            redirectUrl.setLength(redirectUrl.length() - 1);
        }
     
        return new RedirectView(redirectUrl.toString());
    }

    /**
     * Creates a new payment using PaymentService
     */
    @PostMapping("/api/payment")
    @ResponseBody
    public ResponseEntity<PaymentResponse> createPayment(
            @RequestBody PaymentRequest request,
            HttpServletRequest servletRequest) {
        
        PaymentResponse response = paymentService.createPayment(request, servletRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Queries a transaction using PaymentService
     */
    @PostMapping("/api/payment/query")
    @ResponseBody
    public ResponseEntity<QueryResponse> queryTransaction(
            @RequestBody QueryRequest request,
            HttpServletRequest servletRequest) {
        
        QueryResponse response = paymentService.queryTransaction(request, servletRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Refunds a transaction using PaymentService
     */
    @PostMapping("/api/payment/refund")
    @ResponseBody
    public ResponseEntity<RefundResponse> refundTransaction(
            @RequestBody RefundRequest request,
            HttpServletRequest servletRequest) {
        
        RefundResponse response = paymentService.refundTransaction(request, servletRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Handles IPN notification
     */
    @PostMapping("/ipn")
    @ResponseBody
    public ResponseEntity<IPNResponse> handleIpnNotification(
            @RequestParam MultiValueMap<String, String> requestParams) {
        
        // Convert MultiValueMap to regular Map
        Map<String, String> params = new HashMap<>();
        requestParams.forEach((key, values) -> {
            if (!values.isEmpty()) {
                params.put(key, values.get(0));
            }
        });
        
        // TODO: Implement IPN handling logic
        IPNResponse response = new IPNResponse();
        response.setRspCode("00");
        response.setMessage("Success");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Display payment result page with complete transaction information
     * This endpoint provides a user-friendly view of payment results
     */
    @GetMapping("/payment-result")
    public String paymentResultPage(
            @RequestParam Map<String, String> requestParams,
            HttpServletRequest request,
            org.springframework.ui.Model model) {
        
        logger.info("Displaying payment result page with params: {}", requestParams);
        
        try {
            // Create copy of params for hash calculation
            Map<String, String> fields = new HashMap<>(requestParams);
            
            // Get and remove hash from param map before recalculating
            String vnp_SecureHash = fields.get("vnp_SecureHash");
            fields.remove("vnp_SecureHashType");
            fields.remove("vnp_SecureHash");

            // Validate hash using HashService
            String signValue = hashService.hashAllFields(fields);
            boolean isValidHash = signValue.equals(vnp_SecureHash);
            
            if (isValidHash) {
                // Extract payment information
                String orderId = fields.get("vnp_TxnRef");
                String responseCode = fields.get("vnp_ResponseCode");
                String amount = fields.get("vnp_Amount");
                String vnpayTransactionId = fields.get("vnp_TransactionNo");
                String bankCode = fields.get("vnp_BankCode");
                String payDate = fields.get("vnp_PayDate");
                String orderInfo = fields.get("vnp_OrderInfo");
                
                // Format payment date if available
                String formattedPayDate = null;
                if (payDate != null && !payDate.isEmpty()) {
                    try {
                        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                        LocalDateTime dateTime = LocalDateTime.parse(payDate, inputFormatter);
                        formattedPayDate = dateTime.format(outputFormatter);
                    } catch (Exception e) {
                        logger.warn("Error formatting payment date: {}", payDate);
                        formattedPayDate = payDate;
                    }
                }
                
                // Determine status and message
                String status;
                String message;
                
                if ("00".equals(responseCode)) {
                    status = "SUCCESS";
                    message = "Giao dịch đã được thực hiện thành công. Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi!";
                    
                    // Process successful payment
                    processSuccessfulPayment(orderId, fields);
                    
                } else {
                    status = "FAILED";
                    message = getPaymentErrorMessage(responseCode);
                    
                    // Process failed payment
                    processFailedPayment(orderId, responseCode);
                }
                
                // Add attributes to model
                model.addAttribute("status", status);
                model.addAttribute("message", message);
                model.addAttribute("transactionId", orderId);
                model.addAttribute("amount", amount != null ? Long.parseLong(amount) : null);
                model.addAttribute("vnpayTransactionId", vnpayTransactionId);
                model.addAttribute("bankCode", bankCode);
                model.addAttribute("payDate", formattedPayDate);
                model.addAttribute("orderInfo", orderInfo);
                model.addAttribute("responseCode", responseCode);
                model.addAttribute("validHash", true);
                
            } else {
                // Invalid signature
                model.addAttribute("status", "INVALID");
                model.addAttribute("message", "Chữ ký giao dịch không hợp lệ. Vui lòng liên hệ bộ phận hỗ trợ.");
                model.addAttribute("validHash", false);
            }
            
        } catch (Exception e) {
            logger.error("Error processing payment result page", e);
            model.addAttribute("status", "ERROR");
            model.addAttribute("message", "Đã xảy ra lỗi trong quá trình xử lý. Vui lòng thử lại sau.");
            model.addAttribute("validHash", false);
        }
        
        return "payment-result";
    }
    
    /**
     * Get user-friendly error message based on VNPay response code
     */
    private String getPaymentErrorMessage(String responseCode) {
        if (responseCode == null) return "Giao dịch không thành công";
        
        switch (responseCode) {
            case "07":
                return "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường)";
            case "09":
                return "Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng";
            case "10":
                return "Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần";
            case "11":
                return "Đã hết hạn chờ thanh toán. Xin quý khách vui lòng thực hiện lại giao dịch";
            case "12":
                return "Thẻ/Tài khoản của khách hàng bị khóa";
            case "13":
                return "Quý khách nhập sai mật khẩu xác thực giao dịch (OTP). Xin quý khách vui lòng thực hiện lại giao dịch";
            case "24":
                return "Khách hàng hủy giao dịch";
            case "51":
                return "Tài khoản của quý khách không đủ số dư để thực hiện giao dịch";
            case "65":
                return "Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày";
            case "75":
                return "Ngân hàng thanh toán đang bảo trì";
            case "79":
                return "KH nhập sai mật khẩu thanh toán quá số lần quy định";
            case "99":
                return "Các lỗi khác (lỗi còn lại, không có trong danh sách mã lỗi đã liệt kê)";
            default:
                return "Giao dịch không thành công. Mã lỗi: " + responseCode;
        }
    }

    /**
     * Display payment form page
     */
    @GetMapping("/payment-form")
    public String paymentForm() {
        return "payment-form";
    }
}