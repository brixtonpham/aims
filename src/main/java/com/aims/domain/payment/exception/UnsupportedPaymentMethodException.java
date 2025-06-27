package com.aims.domain.payment.exception;

/**
 * Exception thrown when requested payment method is not supported
 */
public class UnsupportedPaymentMethodException extends RuntimeException {
    public UnsupportedPaymentMethodException(String message) {
        super(message);
    }
}