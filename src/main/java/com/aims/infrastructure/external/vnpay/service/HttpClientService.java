package com.aims.infrastructure.external.vnpay.service;

import java.util.Map;

/**
 * Interface for HTTP client service - separates HTTP communication logic
 * Solves low cohesion issue in VNPayService
 */
public interface HttpClientService {
    <T> T callApi(String url, Map<String, String> params, Class<T> responseType);
    <T> T postRequest(String url, Object requestBody, Class<T> responseType);
    <T> T getRequest(String url, Map<String, String> params, Class<T> responseType);
}
