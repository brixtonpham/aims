package com.aims.infrastructure.external.vnpay.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.servlet.http.HttpServletRequest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

/**
 * Configuration class for VNPAY integration
 * Contains all necessary configuration properties and utility methods for VNPAY payment processing
 */
@Configuration
@Data
public class VNPayConfig {

    private static final String UNKNOWN_IP = "unknown";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${vnpay.pay-url}")
    private String payUrl;

    @Value("${vnpay.return-url}")
    private String returnUrl;

    @Value("${vnpay.tmn-code}")
    private String tmnCode;

    @Value("${vnpay.secret-key}")
    private String secretKey;

    @Value("${vnpay.api-url}")
    private String apiUrl;

    /**
     * Generates a hash of all fields in the map for VNPAY request signing
     * @param fields Map containing the parameters to be hashed
     * @return The HMAC-SHA512 hash of all fields concatenated in sorted order
     */
    public String hashAllFields(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && !fieldValue.isEmpty()) {
                sb.append(fieldName);
                sb.append("=");
                sb.append(fieldValue);
            }
            if (itr.hasNext()) {
                sb.append("&");
            }
        }
        return hmacSHA512(getSecretKey(), sb.toString());
    }

    /**
     * Generates HMAC-SHA512 hash for VNPAY request signing
     * @param key The secret key used for signing (vnp_HashSecret)
     * @param data The data to be signed
     * @return Hexadecimal string of the HMAC-SHA512 hash
     */
    public String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            final SecretKeySpec secretKeySpec = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKeySpec);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * Extracts the client IP address from the HTTP request
     * Handles various proxy scenarios and headers
     * @param request The HTTP request
     * @return The client IP address
     */
    public String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || UNKNOWN_IP.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || UNKNOWN_IP.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || UNKNOWN_IP.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || UNKNOWN_IP.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || UNKNOWN_IP.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        // For multiple proxies, first address is the client
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        return ipAddress;
    }

    /**
     * Generates a random number string of specified length
     * Used for creating unique transaction references
     * @param len The length of the random number string
     * @return Random number string
     */
    public String getRandomNumber(int len) {
        if (len <= 0) {
            return "";
        }
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(SECURE_RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
