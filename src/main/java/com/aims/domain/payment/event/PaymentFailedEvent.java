package com.aims.domain.payment.event;

import com.aims.domain.order.service.event.DomainEvent;

/**
 * Domain event fired when a payment fails
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 3
 */
public class PaymentFailedEvent extends DomainEvent {
    
    private final Long orderId;
    private final String paymentMethod;
    private final Long amount;
    private final String errorMessage;
    private final String errorCode;
    
    public PaymentFailedEvent(Long orderId, String paymentMethod, Long amount, 
                            String errorMessage, String errorCode) {
        super("PaymentFailed");
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
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
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    @Override
    public String toString() {
        return String.format("PaymentFailedEvent{orderId=%d, paymentMethod='%s', amount=%d, errorMessage='%s', errorCode='%s', %s}", 
            orderId, paymentMethod, amount, errorMessage, errorCode, super.toString());
    }
}
