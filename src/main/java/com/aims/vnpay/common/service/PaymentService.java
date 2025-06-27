package com.aims.vnpay.common.service;

import com.aims.vnpay.common.dto.PaymentRequest;
import com.aims.vnpay.common.dto.QueryRequest;
import com.aims.vnpay.common.dto.RefundRequest;
import com.aims.vnpay.common.service.VNPayService.PaymentResponse;
import com.aims.vnpay.common.service.VNPayService.QueryResponse;
import com.aims.vnpay.common.service.VNPayService.RefundResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Interface cho payment service - giải quyết tight coupling
 * Cho phép thay đổi payment provider mà không sửa code hiện tại
 */
public interface PaymentService {
    PaymentResponse createPayment(PaymentRequest request, HttpServletRequest servletRequest);
    QueryResponse queryTransaction(QueryRequest request, HttpServletRequest servletRequest);
    RefundResponse refundTransaction(RefundRequest request, HttpServletRequest servletRequest);
} 