package com.aims.domain.order.service;

import com.aims.domain.order.entity.Order;
import com.aims.domain.order.entity.Order.OrderStatus;
import com.aims.domain.order.dto.CreateOrderRequest;
import com.aims.domain.order.dto.OrderTotal;
import com.aims.domain.order.dto.OrderValidationResult;
import com.aims.domain.order.exception.OrderDomainException;

import java.util.List;

/**
 * Domain service interface for Order business operations.
 * Encapsulates core business logic for order management without infrastructure concerns.
 * 
 * This interface follows Clean Architecture principles by:
 * - Defining pure business contracts
 * - Having no dependencies on external systems
 * - Being independent of frameworks and UI
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 1 - Foundation
 */
public interface OrderDomainService {

    /**
     * Creates a new order with comprehensive business validation.
     * 
     * Business Rules Applied:
     * - Validates customer information completeness
     * - Ensures all products are available and in stock
     * - Calculates total amount including taxes and delivery fees
     * - Applies business constraints (minimum order amount, delivery zones, etc.)
     * - Sets initial order status based on payment method
     * 
     * @param request Complete order creation request with customer, items, and delivery info
     * @return Created order with calculated totals and assigned status
     * @throws OrderDomainException when business rules are violated
     * @throws IllegalArgumentException when request parameters are invalid
     */
    Order createOrder(CreateOrderRequest request) throws OrderDomainException;

    /**
     * Retrieves order by unique identifier with business context.
     * 
     * @param orderId Unique order identifier
     * @return Order entity with complete business information
     * @throws OrderDomainException when order not found or access denied
     * @throws IllegalArgumentException when orderId is null or invalid
     */
    Order getOrderById(String orderId) throws OrderDomainException;

    /**
     * Cancels an existing order following business cancellation rules.
     * 
     * Business Rules Applied:
     * - Validates order is in cancellable status
     * - Checks cancellation time limits
     * - Determines refund eligibility
     * - Updates order status with cancellation reason
     * - Triggers inventory restoration
     * 
     * @param orderId Unique order identifier to cancel
     * @throws OrderDomainException when order cannot be cancelled per business rules
     * @throws IllegalArgumentException when orderId is null or invalid
     */
    void cancelOrder(String orderId) throws OrderDomainException;

    /**
     * Gets current order status with business meaning.
     * 
     * Status includes:
     * - Current processing stage
     * - Business-relevant timestamps
     * - Next possible actions
     * - Customer-facing status description
     * 
     * @param orderId Unique order identifier
     * @return Current order status with business context
     * @throws OrderDomainException when order not found
     * @throws IllegalArgumentException when orderId is null or invalid
     */
    OrderStatus getOrderStatus(String orderId) throws OrderDomainException;

    /**
     * Retrieves all orders for a specific customer with business filtering.
     * 
     * Business Rules Applied:
     * - Returns only orders accessible to the customer
     * - Sorts by business relevance (recent first, pending first)
     * - Applies customer-specific business rules
     * 
     * @param customerId Unique customer identifier
     * @return List of orders belonging to the customer, empty if none found
     * @throws OrderDomainException when customer validation fails
     * @throws IllegalArgumentException when customerId is null or invalid
     */
    List<Order> getOrdersByCustomer(String customerId) throws OrderDomainException;

    /**
     * Validates order eligibility for modification based on business rules.
     * 
     * Validation includes:
     * - Order status allows modification
     * - Time constraints are met
     * - Customer authorization
     * - Payment status considerations
     * 
     * @param orderId Unique order identifier
     * @return true if order can be modified, false otherwise
     * @throws OrderDomainException when validation fails
     */
    boolean canModifyOrder(String orderId) throws OrderDomainException;

    /**
     * Updates order status through valid business state transitions.
     * 
     * Business Rules Applied:
     * - Validates status transition is allowed
     * - Updates related business entities
     * - Triggers appropriate business events
     * - Maintains audit trail
     * 
     * @param orderId Unique order identifier
     * @param newStatus Target status for transition
     * @param reason Business reason for status change
     * @throws OrderDomainException when status transition is invalid
     */
    void updateOrderStatus(String orderId, OrderStatus newStatus, String reason) throws OrderDomainException;

    /**
     * Calculates total order amount including all business components.
     * 
     * Calculation Components:
     * - Base product prices
     * - Quantity-based discounts
     * - Tax calculations
     * - Delivery fees
     * - Rush order surcharges
     * - Promotional discounts
     * 
     * @param request Order request with items and delivery information
     * @return Calculated total amount with breakdown
     * @throws OrderDomainException when calculation fails due to business rules
     */
    OrderTotal calculateOrderTotal(CreateOrderRequest request) throws OrderDomainException;

    /**
     * Validates order against all applicable business rules.
     * 
     * Validation includes:
     * - Customer eligibility
     * - Product availability
     * - Delivery feasibility
     * - Business constraints
     * - Regulatory compliance
     * 
     * @param request Order creation request
     * @return Validation result with any business rule violations
     */
    OrderValidationResult validateOrder(CreateOrderRequest request);
}
