package com.aims.application.order;

import com.aims.application.commands.CancelOrderCommand;
import com.aims.application.commands.PlaceOrderCommand;
import com.aims.application.dto.order.CancellationResult;
import com.aims.application.dto.order.OrderCreationResult;
import com.aims.application.dto.order.OrderTrackingResult;
import com.aims.domain.notification.service.NotificationService;
import com.aims.domain.order.service.OrderDomainService;
import com.aims.domain.payment.service.PaymentDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OrderApplicationService.
 * Tests the application service layer orchestration and cross-cutting concerns.
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 1 - Foundation
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderApplicationService Unit Tests")
class OrderApplicationServiceTest {
    
    @Mock
    private OrderDomainService orderDomainService;
    
    @Mock
    private PaymentDomainService paymentDomainService;
    
    @Mock
    private NotificationService notificationService;
    
    @InjectMocks
    private OrderApplicationService orderApplicationService;
    
    private PlaceOrderCommand placeOrderCommand;
    private CancelOrderCommand cancelOrderCommand;
    
    @BeforeEach
    void setUp() {
        // Setup test data
        PlaceOrderCommand.OrderItemCommand orderItem = PlaceOrderCommand.OrderItemCommand.builder()
                .productId("PROD-001")
                .productName("Test Product")
                .quantity(2)
                .unitPrice(50000.0)
                .variant("Standard")
                .build();
        
        PlaceOrderCommand.DeliveryAddressCommand deliveryAddress = PlaceOrderCommand.DeliveryAddressCommand.builder()
                .streetAddress("123 Test Street")
                .city("Ho Chi Minh City")
                .state("Ho Chi Minh")
                .postalCode("700000")
                .country("Vietnam")
                .build();
        
        placeOrderCommand = PlaceOrderCommand.builder()
                .customerId("CUST-001")
                .customerName("John Doe")
                .customerEmail("john.doe@example.com")
                .customerPhone("+84901234567")
                .orderItems(Arrays.asList(orderItem))
                .deliveryAddress(deliveryAddress)
                .paymentMethod("VNPAY")
                .rushOrder(false)
                .deliveryInstructions("Leave at door")
                .orderNotes("Test order")
                .build();
        
        cancelOrderCommand = CancelOrderCommand.builder()
                .orderId("ORD-001")
                .customerId("CUST-001")
                .cancellationReason("Customer requested cancellation")
                .processRefund(true)
                .additionalNotes("Test cancellation")
                .build();
    }
    
    @Test
    @DisplayName("Should successfully place order with VNPAY payment")
    void shouldPlaceOrderSuccessfullyWithVNPay() {
        // When
        OrderCreationResult result = orderApplicationService.placeOrder(placeOrderCommand);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getOrderId());
        assertTrue(result.getOrderId().startsWith("ORD-"));
        assertEquals("PENDING", result.getOrderStatus());
        assertEquals(100000.0, result.getTotalAmount()); // 2 * 50000
        assertEquals("VND", result.getCurrency());
        assertNotNull(result.getPaymentUrl());
        assertTrue(result.getPaymentUrl().contains("vnpayment.vn"));
        assertEquals("Order created successfully", result.getMessage());
    }
    
    @Test
    @DisplayName("Should successfully place order with COD payment")
    void shouldPlaceOrderSuccessfullyWithCOD() {
        // Given
        placeOrderCommand.setPaymentMethod("COD");
        
        // When
        OrderCreationResult result = orderApplicationService.placeOrder(placeOrderCommand);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getOrderId());
        assertTrue(result.getOrderId().startsWith("ORD-"));
        assertEquals("PENDING", result.getOrderStatus());
        assertEquals(100000.0, result.getTotalAmount()); // 2 * 50000
        assertEquals("VND", result.getCurrency());
        assertNull(result.getPaymentUrl()); // No payment URL for COD
        assertEquals("Order created successfully", result.getMessage());
    }
    
    @Test
    @DisplayName("Should handle order placement failure gracefully")
    void shouldHandleOrderPlacementFailure() {
        // Given - command with empty order items to trigger validation failure
        placeOrderCommand.setOrderItems(Arrays.asList());
        
        // When
        OrderCreationResult result = orderApplicationService.placeOrder(placeOrderCommand);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("ORDER_CREATION_FAILED", result.getErrorCode());
        assertNotNull(result.getMessage());
        assertTrue(result.getMessage().contains("Failed to create order"));
    }
    
    @Test
    @DisplayName("Should successfully cancel order with refund")
    void shouldCancelOrderSuccessfullyWithRefund() {
        // When
        CancellationResult result = orderApplicationService.cancelOrder(cancelOrderCommand);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("ORD-001", result.getOrderId());
        assertEquals("CANCELLED", result.getOrderStatus());
        assertEquals(0.0, result.getRefundAmount()); // Mock value, will be actual in implementation
        assertEquals("VND", result.getCurrency());
        assertEquals("Order cancelled successfully", result.getMessage());
    }
    
    @Test
    @DisplayName("Should successfully cancel order without refund")
    void shouldCancelOrderSuccessfullyWithoutRefund() {
        // Given
        cancelOrderCommand.setProcessRefund(false);
        
        // When
        CancellationResult result = orderApplicationService.cancelOrder(cancelOrderCommand);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("ORD-001", result.getOrderId());
        assertEquals("CANCELLED", result.getOrderStatus());
        assertNotNull(result.getCancelledAt());
        assertEquals("Order cancelled successfully", result.getMessage());
    }
    
    @Test
    @DisplayName("Should handle order cancellation failure gracefully")
    void shouldHandleOrderCancellationFailure() {
        // Given - simulate validation failure by setting empty order ID
        cancelOrderCommand.setOrderId("");
        
        // When
        CancellationResult result = orderApplicationService.cancelOrder(cancelOrderCommand);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("ORDER_CANCELLATION_FAILED", result.getErrorCode());
        assertNotNull(result.getMessage());
        assertTrue(result.getMessage().contains("Failed to cancel order"));
    }
    
    @Test
    @DisplayName("Should successfully track order")
    void shouldTrackOrderSuccessfully() {
        // Given
        String orderId = "ORD-001";
        
        // When
        OrderTrackingResult result = orderApplicationService.trackOrder(orderId);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(orderId, result.getOrderId());
        assertEquals("PENDING", result.getCurrentStatus());
        assertEquals("Order is being processed", result.getStatusDescription());
        assertEquals("Order tracking retrieved successfully", result.getMessage());
    }
    
    @Test
    @DisplayName("Should handle order tracking failure gracefully")
    void shouldHandleOrderTrackingFailure() {
        // Given - null order ID to trigger failure
        String orderId = null;
        
        // When
        OrderTrackingResult result = orderApplicationService.trackOrder(orderId);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("ORDER_TRACKING_FAILED", result.getErrorCode());
        assertNotNull(result.getMessage());
        assertTrue(result.getMessage().contains("Failed to track order"));
    }
    
    @Test
    @DisplayName("Should validate place order command properly")
    void shouldValidatePlaceOrderCommand() {
        // Given - command with empty order items
        placeOrderCommand.setOrderItems(Arrays.asList());
        
        // When & Then
        OrderCreationResult result = orderApplicationService.placeOrder(placeOrderCommand);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Order must contain at least one item"));
    }
    
    @Test
    @DisplayName("Should identify online payment methods correctly")
    void shouldIdentifyOnlinePaymentMethods() {
        // Test VNPAY (online payment)
        placeOrderCommand.setPaymentMethod("VNPAY");
        OrderCreationResult vnpayResult = orderApplicationService.placeOrder(placeOrderCommand);
        assertNotNull(vnpayResult.getPaymentUrl());
        
        // Test COD (offline payment)
        placeOrderCommand.setPaymentMethod("COD");
        OrderCreationResult codResult = orderApplicationService.placeOrder(placeOrderCommand);
        assertNull(codResult.getPaymentUrl());
    }
    
    @Test
    @DisplayName("Should calculate total amount correctly")
    void shouldCalculateTotalAmountCorrectly() {
        // Given - 2 items with different prices
        PlaceOrderCommand.OrderItemCommand item1 = PlaceOrderCommand.OrderItemCommand.builder()
                .productId("PROD-001")
                .productName("Product 1")
                .quantity(2)
                .unitPrice(30000.0)
                .build();
        
        PlaceOrderCommand.OrderItemCommand item2 = PlaceOrderCommand.OrderItemCommand.builder()
                .productId("PROD-002")
                .productName("Product 2")
                .quantity(1)
                .unitPrice(50000.0)
                .build();
        
        placeOrderCommand.setOrderItems(Arrays.asList(item1, item2));
        
        // When
        OrderCreationResult result = orderApplicationService.placeOrder(placeOrderCommand);
        
        // Then
        assertEquals(110000.0, result.getTotalAmount()); // (2*30000) + (1*50000) = 110000
    }
}
