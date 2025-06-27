package com.aims.vnpay.common.observer;

import com.aims.vnpay.common.service.NotificationService;
import com.aims.vnpay.common.service.VNPayService.PaymentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Email Observer trong Observer Pattern
 * Gửi email notification khi payment status thay đổi
 */
@Component
public class EmailPaymentObserver implements PaymentObserver {
    
    @Autowired
    private NotificationService notificationService;
    
    @Override
    public void onPaymentSuccess(String orderId, PaymentResponse response) {
        notificationService.sendPaymentSuccessNotification(orderId, response.getPaymentUrl());
    }
    
    @Override
    public void onPaymentFailed(String orderId, String error) {
        notificationService.sendPaymentFailedNotification(orderId, error);
    }
} 