package com.aims.application.command.order;

import com.aims.application.command.Command;
import com.aims.application.command.CommandValidationException;
import lombok.Builder;
import lombok.Data;

/**
 * Command for cancelling an order
 */
@Data
@Builder
public class CancelOrderCommand implements Command<CancellationResult> {
    
    private String orderId;
    private String reason;
    private String requestedBy;
    
    @Override
    public CancellationResult execute() {
        // Execution is delegated to handler
        throw new UnsupportedOperationException("Use CommandBus to execute this command");
    }
    
    @Override
    public void validate() {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new CommandValidationException("Order ID is required");
        }
        
        if (requestedBy == null || requestedBy.trim().isEmpty()) {
            throw new CommandValidationException("Requested by is required");
        }
    }
    
    @Override
    public String getCommandType() {
        return "CANCEL_ORDER";
    }
}
