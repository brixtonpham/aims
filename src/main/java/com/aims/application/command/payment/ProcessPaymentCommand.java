package com.aims.application.command.payment;

import com.aims.application.command.Command;
import com.aims.application.command.CommandValidationException;
import com.aims.domain.payment.model.PaymentMethod;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Data;

/**
 * Command for processing payment
 */
@Data
@Builder
public class ProcessPaymentCommand implements Command<PaymentResult> {
    
    private String orderId;
    private Long amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private String customerId;
    private String language;
    private String bankCode;
    private HttpServletRequest httpRequest;
    
    @Override
    public PaymentResult execute() {
        // Execution is delegated to handler
        throw new UnsupportedOperationException("Use CommandBus to execute this command");
    }
    
    @Override
    public void validate() {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new CommandValidationException("Order ID is required");
        }
        
        if (amount == null || amount <= 0) {
            throw new CommandValidationException("Amount must be positive");
        }
        
        if (paymentMethod == null) {
            throw new CommandValidationException("Payment method is required");
        }
    }
    
    @Override
    public String getCommandType() {
        return "PROCESS_PAYMENT";
    }
}
