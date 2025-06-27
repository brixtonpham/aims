package com.aims.domain.order.service;

import com.aims.domain.order.dto.CreateOrderRequest;
import com.aims.domain.order.dto.OrderItemRequest;
import com.aims.domain.order.entity.OrderStatus;
import com.aims.domain.order.exception.OrderDomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OrderDomainService interface and related domain objects.
 * Validates that all domain concepts are properly defined and accessible.
 * 
 * This test serves as validation for Task 1.1 completion.
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 1 - Foundation
 */
class OrderDomainServiceTest {

    @Test
    @DisplayName("Should validate CreateOrderRequest domain object structure")
    void testCreateOrderRequestStructure() {
        // Given: A valid order request
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId("CUST001");
        request.setCustomerName("John Doe");
        request.setCustomerEmail("john.doe@example.com");
        request.setDeliveryAddress("123 Main St");
        
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId("PROD001");
        item.setQuantity(2);
        item.setUnitPrice(50000.0);
        
        request.setOrderItems(List.of(item));
        
        // When: Validating the request
        boolean isValid = request.isValid();
        
        // Then: Request should be valid
        assertTrue(isValid, "CreateOrderRequest should be valid with all required fields");
        assertEquals("CUST001", request.getCustomerId());
        assertEquals("John Doe", request.getCustomerName());
        assertEquals(1, request.getOrderItems().size());
        assertEquals(2, request.getTotalQuantity());
    }

    @Test
    @DisplayName("Should validate OrderStatus enum functionality")
    void testOrderStatusEnum() {
        // Given: Different order statuses
        OrderStatus pending = OrderStatus.PENDING;
        OrderStatus confirmed = OrderStatus.CONFIRMED;
        OrderStatus cancelled = OrderStatus.CANCELLED;
        
        // When & Then: Test status properties
        assertTrue(pending.isCancellable(), "PENDING orders should be cancellable");
        assertTrue(pending.isModifiable(), "PENDING orders should be modifiable");
        assertFalse(pending.isTerminal(), "PENDING is not a terminal status");
        
        assertTrue(confirmed.isCancellable(), "CONFIRMED orders should be cancellable");
        assertFalse(confirmed.isModifiable(), "CONFIRMED orders should not be modifiable");
        
        assertFalse(cancelled.isCancellable(), "CANCELLED orders should not be cancellable");
        assertTrue(cancelled.isTerminal(), "CANCELLED is a terminal status");
        assertTrue(cancelled.isRefundEligible(), "CANCELLED orders should be refund eligible");
        
        // Test valid transitions
        assertTrue(pending.canTransitionTo(confirmed), "PENDING should transition to CONFIRMED");
        assertTrue(pending.canTransitionTo(cancelled), "PENDING should transition to CANCELLED");
        assertFalse(cancelled.canTransitionTo(confirmed), "CANCELLED should not transition to CONFIRMED");
    }

    @Test
    @DisplayName("Should validate OrderDomainException functionality")
    void testOrderDomainException() {
        // Given: Different exception scenarios
        String orderId = "ORDER123";
        String customerId = "CUST001";
        
        // When: Creating specific exceptions
        OrderDomainException notFoundEx = OrderDomainException.orderNotFound(orderId);
        OrderDomainException notCancellableEx = OrderDomainException.orderNotCancellable(orderId, "DELIVERED");
        OrderDomainException notAuthorizedEx = OrderDomainException.customerNotAuthorized(customerId, orderId);
        
        // Then: Exceptions should have proper error codes and messages
        assertEquals("ORDER_NOT_FOUND", notFoundEx.getErrorCode());
        assertTrue(notFoundEx.getMessage().contains(orderId));
        
        assertEquals("ORDER_NOT_CANCELLABLE", notCancellableEx.getErrorCode());
        assertTrue(notCancellableEx.getMessage().contains("DELIVERED"));
        
        assertEquals("CUSTOMER_NOT_AUTHORIZED", notAuthorizedEx.getErrorCode());
        assertTrue(notAuthorizedEx.getMessage().contains(customerId));
        assertTrue(notAuthorizedEx.getMessage().contains(orderId));
    }

    @Test
    @DisplayName("Should validate domain service interface contract")
    void testDomainServiceInterfaceContract() {
        // This test validates that our interface is properly structured
        // by checking method signatures through reflection
        
        Class<OrderDomainService> serviceClass = OrderDomainService.class;
        
        // Verify key methods exist
        assertDoesNotThrow(() -> {
            serviceClass.getMethod("createOrder", CreateOrderRequest.class);
            serviceClass.getMethod("getOrderById", String.class);
            serviceClass.getMethod("cancelOrder", String.class);
            serviceClass.getMethod("getOrderStatus", String.class);
            serviceClass.getMethod("getOrdersByCustomer", String.class);
        }, "All core OrderDomainService methods should be defined");
        
        // Verify interface is properly structured
        assertTrue(serviceClass.isInterface(), "OrderDomainService should be an interface");
        assertEquals("com.aims.domain.order.service", serviceClass.getPackageName());
    }

    @Test
    @DisplayName("Should validate OrderItemRequest business methods")
    void testOrderItemRequestBusinessMethods() {
        // Given: An order item request
        OrderItemRequest item = new OrderItemRequest("PROD001", "Test Product", 3, 25000.0);
        
        // When & Then: Test business methods
        assertTrue(item.isValid(), "Item with required fields should be valid");
        assertEquals(75000.0, item.getTotalPrice(), "Total price should be quantity * unit price");
        assertEquals("PROD001", item.getProductId());
        assertEquals(3, item.getQuantity());
        
        // Test invalid item
        OrderItemRequest invalidItem = new OrderItemRequest();
        assertFalse(invalidItem.isValid(), "Item without required fields should be invalid");
    }
}
