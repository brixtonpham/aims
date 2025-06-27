package com.aims.application.command.handler;

import com.aims.application.command.CommandHandler;
import com.aims.application.command.payment.ProcessPaymentCommand;
import com.aims.application.command.payment.PaymentResult;
import com.aims.domain.payment.service.PaymentDomainService;
import com.aims.domain.payment.model.DomainPaymentRequest;
import com.aims.domain.payment.event.PaymentProcessedEvent;
import com.aims.domain.payment.event.PaymentFailedEvent;
import com.aims.domain.order.service.event.DomainEventPublisher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for ProcessPaymentCommand
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 3
 */
@Component
public class ProcessPaymentCommandHandler implements CommandHandler<ProcessPaymentCommand, PaymentResult> {
    
    private static final Logger logger = LoggerFactory.getLogger(ProcessPaymentCommandHandler.class);
    
    private final PaymentDomainService paymentDomainService;
    private final DomainEventPublisher eventPublisher;
    
    @Autowired
    public ProcessPaymentCommandHandler(
        @Qualifier("vnpayPaymentAdapter") PaymentDomainService paymentDomainService,
        DomainEventPublisher eventPublisher) {
        this.paymentDomainService = paymentDomainService;
        this.eventPublisher = eventPublisher;
    }
    
    @Override
    public PaymentResult handle(ProcessPaymentCommand command) {
        logger.info("Handling process payment command for order: {}", command.getOrderId());
        
        try {
            // Create domain payment request
            DomainPaymentRequest request = DomainPaymentRequest.builder()
                .orderId(command.getOrderId())
                .amount(command.getAmount())
                .currency(command.getCurrency() != null ? command.getCurrency() : "VND")
                .bankCode(command.getBankCode())
                .language(command.getLanguage())
                .customerId(command.getCustomerId())
                .httpRequest(command.getHttpRequest())
                .build();
            
            // Process payment through domain service
            com.aims.domain.payment.model.PaymentResult domainResult = 
                paymentDomainService.processPayment(request);
            
            // Publish payment event based on result
            if (domainResult.isSuccess()) {
                PaymentProcessedEvent successEvent = new PaymentProcessedEvent(
                    Long.parseLong(command.getOrderId()),
                    command.getPaymentMethod().name(),
                    command.getAmount(),
                    domainResult.getTransactionId(),
                    true
                );
                eventPublisher.publish(successEvent);
            } else {
                PaymentFailedEvent failureEvent = new PaymentFailedEvent(
                    Long.parseLong(command.getOrderId()),
                    command.getPaymentMethod().name(),
                    command.getAmount(),
                    domainResult.getMessage(),
                    domainResult.getErrorCode()
                );
                eventPublisher.publish(failureEvent);
            }
            
            logger.info("Payment processing completed for order: {} with result: {}", 
                       command.getOrderId(), domainResult.isSuccess());
            
            // Convert domain result to command result
            return PaymentResult.builder()
                .success(domainResult.isSuccess())
                .transactionId(domainResult.getTransactionId())
                .orderId(command.getOrderId())
                .amount(command.getAmount())
                .currency(command.getCurrency())
                .paymentMethod(command.getPaymentMethod())
                .paymentUrl(domainResult.getPaymentUrl())
                .message(domainResult.getMessage())
                .build();
                
        } catch (Exception e) {
            logger.error("Failed to process payment for order: {}", command.getOrderId(), e);
            
            // Publish payment failed event
            PaymentFailedEvent failureEvent = new PaymentFailedEvent(
                Long.parseLong(command.getOrderId()),
                command.getPaymentMethod().name(),
                command.getAmount(),
                "Payment processing failed: " + e.getMessage(),
                "PROCESSING_ERROR"
            );
            eventPublisher.publish(failureEvent);
            
            return PaymentResult.builder()
                .success(false)
                .orderId(command.getOrderId())
                .amount(command.getAmount())
                .currency(command.getCurrency())
                .paymentMethod(command.getPaymentMethod())
                .message("Payment processing failed: " + e.getMessage())
                .build();
        }
    }
    
    @Override
    public boolean supports(String commandType) {
        return "PROCESS_PAYMENT".equals(commandType);
    }
    
    @Override
    public int getPriority() {
        return 10;
    }
}
