package com.aims.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Payment configuration for VNPay and other payment providers
 * Centralizes payment-related settings and provides configuration beans
 */
@Configuration
public class PaymentConfig {

    /**
     * VNPay configuration properties
     */
    @Bean
    @ConfigurationProperties(prefix = "vnpay")
    public VNPayProperties vnpayProperties() {
        return new VNPayProperties();
    }

    /**
     * VNPay configuration properties class
     */
    public static class VNPayProperties {
        private String payUrl;
        private String returnUrl;
        private String tmnCode;
        private String secretKey;
        private String apiUrl;
        private int timeoutMinutes = 15;
        private String version = "2.1.0";
        private String command = "pay";
        private String orderType = "other";
        private String locale = "vn";
        private String currCode = "VND";

        // Getters and Setters
        public String getPayUrl() {
            return payUrl;
        }

        public void setPayUrl(String payUrl) {
            this.payUrl = payUrl;
        }

        public String getReturnUrl() {
            return returnUrl;
        }

        public void setReturnUrl(String returnUrl) {
            this.returnUrl = returnUrl;
        }

        public String getTmnCode() {
            return tmnCode;
        }

        public void setTmnCode(String tmnCode) {
            this.tmnCode = tmnCode;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getApiUrl() {
            return apiUrl;
        }

        public void setApiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
        }

        public int getTimeoutMinutes() {
            return timeoutMinutes;
        }

        public void setTimeoutMinutes(int timeoutMinutes) {
            this.timeoutMinutes = timeoutMinutes;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        public String getOrderType() {
            return orderType;
        }

        public void setOrderType(String orderType) {
            this.orderType = orderType;
        }

        public String getLocale() {
            return locale;
        }

        public void setLocale(String locale) {
            this.locale = locale;
        }

        public String getCurrCode() {
            return currCode;
        }

        public void setCurrCode(String currCode) {
            this.currCode = currCode;
        }
    }

    /**
     * Development profile VNPay configuration (sandbox)
     */
    @Bean
    @Profile("dev")
    public VNPayProperties devVNPayProperties() {
        VNPayProperties props = new VNPayProperties();
        props.setPayUrl("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
        props.setApiUrl("https://sandbox.vnpayment.vn/merchant_webapi/api/transaction");
        props.setReturnUrl("http://localhost:8080/api/payment/vnpay/return");
        return props;
    }

    /**
     * Production profile VNPay configuration
     */
    @Bean
    @Profile("prod")
    public VNPayProperties prodVNPayProperties() {
        VNPayProperties props = new VNPayProperties();
        props.setPayUrl("https://vnpayment.vn/paymentv2/vpcpay.html");
        props.setApiUrl("https://vnpayment.vn/merchant_webapi/api/transaction");
        return props;
    }

}
