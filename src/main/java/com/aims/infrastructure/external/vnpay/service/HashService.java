package com.aims.infrastructure.external.vnpay.service;

import java.util.Map;

/**
 * Interface for hash service - separates hash generation logic
 * Solves low cohesion issue in VNPayService
 */
public interface HashService {
    String hashAllFields(Map<String, String> fields);
    String hmacSHA512(String key, String data);
    String generateSecureHash(Map<String, String> params);
}
