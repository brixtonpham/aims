package com.aims.application.order;

import com.aims.application.commands.CancelOrderCommand;
import com.aims.application.commands.PlaceOrderCommand;
import com.aims.application.dto.order.CancellationResult;
import com.aims.application.dto.order.OrderCreationResult;
import com.aims.application.dto.order.OrderTrackingResult;
import com.aims.domain.notification.service.NotificationService;
import com.aims.domain.order.entity.Order;
import com.aims.domain.order.repository.OrderRepository;
import com.aims.domain.order.service.OrderDomainService;

import com.aims.domain.payment.service.PaymentDomainService;
import com.aims.domain.order.dto.CreateOrderRequest;
import com.aims.domain.payment.model.DomainPaymentRequest;
import com.aims.domain.payment.model.PaymentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for order orchestration.
 * Coordinates order operations between domain services and handles cross-cutting concerns.
 * 
 * This service follows Clean Architecture principles by:
 * - Orchestrating business workflows
 * - Managing transactions and error handling
 * - Coordinating between domain services
 * - Handling cross-cutting concerns (logging, validation)
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 1 - Foundation
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderApplicationService {

    private final OrderDomainService orderDomainService;
    private final PaymentDomainService paymentDomainService;
    private final NotificationService notificationService;
    private final OrderRepository orderRepository;

    /**
     * Places a new order with comprehensive workflow orchestration.
     * 
     * Workflow Steps:
     * 1. Validate command parameters
     * 2. Create order through domain service
     * 3. Process payment if required
     * 4. Update order status based on payment result
     * 5. Send confirmation notifications
     * 6. Return result with payment URL if applicable
     * 
     * @param command Complete order placement command
     * @return Order creation result with payment information
     */
    public OrderCreationResult placeOrder(PlaceOrderCommand command) {
        log.info("Processing order placement for customer: {}, items: {}", 
                command.getCustomerId(), command.getOrderItems().size());
        
        try {
            // Step 1: Validate command (additional business validation)
            validatePlaceOrderCommand(command);
            
            // Step 2: Create order through domain service
            log.debug("Creating order for customer: {}", command.getCustomerId());
            var createOrderRequest = convertToCreateOrderRequest(command);
            var order = orderDomainService.createOrder(createOrderRequest);
            
            log.info("Order created successfully. Order ID: {}, Status: {}", 
                    order.getOrderId(), order.getStatus());
            
            // Step 3: Process payment if online payment method
            String paymentUrl = handleOrderPayment(order, command);
            if (paymentUrl != null && paymentUrl.startsWith("ERROR:")) {
                return OrderCreationResult.failure("PAYMENT_PROCESSING_FAILED", 
                    paymentUrl.substring(6)); // Remove "ERROR:" prefix
            }
            
            // Step 5: Send notifications
            sendOrderNotificationSafely(order, command);
            
            // Step 6: Return success result
            log.info("Order placement successful. Order ID: {}, Payment URL: {}", 
                    order.getOrderId(), paymentUrl != null ? "provided" : "not required");
            
            return OrderCreationResult.success(
                String.valueOf(order.getOrderId()),
                order.getStatus().toString(),
                order.getTotalAfterVAT().doubleValue(),
                "VND", // Hard-coded currency for now
                paymentUrl
            );
            
        } catch (Exception e) {
            log.error("Order placement failed for customer: {}", command.getCustomerId(), e);
            return OrderCreationResult.failure("ORDER_CREATION_FAILED", 
                "Failed to create order: " + e.getMessage());
        }
    }

    /**
     * Cancels an existing order with refund processing.
     * 
     * Workflow Steps:
     * 1. Validate cancellation eligibility
     * 2. Cancel order through domain service
     * 3. Process refund if applicable
     * 4. Send cancellation notifications
     * 5. Return cancellation result
     * 
     * @param command Order cancellation command
     * @return Cancellation result with refund information
     */
    public CancellationResult cancelOrder(CancelOrderCommand command) {
        log.info("Processing order cancellation for order: {}, customer: {}", 
                command.getOrderId(), command.getCustomerId());
        
        try {
            // Step 1: Validate cancellation eligibility
            validateCancelOrderCommand(command);
            
            // Step 2: Cancel order through domain service
            log.debug("Cancelling order: {}", command.getOrderId());
            orderDomainService.cancelOrder(command.getOrderId());
            
            // Step 3: Process refund if applicable (for paid orders)
            RefundInfo refundInfo = processRefundIfNeeded(command);
            String refundTransactionId = refundInfo.transactionId;
            double refundAmount = refundInfo.amount;
            
            // Step 4: Send notifications
            sendCancellationNotification(command);
            
            // Step 5: Return success result
            log.info("Order cancellation successful. Order ID: {}", command.getOrderId());
            
            return CancellationResult.success(
                command.getOrderId(),
                "CANCELLED",
                refundAmount,
                "VND",
                refundTransactionId
            );
            
        } catch (Exception e) {
            log.error("Order cancellation failed for order: {}", command.getOrderId(), e);
            return CancellationResult.failure(command.getOrderId(), "ORDER_CANCELLATION_FAILED", 
                "Failed to cancel order: " + e.getMessage());
        }
    }

    /**
     * Tracks order status and returns comprehensive tracking information.
     * 
     * @param orderId Unique order identifier
     * @return Order tracking result with status history
     */
    @Transactional(readOnly = true)
    public OrderTrackingResult trackOrder(String orderId) {
        log.info("Tracking order: {}", orderId);
        
        try {
            // Get order status from domain service
            var status = orderDomainService.getOrderStatus(orderId);
            
            // Build status description based on current status
            String statusDescription = buildStatusDescription(status);
            
            log.debug("Order tracking retrieved for order: {}, status: {}", orderId, status);
            
            return OrderTrackingResult.success(
                orderId,
                status.toString(),
                statusDescription,
                null // History not implemented in this phase
            );
            
        } catch (Exception e) {
            log.error("Order tracking failed for order: {}", orderId, e);
            return OrderTrackingResult.failure(orderId, "ORDER_TRACKING_FAILED", 
                "Failed to track order: " + e.getMessage());
        }
    }

    /**
     * Sends cancellation notification to customer.
     */
    private void sendCancellationNotification(CancelOrderCommand command) {
        try {
            log.debug("Sending order cancellation notification for order: {}", command.getOrderId());
            
            // Get order details for notification
            var order = orderDomainService.getOrderById(command.getOrderId());
            
            // Build cancellation notification content
            var content = buildCancellationNotificationContent(order, command);
            
            // Send notification (get customer info from order)
            notificationService.sendOrderCancellation(
                command.getOrderId(),
                getCustomerEmailFromOrder(order), 
                getCustomerNameFromOrder(order), 
                content
            );
        } catch (Exception e) {
            log.warn("Failed to send cancellation notification for order: {}", 
                    command.getOrderId(), e);
            // Don't fail the cancellation for notification issues
        }
    }

    /**
     * Sends order notification safely without failing the order process.
     */
    private void sendOrderNotificationSafely(Order order, PlaceOrderCommand command) {
        try {
            log.debug("Sending order confirmation notification for order: {}", order.getOrderId());
            notificationService.sendOrderConfirmation(
                String.valueOf(order.getOrderId()), 
                command.getCustomerEmail(), 
                command.getCustomerName(), 
                buildOrderNotificationContent(order, command)
            );
        } catch (Exception e) {
            log.warn("Failed to send order confirmation notification for order: {}", 
                    order.getOrderId(), e);
            // Don't fail the order for notification issues
        }
    }

    /**
     * Builds status description based on order status.
     */
    private String buildStatusDescription(Order.OrderStatus status) {
        switch (status) {
            case PENDING:
                return "Your order is being processed and will be confirmed soon.";
            case CONFIRMED:
                return "Your order has been confirmed and is being prepared for shipment.";
            case SHIPPED:
                return "Your order has been shipped and is on its way to you.";
            case DELIVERED:
                return "Your order has been successfully delivered.";
            case CANCELLED:
                return "Your order has been cancelled.";
            default:
                return "Order status: " + status.getDisplayName();
        }
    }

    /**
     * Builds notification content for order cancellation.
     */
    private String buildCancellationNotificationContent(Order order, CancelOrderCommand command) {
        var content = new StringBuilder();
        content.append("Order Cancellation Notification\n");
        content.append("Order ID: ").append(order.getOrderId()).append("\n");
        content.append("Original Amount: ").append(order.getTotalAfterVAT()).append(" VND\n");
        content.append("Status: CANCELLED\n");
        
        if (command.getCancellationReason() != null) {
            content.append("Reason: ").append(command.getCancellationReason()).append("\n");
        }
        
        if (command.getProcessRefund() != null && command.getProcessRefund()) {
            content.append("Refund will be processed within 3-5 business days.\n");
        }
        
        return content.toString();
    }

    /**
     * Validates place order command for business rules compliance.
     */
    private void validatePlaceOrderCommand(PlaceOrderCommand command) {
        log.debug("Validating place order command for customer: {}", command.getCustomerId());
        
        // Additional business validation beyond Bean Validation
        if (command.getOrderItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        
        // Validate order items
        for (var item : command.getOrderItems()) {
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Item quantity must be positive: " + item.getProductName());
            }
            if (item.getUnitPrice() <= 0) {
                throw new IllegalArgumentException("Item unit price must be positive: " + item.getProductName());
            }
        }
        
        // Validate payment method
        if (!isValidPaymentMethod(command.getPaymentMethod())) {
            throw new IllegalArgumentException("Invalid payment method: " + command.getPaymentMethod());
        }
        
        // Validate delivery address
        if (command.getDeliveryAddress() == null) {
            throw new IllegalArgumentException("Delivery address is required");
        }
    }

    /**
     * Validates cancel order command for business rules compliance.
     */
    private void validateCancelOrderCommand(CancelOrderCommand command) {
        log.debug("Validating cancel order command for order: {}", command.getOrderId());
        
        // Check if order exists and belongs to customer
        try {
            var order = orderDomainService.getOrderById(command.getOrderId());
            
            // Verify customer ownership
            if (!command.getCustomerId().equals(order.getCustomerId())) {
                throw new IllegalArgumentException("Order does not belong to the specified customer");
            }
            
            // Check if order can be cancelled
            if (!order.canBeCancelled()) {
                throw new IllegalStateException("Order cannot be cancelled in current status: " + order.getStatus());
            }
            
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) {
                throw (RuntimeException) e;
            }
            throw new IllegalArgumentException("Order not found or inaccessible: " + command.getOrderId(), e);
        }
        
        // Validate cancellation reason
        if (command.getCancellationReason() == null || command.getCancellationReason().trim().isEmpty()) {
            throw new IllegalArgumentException("Cancellation reason is required");
        }
    }

    /**
     * Validates if the payment method is supported.
     */
    private boolean isValidPaymentMethod(String paymentMethod) {
        return paymentMethod != null && (
            "VNPAY".equals(paymentMethod) ||
            "COD".equals(paymentMethod) ||
            "BANK_TRANSFER".equals(paymentMethod)
        );
    }

    /**
     * Determines if payment method requires online payment processing.
     */
    private boolean requiresOnlinePayment(String paymentMethod) {
        return "VNPAY".equals(paymentMethod) || 
               "CREDIT_CARD".equals(paymentMethod) || 
               "BANK_TRANSFER".equals(paymentMethod);
    }

    /**
     * Converts PlaceOrderCommand to CreateOrderRequest for domain service.
     */
    private CreateOrderRequest convertToCreateOrderRequest(PlaceOrderCommand command) {
        var request = new CreateOrderRequest();
        
        // Customer Information
        request.setCustomerId(command.getCustomerId());
        request.setCustomerName(command.getCustomerName());
        request.setCustomerEmail(command.getCustomerEmail());
        request.setCustomerPhone(command.getCustomerPhone());
        
        // Order Items - convert to domain format
        var orderItems = new java.util.ArrayList<CreateOrderRequest.OrderItemRequest>();
        for (var commandItem : command.getOrderItems()) {
            var orderItem = new CreateOrderRequest.OrderItemRequest();
            // Convert String productId to Long, fallback to hash if not numeric
            try {
                orderItem.setProductId(Long.parseLong(commandItem.getProductId()));
            } catch (NumberFormatException e) {
                orderItem.setProductId((long) commandItem.getProductId().hashCode());
            }
            orderItem.setProductTitle(commandItem.getProductName());
            orderItem.setQuantity(commandItem.getQuantity());
            orderItem.setUnitPrice(commandItem.getUnitPrice().intValue());
            orderItems.add(orderItem);
        }
        request.setOrderItems(orderItems);
        
        // Delivery Information - map to domain format
        var deliveryInfo = new CreateOrderRequest.DeliveryInfoRequest();
        if (command.getDeliveryAddress() != null) {
            // Use customer name and phone as delivery contact
            deliveryInfo.setName(command.getCustomerName());
            deliveryInfo.setPhone(command.getCustomerPhone());
            deliveryInfo.setEmail(command.getCustomerEmail());
            
            // Combine address components
            var fullAddress = new StringBuilder();
            fullAddress.append(command.getDeliveryAddress().getStreetAddress());
            if (command.getDeliveryAddress().getCity() != null) {
                fullAddress.append(", ").append(command.getDeliveryAddress().getCity());
            }
            if (command.getDeliveryAddress().getPostalCode() != null) {
                fullAddress.append(" ").append(command.getDeliveryAddress().getPostalCode());
            }
            deliveryInfo.setAddress(fullAddress.toString());
            deliveryInfo.setProvince(command.getDeliveryAddress().getState());
        }
        
        if (command.getDeliveryInstructions() != null) {
            deliveryInfo.setDeliveryMessage(command.getDeliveryInstructions());
        }
        request.setDeliveryInfo(deliveryInfo);
        
        // Payment Information
        request.setPaymentMethod(command.getPaymentMethod());
        
        // Rush Order Flag
        request.setIsRushOrder(command.getRushOrder());
        
        return request;
    }

    /**
     * Processes payment for an order through payment domain service.
     */
    private PaymentResult processOrderPayment(Order order, PlaceOrderCommand command) {
        try {
            // Create DomainPaymentRequest using builder pattern
            var domainPaymentRequest = DomainPaymentRequest.builder()
                .orderId(String.valueOf(order.getOrderId()))
                .amount(order.getTotalAfterVAT())
                .currency("VND")
                .customerId(command.getCustomerId())
                .orderDescription("Payment for Order #" + order.getOrderId())
                .language("vn")
                .build();
            
            return paymentDomainService.processPayment(domainPaymentRequest);
        } catch (Exception e) {
            log.error("Failed to process payment for order: {}", order.getOrderId(), e);
            // Return failure result using builder pattern
            return PaymentResult.failure("Payment processing failed: " + e.getMessage(), "PAYMENT_ERROR");
        }
    }

    /**
     * Builds notification content for order confirmation.
     */
    private String buildOrderNotificationContent(Order order, PlaceOrderCommand command) {
        var content = new StringBuilder();
        content.append("Order Confirmation\n");
        content.append("Order ID: ").append(order.getOrderId()).append("\n");
        content.append("Total Amount: ").append(order.getTotalAfterVAT()).append(" VND\n");
        content.append("Status: ").append(order.getStatus()).append("\n");
        content.append("Items: ").append(command.getOrderItems().size()).append(" items\n");
        
        if (command.getDeliveryAddress() != null) {
            content.append("Delivery Address: ").append(command.getDeliveryAddress().getStreetAddress()).append("\n");
        }
        
        return content.toString();
    }

    /**
     * Handles payment processing for an order.
     * 
     * @param order The order to process payment for
     * @param command The order placement command containing payment details
     * @return Payment URL if successful, or error message prefixed with "ERROR:" if failed
     */
    private String handleOrderPayment(Order order, PlaceOrderCommand command) {
        if (requiresOnlinePayment(command.getPaymentMethod())) {
            log.debug("Processing online payment for order: {}", order.getOrderId());
            try {
                var paymentResult = processOrderPayment(order, command);
                String paymentUrl = paymentResult.getPaymentUrl();
                
                // Step 4: Update order status based on payment result
                if (paymentResult.isSuccess()) {
                    log.debug("Payment initiated successfully for order: {}", order.getOrderId());
                    // Order status will be updated by payment callback
                    return paymentUrl;
                } else {
                    log.warn("Payment initiation failed for order: {}", order.getOrderId());
                    // Cancel order if payment initiation fails
                    cancelOrderSafely(order, "payment failure");
                    return "ERROR:Failed to initiate payment: " + paymentResult.getMessage();
                }
            } catch (Exception e) {
                log.error("Payment processing failed for order: {}", order.getOrderId(), e);
                // Cancel order if payment processing fails
                cancelOrderSafely(order, "payment processing failure");
                return "ERROR:Payment processing failed: " + e.getMessage();
            }
        } else {
            log.debug("Order uses offline payment method: {}", command.getPaymentMethod());
            // For COD orders, mark as confirmed
            if ("COD".equals(command.getPaymentMethod())) {
                order.confirm(); // Use the business method
                orderRepository.update(order);
            }
            return null; // No payment URL needed for offline payment
        }
    }

    /**
     * Simple record to hold refund information.
     */
    private static class RefundInfo {
        final String transactionId;
        final double amount;
        
        RefundInfo(String transactionId, double amount) {
            this.transactionId = transactionId;
            this.amount = amount;
        }
    }

    /**
     * Processes refund if needed for order cancellation.
     */
    private RefundInfo processRefundIfNeeded(CancelOrderCommand command) {
        if (command.getProcessRefund() != null && command.getProcessRefund()) {
            log.debug("Processing refund for cancelled order: {}", command.getOrderId());
            try {
                // Get order details for refund calculation
                var order = orderDomainService.getOrderById(command.getOrderId());
                double refundAmount = order.getTotalAfterVAT().doubleValue();
                
                // For now, we'll generate a mock refund transaction ID
                // In a real implementation, this would call the payment service
                String refundTransactionId = "REFUND_" + command.getOrderId() + "_" + System.currentTimeMillis();
                log.info("Refund processed for order: {}, amount: {}, transaction: {}", 
                        command.getOrderId(), refundAmount, refundTransactionId);
                
                return new RefundInfo(refundTransactionId, refundAmount);
            } catch (Exception e) {
                log.warn("Failed to process refund for order: {}", command.getOrderId(), e);
                // Continue with cancellation even if refund fails
                return new RefundInfo(null, 0.0);
            }
        }
        return new RefundInfo(null, 0.0);
    }

    /**
     * Gets customer email from order, with fallback.
     */
    private String getCustomerEmailFromOrder(Order order) {
        // In a real system, this would lookup customer details from a customer service
        // For now, return a placeholder or extract from order if available
        return "customer-" + order.getCustomerId() + "@example.com";
    }

    /**
     * Gets customer name from order, with fallback.
     */
    private String getCustomerNameFromOrder(Order order) {
        // In a real system, this would lookup customer details from a customer service
        // For now, return a placeholder based on customer ID
        return "Customer " + order.getCustomerId();
    }

    /**
     * Cancels order safely without throwing exceptions.
     */
    private void cancelOrderSafely(Order order, String reason) {
        try {
            orderDomainService.cancelOrder(String.valueOf(order.getOrderId()));
        } catch (Exception cancelException) {
            log.error("Failed to cancel order after {}: {}", reason, order.getOrderId(), cancelException);
        }
    }
}
