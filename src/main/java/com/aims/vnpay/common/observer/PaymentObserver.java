package com.aims.vnpay.common.observer;

import com.aims.vnpay.common.service.VNPayService.PaymentResponse;

/**
 * Observer Pattern cho Payment Notifications
 * Thông báo cho nhiều observers khi payment status thay đổi
 */
public interface PaymentObserver {
    void onPaymentSuccess(String orderId, PaymentResponse response);
    void onPaymentFailed(String orderId, String error);
} 