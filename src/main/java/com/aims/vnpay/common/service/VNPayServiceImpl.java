package com.aims.vnpay.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aims.vnpay.common.dto.PaymentRequest;
import com.aims.vnpay.common.dto.QueryRequest;
import com.aims.vnpay.common.dto.RefundRequest;
import com.aims.vnpay.common.service.VNPayService.PaymentResponse;
import com.aims.vnpay.common.service.VNPayService.QueryResponse;
import com.aims.vnpay.common.service.VNPayService.RefundResponse;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Implementation of PaymentService interface that delegates to VNPayService
 * This provides the required bean with proper qualifier for dependency injection
 */
@Service("vnpayService")
public class VNPayServiceImpl implements PaymentService {
    
    private final VNPayService vnpayService;
    
    @Autowired
    public VNPayServiceImpl(VNPayService vnpayService) {
        this.vnpayService = vnpayService;
    }
    
    @Override
    public PaymentResponse createPayment(PaymentRequest request, HttpServletRequest servletRequest) {
        return vnpayService.createPayment(request, servletRequest);
    }
    
    @Override
    public QueryResponse queryTransaction(QueryRequest request, HttpServletRequest servletRequest) {
        return vnpayService.queryTransaction(request, servletRequest);
    }
    
    @Override
    public RefundResponse refundTransaction(RefundRequest request, HttpServletRequest servletRequest) {
        return vnpayService.refundTransaction(request, servletRequest);
    }
}
