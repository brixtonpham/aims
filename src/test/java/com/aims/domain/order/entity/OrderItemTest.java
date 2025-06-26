package com.aims.domain.order.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OrderItem entity
 * Tests business logic, validations, and state transitions
 */
@DisplayName("OrderItem Tests")
class OrderItemTest {

    private OrderItem orderItem;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        // Create test order
        testOrder = Order.builder()
            .orderId(1L)
            .customerId("customer123")
            .build();

        // Create test order item
        orderItem = OrderItem.builder()
            .orderItemId(1L)
            .order(testOrder)
            .productId(100L)
            .productTitle("Test Product")
            .quantity(2)
            .unitPrice(50000)
            .status(OrderItem.OrderItemStatus.PENDING)
            .rushOrderEnabled(false)
            .build();
    }

    @Nested
    @DisplayName("Construction and Validation Tests")
    class ConstructionAndValidationTests {

        @Test
        @DisplayName("Should create valid order item with all required fields")
        void createOrderItem_WithValidData_ShouldSucceed() {
            // Assert
            assertEquals(1L, orderItem.getOrderItemId());
            assertEquals(testOrder, orderItem.getOrder());
            assertEquals(100L, orderItem.getProductId());
            assertEquals("Test Product", orderItem.getProductTitle());
            assertEquals(2, orderItem.getQuantity());
            assertEquals(50000, orderItem.getUnitPrice());
            assertEquals(OrderItem.OrderItemStatus.PENDING, orderItem.getStatus());
            assertFalse(orderItem.getRushOrderEnabled());
        }

        @Test
        @DisplayName("Should validate order item correctly")
        void isValid_WithValidOrderItem_ShouldReturnTrue() {
            // Act
            boolean isValid = orderItem.isValid();

            // Assert
            assertTrue(isValid);
        }

        @Test
        @DisplayName("Should invalidate order item with null product ID")
        void isValid_WithNullProductId_ShouldReturnFalse() {
            // Arrange
            orderItem.setProductId(null);

            // Act
            boolean isValid = orderItem.isValid();

            // Assert
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should invalidate order item with zero quantity")
        void isValid_WithZeroQuantity_ShouldReturnFalse() {
            // Arrange
            orderItem.setQuantity(0);

            // Act
            boolean isValid = orderItem.isValid();

            // Assert
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should invalidate order item with negative unit price")
        void isValid_WithNegativeUnitPrice_ShouldReturnFalse() {
            // Arrange
            orderItem.setUnitPrice(-1000);

            // Act
            boolean isValid = orderItem.isValid();

            // Assert
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should invalidate order item with null status")
        void isValid_WithNullStatus_ShouldReturnFalse() {
            // Arrange
            orderItem.setStatus(null);

            // Act
            boolean isValid = orderItem.isValid();

            // Assert
            assertFalse(isValid);
        }
    }

    @Nested
    @DisplayName("Calculation Tests")
    class CalculationTests {

        @Test
        @DisplayName("Should calculate total fee correctly")
        void calculateTotalFee_WithValidQuantityAndPrice_ShouldCalculateCorrectly() {
            // Act
            orderItem.calculateTotalFee();

            // Assert
            assertEquals(100000L, orderItem.getTotalFee()); // 2 * 50000
        }

        @Test
        @DisplayName("Should get total price correctly")
        void getTotalPrice_WithCalculatedFee_ShouldReturnCorrectPrice() {
            // Arrange
            orderItem.calculateTotalFee();

            // Act
            long totalPrice = orderItem.getTotalPrice();

            // Assert
            assertEquals(100000L, totalPrice);
        }

        @Test
        @DisplayName("Should get total price from calculation when totalFee is null")
        void getTotalPrice_WithNullTotalFee_ShouldCalculateFromQuantityAndPrice() {
            // Arrange
            orderItem.setTotalFee(null);

            // Act
            long totalPrice = orderItem.getTotalPrice();

            // Assert
            assertEquals(100000L, totalPrice); // 2 * 50000
        }

        @Test
        @DisplayName("Should return zero when quantity or price is null")
        void getTotalPrice_WithNullQuantityOrPrice_ShouldReturnZero() {
            // Arrange
            orderItem.setQuantity(null);
            orderItem.setTotalFee(null);

            // Act
            long totalPrice = orderItem.getTotalPrice();

            // Assert
            assertEquals(0L, totalPrice);
        }

        @Test
        @DisplayName("Should get formatted total correctly")
        void getFormattedTotal_WithValidTotal_ShouldReturnFormattedString() {
            // Arrange
            orderItem.calculateTotalFee();

            // Act
            String formattedTotal = orderItem.getFormattedTotal();

            // Assert
            assertEquals("100,000 VND", formattedTotal);
        }

        @Test
        @DisplayName("Should calculate total weight correctly")
        void getTotalWeight_WithValidQuantity_ShouldReturnWeight() {
            // Act
            float totalWeight = orderItem.getTotalWeight();

            // Assert
            assertEquals(2.0f, totalWeight); // 2 * 1.0f (default weight per item)
        }
    }

    @Nested
    @DisplayName("Update Methods Tests")
    class UpdateMethodsTests {

        @Test
        @DisplayName("Should update quantity successfully")
        void updateQuantity_WithValidQuantity_ShouldUpdateAndRecalculate() {
            // Act
            orderItem.updateQuantity(5);

            // Assert
            assertEquals(5, orderItem.getQuantity());
            assertEquals(250000L, orderItem.getTotalFee()); // 5 * 50000
        }

        @Test
        @DisplayName("Should throw exception when updating with zero quantity")
        void updateQuantity_WithZeroQuantity_ShouldThrowException() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderItem.updateQuantity(0)
            );

            assertEquals("Quantity must be positive", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when updating with negative quantity")
        void updateQuantity_WithNegativeQuantity_ShouldThrowException() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderItem.updateQuantity(-1)
            );

            assertEquals("Quantity must be positive", exception.getMessage());
        }

        @Test
        @DisplayName("Should update unit price successfully")
        void updateUnitPrice_WithValidPrice_ShouldUpdateAndRecalculate() {
            // Act
            orderItem.updateUnitPrice(75000);

            // Assert
            assertEquals(75000, orderItem.getUnitPrice());
            assertEquals(150000L, orderItem.getTotalFee()); // 2 * 75000
        }

        @Test
        @DisplayName("Should throw exception when updating with negative unit price")
        void updateUnitPrice_WithNegativePrice_ShouldThrowException() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderItem.updateUnitPrice(-1000)
            );

            assertEquals("Unit price cannot be negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should accept zero unit price")
        void updateUnitPrice_WithZeroPrice_ShouldUpdate() {
            // Act
            orderItem.updateUnitPrice(0);

            // Assert
            assertEquals(0, orderItem.getUnitPrice());
            assertEquals(0L, orderItem.getTotalFee());
        }
    }

    @Nested
    @DisplayName("Status Transition Tests")
    class StatusTransitionTests {

        @Test
        @DisplayName("Should confirm pending order item")
        void confirm_PendingItem_ShouldChangeToConfirmed() {
            // Arrange
            assertEquals(OrderItem.OrderItemStatus.PENDING, orderItem.getStatus());

            // Act
            orderItem.confirm();

            // Assert
            assertEquals(OrderItem.OrderItemStatus.CONFIRMED, orderItem.getStatus());
        }

        @Test
        @DisplayName("Should throw exception when confirming non-pending item")
        void confirm_NonPendingItem_ShouldThrowException() {
            // Arrange
            orderItem.setStatus(OrderItem.OrderItemStatus.CONFIRMED);

            // Act & Assert
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> orderItem.confirm()
            );

            assertEquals("Only pending items can be confirmed", exception.getMessage());
        }

        @Test
        @DisplayName("Should ship confirmed order item")
        void ship_ConfirmedItem_ShouldChangeToShipped() {
            // Arrange
            orderItem.setStatus(OrderItem.OrderItemStatus.CONFIRMED);

            // Act
            orderItem.ship();

            // Assert
            assertEquals(OrderItem.OrderItemStatus.SHIPPED, orderItem.getStatus());
        }

        @Test
        @DisplayName("Should throw exception when shipping non-confirmed item")
        void ship_NonConfirmedItem_ShouldThrowException() {
            // Arrange
            orderItem.setStatus(OrderItem.OrderItemStatus.PENDING);

            // Act & Assert
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> orderItem.ship()
            );

            assertEquals("Only confirmed items can be shipped", exception.getMessage());
        }

        @Test
        @DisplayName("Should deliver shipped order item")
        void deliver_ShippedItem_ShouldChangeToDeliveredAndSetTime() {
            // Arrange
            orderItem.setStatus(OrderItem.OrderItemStatus.SHIPPED);
            LocalDateTime beforeDelivery = LocalDateTime.now();

            // Act
            orderItem.deliver();

            // Assert
            assertEquals(OrderItem.OrderItemStatus.DELIVERED, orderItem.getStatus());
            assertNotNull(orderItem.getDeliveryTime());
            assertTrue(orderItem.getDeliveryTime().isAfter(beforeDelivery));
        }

        @Test
        @DisplayName("Should throw exception when delivering non-shipped item")
        void deliver_NonShippedItem_ShouldThrowException() {
            // Arrange
            orderItem.setStatus(OrderItem.OrderItemStatus.CONFIRMED);

            // Act & Assert
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> orderItem.deliver()
            );

            assertEquals("Only shipped items can be delivered", exception.getMessage());
        }

        @Test
        @DisplayName("Should cancel non-delivered order item")
        void cancel_NonDeliveredItem_ShouldChangeToCancelled() {
            // Arrange
            orderItem.setStatus(OrderItem.OrderItemStatus.CONFIRMED);

            // Act
            orderItem.cancel();

            // Assert
            assertEquals(OrderItem.OrderItemStatus.CANCELLED, orderItem.getStatus());
        }

        @Test
        @DisplayName("Should throw exception when cancelling delivered item")
        void cancel_DeliveredItem_ShouldThrowException() {
            // Arrange
            orderItem.setStatus(OrderItem.OrderItemStatus.DELIVERED);

            // Act & Assert
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> orderItem.cancel()
            );

            assertEquals("Delivered items cannot be cancelled", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Status Check Tests")
    class StatusCheckTests {

        @Test
        @DisplayName("Should correctly identify delivered item")
        void isDelivered_DeliveredItem_ShouldReturnTrue() {
            // Arrange
            orderItem.setStatus(OrderItem.OrderItemStatus.DELIVERED);

            // Act & Assert
            assertTrue(orderItem.isDelivered());
        }

        @Test
        @DisplayName("Should correctly identify non-delivered item")
        void isDelivered_NonDeliveredItem_ShouldReturnFalse() {
            // Arrange
            orderItem.setStatus(OrderItem.OrderItemStatus.SHIPPED);

            // Act & Assert
            assertFalse(orderItem.isDelivered());
        }

        @Test
        @DisplayName("Should correctly identify cancelled item")
        void isCancelled_CancelledItem_ShouldReturnTrue() {
            // Arrange
            orderItem.setStatus(OrderItem.OrderItemStatus.CANCELLED);

            // Act & Assert
            assertTrue(orderItem.isCancelled());
        }

        @Test
        @DisplayName("Should correctly identify non-cancelled item")
        void isCancelled_NonCancelledItem_ShouldReturnFalse() {
            // Arrange
            orderItem.setStatus(OrderItem.OrderItemStatus.DELIVERED);

            // Act & Assert
            assertFalse(orderItem.isCancelled());
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("Should create order item with factory method")
        void create_WithValidParameters_ShouldCreateOrderItem() {
            // Act
            OrderItem created = OrderItem.create(testOrder, 200L, "Factory Product", 3, 25000);

            // Assert
            assertEquals(testOrder, created.getOrder());
            assertEquals(200L, created.getProductId());
            assertEquals("Factory Product", created.getProductTitle());
            assertEquals(3, created.getQuantity());
            assertEquals(25000, created.getUnitPrice());
        }

        @Test
        @DisplayName("Should throw exception when creating with null order")
        void create_WithNullOrder_ShouldThrowException() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> OrderItem.create(null, 200L, "Product", 3, 25000)
            );

            assertEquals("Order cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when creating with null product ID")
        void create_WithNullProductId_ShouldThrowException() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> OrderItem.create(testOrder, null, "Product", 3, 25000)
            );

            assertEquals("Product ID cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when creating with zero quantity")
        void create_WithZeroQuantity_ShouldThrowException() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> OrderItem.create(testOrder, 200L, "Product", 0, 25000)
            );

            assertEquals("Quantity must be positive", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when creating with negative unit price")
        void create_WithNegativeUnitPrice_ShouldThrowException() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> OrderItem.create(testOrder, 200L, "Product", 3, -1000)
            );

            assertEquals("Unit price cannot be negative", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Complete Workflow Tests")
    class CompleteWorkflowTests {

        @Test
        @DisplayName("Should complete full order item lifecycle")
        void orderItemLifecycle_CompleteFlow_ShouldSucceed() {
            // Start with pending item
            assertEquals(OrderItem.OrderItemStatus.PENDING, orderItem.getStatus());
            assertFalse(orderItem.isDelivered());
            assertFalse(orderItem.isCancelled());

            // Confirm the item
            orderItem.confirm();
            assertEquals(OrderItem.OrderItemStatus.CONFIRMED, orderItem.getStatus());

            // Ship the item
            orderItem.ship();
            assertEquals(OrderItem.OrderItemStatus.SHIPPED, orderItem.getStatus());

            // Deliver the item
            orderItem.deliver();
            assertEquals(OrderItem.OrderItemStatus.DELIVERED, orderItem.getStatus());
            assertTrue(orderItem.isDelivered());
            assertNotNull(orderItem.getDeliveryTime());
        }

        @Test
        @DisplayName("Should handle order item cancellation workflow")
        void orderItemCancellation_FromPending_ShouldSucceed() {
            // Start with pending item
            assertEquals(OrderItem.OrderItemStatus.PENDING, orderItem.getStatus());

            // Cancel the item
            orderItem.cancel();
            assertEquals(OrderItem.OrderItemStatus.CANCELLED, orderItem.getStatus());
            assertTrue(orderItem.isCancelled());
        }

        @Test
        @DisplayName("Should handle quantity and price updates")
        void orderItemUpdates_QuantityAndPrice_ShouldRecalculate() {
            // Initial state
            orderItem.calculateTotalFee();
            assertEquals(100000L, orderItem.getTotalFee());

            // Update quantity
            orderItem.updateQuantity(4);
            assertEquals(200000L, orderItem.getTotalFee());

            // Update unit price
            orderItem.updateUnitPrice(30000);
            assertEquals(120000L, orderItem.getTotalFee());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle order item with zero unit price")
        void orderItem_WithZeroUnitPrice_ShouldCalculateCorrectly() {
            // Arrange
            orderItem.setUnitPrice(0);

            // Act
            orderItem.calculateTotalFee();

            // Assert
            assertEquals(0L, orderItem.getTotalFee());
            assertEquals("0 VND", orderItem.getFormattedTotal());
        }

        @Test
        @DisplayName("Should handle order item with large quantity")
        void orderItem_WithLargeQuantity_ShouldCalculateCorrectly() {
            // Arrange
            orderItem.setQuantity(1000);
            orderItem.setUnitPrice(100);

            // Act
            orderItem.calculateTotalFee();

            // Assert
            assertEquals(100000L, orderItem.getTotalFee());
            assertEquals(1000.0f, orderItem.getTotalWeight());
        }

        @Test
        @DisplayName("Should handle null totalFee gracefully")
        void getTotalPrice_WithNullValues_ShouldHandleGracefully() {
            // Arrange
            orderItem.setTotalFee(null);
            orderItem.setQuantity(null);
            orderItem.setUnitPrice(null);

            // Act
            long totalPrice = orderItem.getTotalPrice();
            float totalWeight = orderItem.getTotalWeight();

            // Assert
            assertEquals(0L, totalPrice);
            assertEquals(0.0f, totalWeight);
        }
    }
}