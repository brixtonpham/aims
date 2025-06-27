package com.aims.application.command.payment;

import com.aims.domain.payment.model.PaymentMethod;
import lombok.Builder;
import lombok.Data;

/**
 * Result of payment processing command
 */
@Data
@Builder
public class PaymentResult {
    private boolean success;
    private String paymentUrl;
    private String transactionId;
    private String message;
    private String orderId;
    private Long amount;
    private String currency;
    private PaymentMethod paymentMethod;
}
