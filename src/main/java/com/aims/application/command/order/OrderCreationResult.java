package com.aims.application.command.order;

import lombok.Builder;
import lombok.Data;

/**
 * Result of order creation command
 */
@Data
@Builder
public class OrderCreationResult {
    private boolean success;
    private String orderId;
    private Long totalAmount;
    private String message;
}
