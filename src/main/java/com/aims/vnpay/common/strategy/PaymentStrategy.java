package com.aims.vnpay.common.strategy;

import com.aims.vnpay.common.dto.PaymentRequest;
import com.aims.vnpay.common.dto.QueryRequest;
import com.aims.vnpay.common.dto.RefundRequest;
import com.aims.vnpay.common.service.VNPayService.PaymentResponse;
import com.aims.vnpay.common.service.VNPayService.QueryResponse;
import com.aims.vnpay.common.service.VNPayService.RefundResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Strategy Pattern cho Payment Providers
 * Interface cho các payment strategy khác nhau
 */
public interface PaymentStrategy {
    PaymentResponse createPayment(PaymentRequest request, HttpServletRequest servletRequest);
    QueryResponse queryTransaction(QueryRequest request, HttpServletRequest servletRequest);
    RefundResponse refundTransaction(RefundRequest request, HttpServletRequest servletRequest);
    String getProviderName();
} 