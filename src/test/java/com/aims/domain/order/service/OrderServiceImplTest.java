package com.aims.domain.order.service;

import com.aims.domain.order.entity.Order;
import com.aims.domain.order.entity.Order.OrderStatus;
import com.aims.domain.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderServiceImpl
 * Validates domain service business logic without external dependencies
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceImpl Unit Tests")
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;
    private static final String TEST_ORDER_ID = "12345";
    private static final Long TEST_ORDER_ID_LONG = 12345L;

    @BeforeEach
    void setUp() {
        testOrder = Order.builder()
            .orderId(TEST_ORDER_ID_LONG)
            .customerId("customer123")
            .totalBeforeVAT(100000L)
            .totalAfterVAT(110000L)
            .status(OrderStatus.PENDING)
            .vatRate(10)
            .orderTime(LocalDateTime.now())
            .paymentMethod("CREDIT_CARD")
            .isRushOrder(false)
            .build();
        
        // Add an order item to make the order valid
        testOrder.addOrderItem(1L, "Test Product", 2, 50000);
    }

    @Test
    @DisplayName("Should return correct order amount")
    void getOrderAmount_ValidOrder_ReturnsAmount() {
        // Given
        when(orderRepository.findById(TEST_ORDER_ID_LONG)).thenReturn(Optional.of(testOrder));

        // When
        Long amount = orderService.getOrderAmount(TEST_ORDER_ID);

        // Then
        assertEquals(110000L, amount);
        verify(orderRepository).findById(TEST_ORDER_ID_LONG);
    }

    @Test
    @DisplayName("Should throw exception when getting amount for non-existent order")
    void getOrderAmount_NonExistentOrder_ThrowsException() {
        // Given
        when(orderRepository.findById(TEST_ORDER_ID_LONG)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> orderService.getOrderAmount(TEST_ORDER_ID));

        assertTrue(exception.getMessage().contains("Order not found"));
        verify(orderRepository).findById(TEST_ORDER_ID_LONG);
    }

    @Test
    @DisplayName("Should return true when order exists")
    void orderExists_ValidOrder_ReturnsTrue() {
        // Given
        when(orderRepository.existsById(TEST_ORDER_ID_LONG)).thenReturn(true);

        // When
        boolean exists = orderService.orderExists(TEST_ORDER_ID);

        // Then
        assertTrue(exists);
        verify(orderRepository).existsById(TEST_ORDER_ID_LONG);
    }

    @Test
    @DisplayName("Should return false when order does not exist")
    void orderExists_NonExistentOrder_ReturnsFalse() {
        // Given
        when(orderRepository.existsById(TEST_ORDER_ID_LONG)).thenReturn(false);

        // When
        boolean exists = orderService.orderExists(TEST_ORDER_ID);

        // Then
        assertFalse(exists);
        verify(orderRepository).existsById(TEST_ORDER_ID_LONG);
    }

    @Test
    @DisplayName("Should return false for invalid order ID format")
    void orderExists_InvalidOrderId_ReturnsFalse() {
        // When
        boolean exists = orderService.orderExists("invalid");

        // Then
        assertFalse(exists);
        verify(orderRepository, never()).existsById(any());
    }

    @Test
    @DisplayName("Should return correct order status")
    void getOrderStatus_ValidOrder_ReturnsStatus() {
        // Given
        when(orderRepository.findById(TEST_ORDER_ID_LONG)).thenReturn(Optional.of(testOrder));

        // When
        String status = orderService.getOrderStatus(TEST_ORDER_ID);

        // Then
        assertEquals("PENDING", status);
        verify(orderRepository).findById(TEST_ORDER_ID_LONG);
    }

    @Test
    @DisplayName("Should successfully cancel order")
    void cancelOrder_ValidOrder_Success() {
        // Given
        when(orderRepository.findById(TEST_ORDER_ID_LONG)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        orderService.cancelOrder(TEST_ORDER_ID);

        // Then
        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
        verify(orderRepository).findById(TEST_ORDER_ID_LONG);
        verify(orderRepository).save(testOrder);
    }

    @Test
    @DisplayName("Should throw exception when cancelling non-cancellable order")
    void cancelOrder_DeliveredOrder_ThrowsException() {
        // Given
        testOrder.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(TEST_ORDER_ID_LONG)).thenReturn(Optional.of(testOrder));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> orderService.cancelOrder(TEST_ORDER_ID));

        assertTrue(exception.getMessage().contains("cannot be cancelled"));
        verify(orderRepository).findById(TEST_ORDER_ID_LONG);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully create order")
    void createOrder_ValidOrder_ReturnsCreatedOrder() {
        // Given
        Order newOrder = Order.builder()
            .customerId("customer456")
            .totalBeforeVAT(50000L)
            .totalAfterVAT(55000L)
            .vatRate(10)
            .paymentMethod("CREDIT_CARD")
            .isRushOrder(false)
            .build();
        
        // Add an order item to make the order valid
        newOrder.addOrderItem(1L, "Test Product", 1, 50000);

        when(orderRepository.save(any(Order.class))).thenReturn(newOrder);

        // When
        Order result = orderService.createOrder(newOrder);

        // Then
        assertNotNull(result);
        assertEquals(OrderStatus.PENDING, result.getStatus());
        verify(orderRepository).save(newOrder);
    }

    @Test
    @DisplayName("Should throw exception when creating invalid order")
    void createOrder_InvalidOrder_ThrowsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class,
            () -> orderService.createOrder(null));

        // Given invalid order (empty cart)
        Order invalidOrder = Order.builder().build();

        assertThrows(IllegalArgumentException.class,
            () -> orderService.createOrder(invalidOrder));

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully confirm order")
    void confirmOrder_PendingOrder_Success() {
        // Given
        when(orderRepository.findById(TEST_ORDER_ID_LONG)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        orderService.confirmOrder(TEST_ORDER_ID);

        // Then
        assertEquals(OrderStatus.CONFIRMED, testOrder.getStatus());
        verify(orderRepository).findById(TEST_ORDER_ID_LONG);
        verify(orderRepository).save(testOrder);
    }

    @Test
    @DisplayName("Should throw exception when confirming non-pending order")
    void confirmOrder_ConfirmedOrder_ThrowsException() {
        // Given
        testOrder.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findById(TEST_ORDER_ID_LONG)).thenReturn(Optional.of(testOrder));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> orderService.confirmOrder(TEST_ORDER_ID));

        assertTrue(exception.getMessage().contains("Only pending orders can be confirmed"));
        verify(orderRepository).findById(TEST_ORDER_ID_LONG);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return order by ID")
    void getOrderById_ValidOrder_ReturnsOrder() {
        // Given
        when(orderRepository.findById(TEST_ORDER_ID_LONG)).thenReturn(Optional.of(testOrder));

        // When
        Optional<Order> result = orderService.getOrderById(TEST_ORDER_ID);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testOrder, result.get());
        verify(orderRepository).findById(TEST_ORDER_ID_LONG);
    }

    @Test
    @DisplayName("Should return empty for non-existent order")
    void getOrderById_NonExistentOrder_ReturnsEmpty() {
        // Given
        when(orderRepository.findById(TEST_ORDER_ID_LONG)).thenReturn(Optional.empty());

        // When
        Optional<Order> result = orderService.getOrderById(TEST_ORDER_ID);

        // Then
        assertTrue(result.isEmpty());
        verify(orderRepository).findById(TEST_ORDER_ID_LONG);
    }
}