package com.aims.application.command.order;

import com.aims.application.command.Command;
import com.aims.application.command.CommandValidationException;
import com.aims.domain.order.entity.OrderItem;
import com.aims.domain.order.entity.DeliveryInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Command for placing an order
 */
@Data
@Builder
public class PlaceOrderCommand implements Command<OrderCreationResult> {
    
    private String customerId;
    private List<OrderItem> items;
    private DeliveryInfo deliveryInfo;
    private String paymentMethod;
    private String language;
    private String bankCode;
    private boolean isRushOrder;
    private HttpServletRequest httpRequest;
    
    @Override
    public OrderCreationResult execute() {
        // Execution is delegated to handler
        throw new UnsupportedOperationException("Use CommandBus to execute this command");
    }
    
    @Override
    public void validate() {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new CommandValidationException("Customer ID is required");
        }
        
        if (items == null || items.isEmpty()) {
            throw new CommandValidationException("Order items are required");
        }
        
        if (deliveryInfo == null) {
            throw new CommandValidationException("Delivery information is required");
        }
        
        // Validate items
        for (OrderItem item : items) {
            if (item.getQuantity() <= 0) {
                throw new CommandValidationException("Item quantity must be positive");
            }
            if (item.getUnitPrice() < 0) {
                throw new CommandValidationException("Item price cannot be negative");
            }
        }
    }
    
    @Override
    public String getCommandType() {
        return "PLACE_ORDER";
    }
}
