package com.aims.domain.payment.event;

import com.aims.domain.order.service.event.DomainEvent;

/**
 * Domain event fired when a payment is processed
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 3
 */
public class PaymentProcessedEvent extends DomainEvent {
    
    private final Long orderId;
    private final String paymentMethod;
    private final Long amount;
    private final String transactionId;
    private final boolean successful;
    
    public PaymentProcessedEvent(Long orderId, String paymentMethod, Long amount, 
                               String transactionId, boolean successful) {
        super("PaymentProcessed");
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.transactionId = transactionId;
        this.successful = successful;
    }
    
    public Long getOrderId() {
        return orderId;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public Long getAmount() {
        return amount;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public boolean isSuccessful() {
        return successful;
    }
    
    @Override
    public String toString() {
        return String.format("PaymentProcessedEvent{orderId=%d, paymentMethod='%s', amount=%d, transactionId='%s', successful=%s, %s}", 
            orderId, paymentMethod, amount, transactionId, successful, super.toString());
    }
}
