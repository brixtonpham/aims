package com.aims.vnpay.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Demo controller for testing payment result page without dependencies
 */
@Controller
public class PaymentResultDemoController {

    /**
     * Demo endpoint to test payment result page with sample data
     */
    @GetMapping("/payment-result-demo")
    public String paymentResultDemo(
            @RequestParam(defaultValue = "SUCCESS") String status,
            @RequestParam(defaultValue = "ORDER123456") String transactionId,
            @RequestParam(defaultValue = "10000000") String amount,
            Model model) {
        
        // Set up sample data for testing
        if ("SUCCESS".equals(status)) {
            model.addAttribute("status", "SUCCESS");
            model.addAttribute("message", "Giao dịch đã được thực hiện thành công. Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi!");
            model.addAttribute("transactionId", transactionId);
            model.addAttribute("amount", Long.parseLong(amount));
            model.addAttribute("vnpayTransactionId", "VNP" + System.currentTimeMillis());
            model.addAttribute("bankCode", "VCB");
            model.addAttribute("payDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            model.addAttribute("orderInfo", "Thanh toán đơn hàng: " + transactionId);
            model.addAttribute("responseCode", "00");
            model.addAttribute("validHash", true);
        } else if ("FAILED".equals(status)) {
            model.addAttribute("status", "FAILED");
            model.addAttribute("message", "Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư để thực hiện giao dịch");
            model.addAttribute("transactionId", transactionId);
            model.addAttribute("amount", Long.parseLong(amount));
            model.addAttribute("vnpayTransactionId", null);
            model.addAttribute("bankCode", "VCB");
            model.addAttribute("payDate", null);
            model.addAttribute("orderInfo", "Thanh toán đơn hàng: " + transactionId);
            model.addAttribute("responseCode", "51");
            model.addAttribute("validHash", true);
        } else {
            model.addAttribute("status", "INVALID");
            model.addAttribute("message", "Chữ ký giao dịch không hợp lệ. Vui lòng liên hệ bộ phận hỗ trợ.");
            model.addAttribute("validHash", false);
        }
        
        return "payment-result";
    }

    /**
     * Demo payment form without dependencies
     */
    @GetMapping("/payment-form-demo")
    public String paymentFormDemo() {
        return "payment-form";
    }
}
