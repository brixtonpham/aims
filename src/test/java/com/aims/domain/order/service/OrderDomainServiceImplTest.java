package com.aims.domain.order.service;

import com.aims.domain.order.entity.Order;
import com.aims.domain.order.entity.Order.OrderStatus;
import com.aims.domain.order.repository.OrderRepository;
import com.aims.domain.order.dto.CreateOrderRequest;
import com.aims.domain.order.dto.OrderValidationResult;
import com.aims.domain.order.exception.OrderDomainException;
import com.aims.domain.order.service.event.DomainEventPublisher;
import com.aims.infrastructure.product.ProductService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderDomainServiceImpl
 * Validates business logic implementation without external dependencies
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 2 - Task 2.1
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderDomainServiceImpl Unit Tests")
class OrderDomainServiceImplTest {
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private ProductService productService;
    
    @Mock
    private DomainEventPublisher eventPublisher;
    
    private OrderDomainServiceImpl orderDomainService;
    
    @BeforeEach
    void setUp() {
        orderDomainService = new OrderDomainServiceImpl(orderRepository, productService, eventPublisher);
    }
    
    @Test
    @DisplayName("Should validate order request successfully")
    void validateOrder_ValidRequest_ShouldReturnValid() {
        // Given
        CreateOrderRequest request = createValidOrderRequest();
        
        // Mock product service responses
        when(productService.isProductAvailable(1L, 2)).thenReturn(true);
        when(productService.getProductWeight(1L)).thenReturn(1.0f);
        
        // When
        OrderValidationResult result = orderDomainService.validateOrder(request);
        
        // Then
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
    }
    
    @Test
    @DisplayName("Should reject order with invalid customer ID")
    void validateOrder_InvalidCustomerId_ShouldReturnInvalid() {
        // Given
        CreateOrderRequest request = createValidOrderRequest();
        request.setCustomerId(null);
        
        // When
        OrderValidationResult result = orderDomainService.validateOrder(request);
        
        // Then
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Customer ID is required"));
    }
    
    @Test
    @DisplayName("Should reject order with empty items")
    void validateOrder_EmptyItems_ShouldReturnInvalid() {
        // Given
        CreateOrderRequest request = createValidOrderRequest();
        request.setOrderItems(Arrays.asList());
        
        // When
        OrderValidationResult result = orderDomainService.validateOrder(request);
        
        // Then
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Order must contain at least one item"));
    }
    
    @Test
    @DisplayName("Should cancel order successfully when pending")
    void cancelOrder_PendingOrder_ShouldCancelSuccessfully() {
        // Given
        String orderId = "123";
        Order order = createMockOrder();
        order.setStatus(OrderStatus.PENDING);
        
        when(orderRepository.findById(123L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        
        // When
        assertDoesNotThrow(() -> orderDomainService.cancelOrder(orderId));
        
        // Then
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(eventPublisher).publish(any());
    }
    
    @Test
    @DisplayName("Should throw exception when cancelling non-pending order")
    void cancelOrder_NonPendingOrder_ShouldThrowException() {
        // Given
        String orderId = "123";
        Order order = createMockOrder();
        order.setStatus(OrderStatus.CONFIRMED);
        
        when(orderRepository.findById(123L)).thenReturn(Optional.of(order));
        
        // When & Then
        OrderDomainException exception = assertThrows(OrderDomainException.class, 
            () -> orderDomainService.cancelOrder(orderId));
        
        assertTrue(exception.getMessage().contains("cannot be cancelled"));
    }
    
    @Test
    @DisplayName("Should get order status correctly")
    void getOrderStatus_ExistingOrder_ShouldReturnStatus() {
        // Given
        String orderId = "123";
        Order order = createMockOrder();
        order.setStatus(OrderStatus.PENDING);
        
        when(orderRepository.findById(123L)).thenReturn(Optional.of(order));
        
        // When
        OrderStatus status = orderDomainService.getOrderStatus(orderId);
        
        // Then
        assertEquals(OrderStatus.PENDING, status);
    }
    
    @Test
    @DisplayName("Should throw exception for invalid order ID")
    void getOrderById_InvalidOrderId_ShouldThrowException() {
        // Given
        String invalidOrderId = "invalid";
        
        // When & Then
        assertThrows(OrderDomainException.class, 
            () -> orderDomainService.getOrderById(invalidOrderId));
    }
    
    @Test
    @DisplayName("Should check modification eligibility correctly")
    void canModifyOrder_PendingOrder_ShouldReturnTrue() {
        // Given
        String orderId = "123";
        Order order = createMockOrder();
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(java.time.LocalDateTime.now().minusMinutes(30)); // 30 minutes ago
        
        when(orderRepository.findById(123L)).thenReturn(Optional.of(order));
        
        // When
        boolean canModify = orderDomainService.canModifyOrder(orderId);
        
        // Then
        assertTrue(canModify);
    }
    
    private CreateOrderRequest createValidOrderRequest() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId("customer123");
        request.setCustomerName("Test Customer");
        request.setCustomerEmail("test@example.com");
        request.setPaymentMethod("VNPAY");
        request.setIsRushOrder(false);
        
        // Create order item
        CreateOrderRequest.OrderItemRequest item = new CreateOrderRequest.OrderItemRequest();
        item.setProductId(1L);
        item.setProductTitle("Test Product");
        item.setQuantity(2);
        item.setUnitPrice(50000);
        request.setOrderItems(Arrays.asList(item));
        
        // Create delivery info
        CreateOrderRequest.DeliveryInfoRequest deliveryInfo = new CreateOrderRequest.DeliveryInfoRequest();
        deliveryInfo.setName("Test Customer");
        deliveryInfo.setPhone("0123456789");
        deliveryInfo.setEmail("test@example.com");
        deliveryInfo.setAddress("123 Test Street");
        deliveryInfo.setProvince("Hanoi");
        request.setDeliveryInfo(deliveryInfo);
        
        return request;
    }
    
    private Order createMockOrder() {
        Order order = Order.builder()
            .orderId(123L)
            .customerId("customer123")
            .status(OrderStatus.PENDING)
            .totalBeforeVAT(100000L)
            .totalAfterVAT(110000L)
            .vatRate(10)
            .createdAt(java.time.LocalDateTime.now())
            .build();
        
        return order;
    }
}
