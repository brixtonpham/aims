package com.aims.application.command.order;

import com.aims.domain.payment.model.RefundResult;
import lombok.Builder;
import lombok.Data;

/**
 * Result of order cancellation command
 */
@Data
@Builder
public class CancellationResult {
    private boolean success;
    private String orderId;
    private RefundResult refundResult;
    private String message;
}
