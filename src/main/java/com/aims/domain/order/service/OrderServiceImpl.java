package com.aims.domain.order.service;

import com.aims.domain.order.entity.Order;
import com.aims.domain.order.entity.Order.OrderStatus;
import com.aims.domain.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Domain service implementation for Order business logic.
 * Contains pure business logic without orchestration concerns.
 * Follows Domain-Driven Design principles.
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    private static final String ORDER_NOT_FOUND_MESSAGE = "Order not found with ID: ";
    
    private final OrderRepository orderRepository;
    
    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    
    @Override
    public boolean canProcessPayment(String orderId) {
        logger.debug("Checking if order {} can be processed for payment", orderId);
        
        validateOrderId(orderId);
        
        Optional<Order> orderOpt = orderRepository.findById(Long.parseLong(orderId));
        if (orderOpt.isEmpty()) {
            logger.warn("Order not found for payment processing: {}", orderId);
            return false;
        }
        
        Order order = orderOpt.get();
        OrderStatus status = order.getStatus();
        
        // Business rule: Only pending orders can be paid
        boolean canProcess = OrderStatus.PENDING.equals(status);
        
        logger.debug("Order {} payment processing allowed: {}", orderId, canProcess);
        return canProcess;
    }
    
    @Override
    public void markOrderAsPaid(String orderId) {
        logger.info("Marking order {} as paid", orderId);
        
        validateOrderId(orderId);
        
        Order order = orderRepository.findById(Long.parseLong(orderId))
            .orElseThrow(() -> new IllegalArgumentException(ORDER_NOT_FOUND_MESSAGE + orderId));
        
        // Business rule: Validate order can be marked as paid
        if (!canProcessPayment(orderId)) {
            throw new IllegalStateException("Order " + orderId + " cannot be marked as paid");
        }
        
        order.setStatus(OrderStatus.CONFIRMED); // Paid orders are confirmed
        
        orderRepository.save(order);
        logger.info("Order {} successfully marked as paid", orderId);
    }
    
    @Override
    public void markOrderPaymentFailed(String orderId) {
        logger.info("Marking order {} payment as failed", orderId);
        
        validateOrderId(orderId);
        
        // Validate order exists
        orderRepository.findById(Long.parseLong(orderId))
            .orElseThrow(() -> new IllegalArgumentException(ORDER_NOT_FOUND_MESSAGE + orderId));
        
        // Keep as PENDING for retry, or could add PAYMENT_FAILED status
        logger.info("Order {} payment marked as failed", orderId);
    }
    
    @Override
    public void markOrderAsRefunded(String orderId) {
        logger.info("Marking order {} as refunded", orderId);
        
        validateOrderId(orderId);
        
        Order order = orderRepository.findById(Long.parseLong(orderId))
            .orElseThrow(() -> new IllegalArgumentException(ORDER_NOT_FOUND_MESSAGE + orderId));
        
        // Business rule: Only confirmed orders can be refunded
        if (!OrderStatus.CONFIRMED.equals(order.getStatus())) {
            throw new IllegalStateException("Only confirmed orders can be refunded. Order " + orderId + " status: " + order.getStatus());
        }
        
        order.setStatus(OrderStatus.CANCELLED); // Refunded orders are cancelled
        
        orderRepository.save(order);
        logger.info("Order {} successfully marked as refunded", orderId);
    }
    
    @Override
    public Long getOrderAmount(String orderId) {
        logger.debug("Getting order amount for order {}", orderId);
        
        validateOrderId(orderId);
        
        Order order = orderRepository.findById(Long.parseLong(orderId))
            .orElseThrow(() -> new IllegalArgumentException(ORDER_NOT_FOUND_MESSAGE + orderId));
        
        Long amount = order.getTotalAfterVAT();
        logger.debug("Order {} amount: {}", orderId, amount);
        
        return amount;
    }
    
    @Override
    public boolean orderExists(String orderId) {
        logger.debug("Checking if order {} exists", orderId);
        
        try {
            validateOrderId(orderId);
            boolean exists = orderRepository.existsById(Long.parseLong(orderId));
            logger.debug("Order {} exists: {}", orderId, exists);
            return exists;
        } catch (IllegalArgumentException e) {
            logger.debug("Invalid order ID format: {}", orderId);
            return false;
        }
    }
    
    @Override
    public String getOrderStatus(String orderId) {
        logger.debug("Getting order status for order {}", orderId);
        
        validateOrderId(orderId);
        
        Order order = orderRepository.findById(Long.parseLong(orderId))
            .orElseThrow(() -> new IllegalArgumentException(ORDER_NOT_FOUND_MESSAGE + orderId));
        
        String status = order.getStatus().name();
        logger.debug("Order {} status: {}", orderId, status);
        
        return status;
    }
    
    // Additional domain business methods
    
    /**
     * Cancel order with business validation
     */
    public void cancelOrder(String orderId) {
        logger.info("Cancelling order {}", orderId);
        
        validateOrderId(orderId);
        
        Order order = orderRepository.findById(Long.parseLong(orderId))
            .orElseThrow(() -> new IllegalArgumentException(ORDER_NOT_FOUND_MESSAGE + orderId));
        
        // Use order's business logic for cancellation
        order.cancel();
        
        orderRepository.save(order);
        logger.info("Order {} successfully cancelled", orderId);
    }
    
    /**
     * Get order by ID with business context
     */
    public Optional<Order> getOrderById(String orderId) {
        logger.debug("Retrieving order with ID: {}", orderId);
        
        validateOrderId(orderId);
        
        return orderRepository.findById(Long.parseLong(orderId));
    }
    
    /**
     * Create order with business validation
     */
    public Order createOrder(Order order) {
        logger.debug("Creating new order");
        
        // Domain validation
        validateOrderForCreation(order);
        
        // Business rule: Set initial status if not set
        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.PENDING);
        }
        
        Order savedOrder = orderRepository.save(order);
        logger.info("Order created successfully with ID: {}", savedOrder.getOrderId());
        
        return savedOrder;
    }
    
    /**
     * Confirm order with business validation
     */
    public void confirmOrder(String orderId) {
        logger.info("Confirming order {}", orderId);
        
        validateOrderId(orderId);
        
        Order order = orderRepository.findById(Long.parseLong(orderId))
            .orElseThrow(() -> new IllegalArgumentException(ORDER_NOT_FOUND_MESSAGE + orderId));
        
        // Use order's business logic for confirmation
        order.confirm();
        
        orderRepository.save(order);
        logger.info("Order {} successfully confirmed", orderId);
    }
    
    // Private helper methods for business validation
    
    private void validateOrderId(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }
        
        try {
            Long.parseLong(orderId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid order ID format: " + orderId);
        }
    }
    
    private void validateOrderForCreation(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        
        if (!order.isValid()) {
            throw new IllegalArgumentException("Order validation failed");
        }
        
        // Additional business rules can be added here
    }
}
