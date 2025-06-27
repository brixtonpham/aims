package com.aims.application.command.handler;

import com.aims.application.command.CommandHandler;
import com.aims.application.command.order.CancelOrderCommand;
import com.aims.application.command.order.CancellationResult;
import com.aims.domain.order.entity.Order;
import com.aims.domain.order.service.OrderDomainService;
import com.aims.domain.payment.service.PaymentDomainService;
import com.aims.domain.payment.model.DomainRefundRequest;
import com.aims.domain.payment.model.RefundResult;
import com.aims.domain.order.service.event.DomainEventPublisher;
import com.aims.domain.order.service.event.OrderCancelledEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for CancelOrderCommand
 */
@Component
public class CancelOrderCommandHandler implements CommandHandler<CancelOrderCommand, CancellationResult> {
    
    private static final Logger logger = LoggerFactory.getLogger(CancelOrderCommandHandler.class);
    
    private final OrderDomainService orderDomainService;
    private final PaymentDomainService paymentDomainService;
    private final DomainEventPublisher domainEventPublisher;
    
    @Autowired
    public CancelOrderCommandHandler(
        OrderDomainService orderDomainService,
        @Qualifier("vnpayPaymentAdapter") PaymentDomainService paymentDomainService,
        DomainEventPublisher domainEventPublisher) {
        this.orderDomainService = orderDomainService;
        this.paymentDomainService = paymentDomainService;
        this.domainEventPublisher = domainEventPublisher;
    }
    
    @Override
    public CancellationResult handle(CancelOrderCommand command) {
        logger.info("Handling cancel order command for order: {}", command.getOrderId());
        
        try {
            // Get order information
            var order = orderDomainService.getOrderById(command.getOrderId());
            
            // Process refund if order requires refund (non-COD and already confirmed/processed)
            RefundResult refundResult = null;
            if (requiresRefund(order)) {
                DomainRefundRequest refundRequest = DomainRefundRequest.builder()
                    .orderId(command.getOrderId())
                    .amount(order.getTotalAfterVAT())
                    .reason(command.getReason())
                    .requestedBy(command.getRequestedBy())
                    .build();
                
                refundResult = paymentDomainService.processRefund(refundRequest);
                
                if (!refundResult.isSuccess()) {
                    return CancellationResult.builder()
                        .success(false)
                        .message("Cannot cancel order - refund failed: " + refundResult.getMessage())
                        .build();
                }
            }
            
            // Cancel order through domain service
            orderDomainService.cancelOrder(command.getOrderId());
            
            // Publish order cancelled event
            OrderCancelledEvent cancelledEvent = new OrderCancelledEvent(
                order.getOrderId(),
                order.getCustomerId(),
                refundResult != null ? refundResult.getAmount() : 0L
            );
            domainEventPublisher.publish(cancelledEvent);
            
            logger.info("Order cancelled successfully: {}", command.getOrderId());
            
            return CancellationResult.builder()
                .success(true)
                .orderId(command.getOrderId())
                .refundResult(refundResult)
                .message("Order cancelled successfully")
                .build();
                
        } catch (Exception e) {
            logger.error("Failed to cancel order: {}", command.getOrderId(), e);
            
            return CancellationResult.builder()
                .success(false)
                .orderId(command.getOrderId())
                .message("Failed to cancel order: " + e.getMessage())
                .build();
        }
    }
    
    @Override
    public boolean supports(String commandType) {
        return "CANCEL_ORDER".equals(commandType);
    }
    
    @Override
    public int getPriority() {
        return 10;
    }
    
    /**
     * Determines if an order requires a refund when cancelled.
     * 
     * Business Logic:
     * - COD orders don't require refund as no payment was processed online
     * - Orders in PENDING status may not have been paid yet
     * - Orders in CONFIRMED, SHIPPED status likely had successful payment
     * 
     * @param order The order to check
     * @return true if refund is required
     */
    private boolean requiresRefund(Order order) {
        // COD orders don't require online refund processing
        if ("COD".equals(order.getPaymentMethod())) {
            return false;
        }
        
        // Only process refunds for orders that are likely to have been paid
        // PENDING orders may not have completed payment yet
        return order.getStatus() == Order.OrderStatus.CONFIRMED ||
               order.getStatus() == Order.OrderStatus.SHIPPED;
    }
}
