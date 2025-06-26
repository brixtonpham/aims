package com.aims.domain.order.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Order entity
 * Tests business logic and domain methods without external dependencies
 */
@DisplayName("Order Entity Unit Tests")
class OrderTest {

    private Order order;
    private DeliveryInfo deliveryInfo;

    @BeforeEach
    void setUp() {
        // Create test delivery info
        deliveryInfo = DeliveryInfo.builder()
            .deliveryId(1L)
            .name("Test Customer")
            .phone("0123456789")
            .address("123 Test Street")
            .province("Ho Chi Minh City")
            .deliveryType(DeliveryInfo.DeliveryType.STANDARD)
            .build();

        // Create test order
        order = Order.builder()
            .orderId(1L)
            .customerId("CUST001")
            .status(Order.OrderStatus.PENDING)
            .vatRate(10)
            .isRushOrder(false)
            .orderTime(LocalDateTime.now())
            .paymentMethod("VNPAY")
            .deliveryInfo(deliveryInfo)
            .build();
    }

    @Nested
    @DisplayName("Order Item Management Tests")
    class OrderItemManagementTests {

        @Test
        @DisplayName("Should add order item successfully")
        void addOrderItem_ValidData_ShouldAddSuccessfully() {
            // When
            OrderItem item = order.addOrderItem(1L, "Test Product", 2, 50000);

            // Then
            assertNotNull(item);
            assertEquals(1, order.getOrderItems().size());
            assertEquals("Test Product", item.getProductTitle());
            assertEquals(2, item.getQuantity());
            assertEquals(50000, item.getUnitPrice());
            assertEquals(100000L, item.getTotalPrice());
        }

        @Test
        @DisplayName("Should calculate totals after adding items")
        void addOrderItem_ShouldCalculateTotals() {
            // When
            order.addOrderItem(1L, "Product 1", 2, 50000);
            order.addOrderItem(2L, "Product 2", 1, 30000);

            // Then
            assertEquals(130000L, order.getTotalBeforeVAT());
            assertEquals(143000L, order.getTotalAfterVAT()); // 130000 + 10% VAT
        }

        @Test
        @DisplayName("Should remove order item successfully")
        void removeOrderItem_ExistingItem_ShouldRemoveSuccessfully() {
            // Given
            OrderItem item = order.addOrderItem(1L, "Test Product", 2, 50000);
            Long itemId = item.getOrderItemId();

            // When
            boolean removed = order.removeOrderItem(itemId);

            // Then
            assertTrue(removed);
            assertEquals(0, order.getOrderItems().size());
            assertEquals(0L, order.getTotalBeforeVAT());
            assertEquals(0L, order.getTotalAfterVAT());
        }

        @Test
        @DisplayName("Should return false when removing non-existent item")
        void removeOrderItem_NonExistentItem_ShouldReturnFalse() {
            // When
            boolean removed = order.removeOrderItem(999L);

            // Then
            assertFalse(removed);
        }

        @Test
        @DisplayName("Should get correct total items count")
        void getTotalItems_WithMultipleItems_ShouldReturnCorrectCount() {
            // Given
            order.addOrderItem(1L, "Product 1", 2, 50000);
            order.addOrderItem(2L, "Product 2", 3, 30000);

            // When
            int totalItems = order.getTotalItems();

            // Then
            assertEquals(5, totalItems); // 2 + 3
        }
    }

    @Nested
    @DisplayName("Order Status Management Tests")
    class OrderStatusManagementTests {

        @Test
        @DisplayName("Should confirm pending order successfully")
        void confirm_PendingOrder_ShouldConfirmSuccessfully() {
            // Given
            order.setStatus(Order.OrderStatus.PENDING);

            // When
            order.confirm();

            // Then
            assertEquals(Order.OrderStatus.CONFIRMED, order.getStatus());
        }

        @Test
        @DisplayName("Should throw exception when confirming non-pending order")
        void confirm_NonPendingOrder_ShouldThrowException() {
            // Given
            order.setStatus(Order.OrderStatus.CONFIRMED);

            // When & Then
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> order.confirm()
            );
            assertEquals("Only pending orders can be confirmed", exception.getMessage());
        }

        @Test
        @DisplayName("Should ship confirmed order successfully")
        void ship_ConfirmedOrder_ShouldShipSuccessfully() {
            // Given
            order.setStatus(Order.OrderStatus.CONFIRMED);

            // When
            order.ship();

            // Then
            assertEquals(Order.OrderStatus.SHIPPED, order.getStatus());
        }

        @Test
        @DisplayName("Should throw exception when shipping non-confirmed order")
        void ship_NonConfirmedOrder_ShouldThrowException() {
            // Given
            order.setStatus(Order.OrderStatus.PENDING);

            // When & Then
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> order.ship()
            );
            assertEquals("Only confirmed orders can be shipped", exception.getMessage());
        }

        @Test
        @DisplayName("Should deliver shipped order successfully")
        void deliver_ShippedOrder_ShouldDeliverSuccessfully() {
            // Given
            order.setStatus(Order.OrderStatus.SHIPPED);

            // When
            order.deliver();

            // Then
            assertEquals(Order.OrderStatus.DELIVERED, order.getStatus());
            assertTrue(order.isCompleted());
        }

        @Test
        @DisplayName("Should throw exception when delivering non-shipped order")
        void deliver_NonShippedOrder_ShouldThrowException() {
            // Given
            order.setStatus(Order.OrderStatus.CONFIRMED);

            // When & Then
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> order.deliver()
            );
            assertEquals("Only shipped orders can be delivered", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Order Cancellation Tests")
    class OrderCancellationTests {

        @Test
        @DisplayName("Should cancel pending order successfully")
        void cancel_PendingOrder_ShouldCancelSuccessfully() {
            // Given
            order.setStatus(Order.OrderStatus.PENDING);

            // When
            order.cancel();

            // Then
            assertEquals(Order.OrderStatus.CANCELLED, order.getStatus());
            assertTrue(order.isCancelled());
        }

        @Test
        @DisplayName("Should cancel confirmed order successfully")
        void cancel_ConfirmedOrder_ShouldCancelSuccessfully() {
            // Given
            order.setStatus(Order.OrderStatus.CONFIRMED);

            // When
            order.cancel();

            // Then
            assertEquals(Order.OrderStatus.CANCELLED, order.getStatus());
        }

        @Test
        @DisplayName("Should throw exception when cancelling delivered order")
        void cancel_DeliveredOrder_ShouldThrowException() {
            // Given
            order.setStatus(Order.OrderStatus.DELIVERED);

            // When & Then
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> order.cancel()
            );
            assertTrue(exception.getMessage().contains("cannot be cancelled"));
        }

        @Test
        @DisplayName("Should check if order can be cancelled")
        void canBeCancelled_DifferentStatuses_ShouldReturnCorrectValues() {
            // Test pending order
            order.setStatus(Order.OrderStatus.PENDING);
            assertTrue(order.canBeCancelled());

            // Test confirmed order
            order.setStatus(Order.OrderStatus.CONFIRMED);
            assertTrue(order.canBeCancelled());

            // Test shipped order
            order.setStatus(Order.OrderStatus.SHIPPED);
            assertFalse(order.canBeCancelled());

            // Test delivered order
            order.setStatus(Order.OrderStatus.DELIVERED);
            assertFalse(order.canBeCancelled());
        }
    }

    @Nested
    @DisplayName("Order Calculation Tests")
    class OrderCalculationTests {

        @Test
        @DisplayName("Should recalculate totals correctly")
        void recalculateTotals_WithItems_ShouldCalculateCorrectly() {
            // Given
            order.addOrderItem(1L, "Product 1", 2, 100000); // 200,000
            order.addOrderItem(2L, "Product 2", 1, 50000);  // 50,000
            // Total before VAT: 250,000
            // VAT 10%: 25,000
            // Total after VAT: 275,000

            // When
            order.recalculateTotals();

            // Then
            assertEquals(250000L, order.getTotalBeforeVAT());
            assertEquals(275000L, order.getTotalAfterVAT());
        }

        @Test
        @DisplayName("Should get formatted total correctly")
        void getFormattedTotal_WithAmount_ShouldFormatCorrectly() {
            // Given
            order.addOrderItem(1L, "Product", 1, 100000);

            // When
            String formattedTotal = order.getFormattedTotal();

            // Then
            assertEquals("110,000 VND", formattedTotal); // 100,000 + 10% VAT
        }

        @Test
        @DisplayName("Should calculate total weight correctly")
        void getTotalWeight_WithItems_ShouldCalculateCorrectly() {
            // Given - assuming each item weighs 1.0kg for simplicity
            order.addOrderItem(1L, "Product 1", 2, 50000); // 2 * 1.0 = 2.0kg
            order.addOrderItem(2L, "Product 2", 3, 30000); // 3 * 1.0 = 3.0kg

            // When
            float totalWeight = order.getTotalWeight();

            // Then
            assertEquals(5.0f, totalWeight); // 2.0 + 3.0
        }
    }

    @Nested
    @DisplayName("Order Validation Tests")
    class OrderValidationTests {

        @Test
        @DisplayName("Should be valid with items and proper data")
        void isValid_WithItemsAndProperData_ShouldReturnTrue() {
            // Given
            order.addOrderItem(1L, "Product", 1, 50000);

            // When
            boolean isValid = order.isValid();

            // Then
            assertTrue(isValid);
        }

        @Test
        @DisplayName("Should be invalid without items")
        void isValid_WithoutItems_ShouldReturnFalse() {
            // When
            boolean isValid = order.isValid();

            // Then
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should be invalid with null status")
        void isValid_WithNullStatus_ShouldReturnFalse() {
            // Given
            order.addOrderItem(1L, "Product", 1, 50000);
            order.setStatus(null);

            // When
            boolean isValid = order.isValid();

            // Then
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should be invalid with negative totals")
        void isValid_WithNegativeTotals_ShouldReturnFalse() {
            // Given
            order.addOrderItem(1L, "Product", 1, 50000);
            order.setTotalBeforeVAT(-1000L);

            // When
            boolean isValid = order.isValid();

            // Then
            assertFalse(isValid);
        }
    }

    @Nested
    @DisplayName("Order Status Check Tests")
    class OrderStatusCheckTests {

        @Test
        @DisplayName("Should correctly identify completed order")
        void isCompleted_DeliveredOrder_ShouldReturnTrue() {
            // Given
            order.setStatus(Order.OrderStatus.DELIVERED);

            // When & Then
            assertTrue(order.isCompleted());
        }

        @Test
        @DisplayName("Should correctly identify non-completed order")
        void isCompleted_NonDeliveredOrder_ShouldReturnFalse() {
            // Given
            order.setStatus(Order.OrderStatus.SHIPPED);

            // When & Then
            assertFalse(order.isCompleted());
        }

        @Test
        @DisplayName("Should correctly identify cancelled order")
        void isCancelled_CancelledOrder_ShouldReturnTrue() {
            // Given
            order.setStatus(Order.OrderStatus.CANCELLED);

            // When & Then
            assertTrue(order.isCancelled());
        }

        @Test
        @DisplayName("Should correctly identify non-cancelled order")
        void isCancelled_NonCancelledOrder_ShouldReturnFalse() {
            // Given
            order.setStatus(Order.OrderStatus.PENDING);

            // When & Then
            assertFalse(order.isCancelled());
        }
    }

    @Nested
    @DisplayName("Order Builder Tests")
    class OrderBuilderTests {

        @Test
        @DisplayName("Should create order with builder pattern")
        void builder_WithValidData_ShouldCreateOrderSuccessfully() {
            // When
            Order newOrder = Order.builder()
                .customerId("CUST002")
                .status(Order.OrderStatus.PENDING)
                .vatRate(10)
                .isRushOrder(true)
                .paymentMethod("CASH")
                .build();

            // Then
            assertNotNull(newOrder);
            assertEquals("CUST002", newOrder.getCustomerId());
            assertEquals(Order.OrderStatus.PENDING, newOrder.getStatus());
            assertEquals(10, newOrder.getVatRate());
            assertTrue(newOrder.getIsRushOrder());
            assertEquals("CASH", newOrder.getPaymentMethod());
        }

        @Test
        @DisplayName("Should set default values with builder")
        void builder_WithDefaults_ShouldSetDefaultValues() {
            // When
            Order newOrder = Order.builder().build();

            // Then
            assertEquals(Order.OrderStatus.PENDING, newOrder.getStatus());
            assertEquals(10, newOrder.getVatRate());
            assertFalse(newOrder.getIsRushOrder());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle empty order items list")
        void getTotalItems_EmptyOrderItems_ShouldReturnZero() {
            // When
            int totalItems = order.getTotalItems();

            // Then
            assertEquals(0, totalItems);
        }

        @Test
        @DisplayName("Should handle zero VAT rate")
        void recalculateTotals_ZeroVATRate_ShouldCalculateCorrectly() {
            // Given
            order.setVatRate(0);
            order.addOrderItem(1L, "Product", 1, 100000);

            // When
            order.recalculateTotals();

            // Then
            assertEquals(100000L, order.getTotalBeforeVAT());
            assertEquals(100000L, order.getTotalAfterVAT()); // No VAT
        }

        @Test
        @DisplayName("Should handle high VAT rate")
        void recalculateTotals_HighVATRate_ShouldCalculateCorrectly() {
            // Given
            order.setVatRate(25); // 25% VAT
            order.addOrderItem(1L, "Product", 1, 100000);

            // When
            order.recalculateTotals();

            // Then
            assertEquals(100000L, order.getTotalBeforeVAT());
            assertEquals(125000L, order.getTotalAfterVAT()); // 100,000 + 25%
        }
    }
}