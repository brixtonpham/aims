package com.aims.domain.cart.entity;

import com.aims.domain.product.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Cart entity
 * Tests the business logic and behavior of Cart domain object
 */
@DisplayName("Cart Entity Tests")
class CartTest {

    private Cart cart;
    private Product product1;
    private Product product2;
    private Product outOfStockProduct;

    @BeforeEach
    void setUp() {
        // Initialize cart
        cart = Cart.builder()
                .cartId(1L)
                .customerId("customer123")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Initialize products
        product1 = Product.builder()
                .productId(1L)
                .title("Test Product 1")
                .price(100)
                .quantity(10)
                .weight(0.5f)
                .rushOrderSupported(true)
                .type("book")
                .build();

        product2 = Product.builder()
                .productId(2L)
                .title("Test Product 2")
                .price(200)
                .quantity(5)
                .weight(1.0f)
                .rushOrderSupported(false)
                .type("cd")
                .build();

        outOfStockProduct = Product.builder()
                .productId(3L)
                .title("Out of Stock Product")
                .price(150)
                .quantity(0)
                .weight(0.3f)
                .rushOrderSupported(true)
                .type("dvd")
                .build();
    }

    @Nested
    @DisplayName("Add Product Tests")
    class AddProductTests {

        @Test
        @DisplayName("Should add new product successfully")
        void addProduct_NewProduct_ShouldAddSuccessfully() {
            // When
            CartItem cartItem = cart.addProduct(product1, 2);

            // Then
            assertNotNull(cartItem);
            assertEquals(product1, cartItem.getProduct());
            assertEquals(2, cartItem.getQuantity());
            assertEquals(1, cart.getCartItems().size());
            assertEquals(2, cart.getTotalItems());
        }

        @Test
        @DisplayName("Should increase quantity when adding existing product")
        void addProduct_ExistingProduct_ShouldIncreaseQuantity() {
            // Given
            cart.addProduct(product1, 2);

            // When
            CartItem cartItem = cart.addProduct(product1, 3);

            // Then
            assertEquals(5, cartItem.getQuantity());
            assertEquals(1, cart.getCartItems().size());
            assertEquals(5, cart.getTotalItems());
        }

        @Test
        @DisplayName("Should throw exception when product is null")
        void addProduct_NullProduct_ShouldThrowException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> cart.addProduct(null, 2)
            );
            assertEquals("Product and quantity must be valid", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when quantity is zero or negative")
        void addProduct_InvalidQuantity_ShouldThrowException() {
            // Zero quantity
            IllegalArgumentException exception1 = assertThrows(
                    IllegalArgumentException.class,
                    () -> cart.addProduct(product1, 0)
            );
            assertEquals("Product and quantity must be valid", exception1.getMessage());

            // Negative quantity
            IllegalArgumentException exception2 = assertThrows(
                    IllegalArgumentException.class,
                    () -> cart.addProduct(product1, -1)
            );
            assertEquals("Product and quantity must be valid", exception2.getMessage());
        }
    }

    @Nested
    @DisplayName("Update Product Quantity Tests")
    class UpdateProductQuantityTests {

        @Test
        @DisplayName("Should update product quantity successfully")
        void updateProductQuantity_ValidQuantity_ShouldUpdateSuccessfully() {
            // Given
            cart.addProduct(product1, 2);

            // When
            boolean result = cart.updateProductQuantity(product1.getProductId(), 5);

            // Then
            assertTrue(result);
            Optional<CartItem> cartItem = cart.findCartItemByProduct(product1.getProductId());
            assertTrue(cartItem.isPresent());
            assertEquals(5, cartItem.get().getQuantity());
        }

        @Test
        @DisplayName("Should remove product when quantity is zero")
        void updateProductQuantity_ZeroQuantity_ShouldRemoveProduct() {
            // Given
            cart.addProduct(product1, 2);

            // When
            boolean result = cart.updateProductQuantity(product1.getProductId(), 0);

            // Then
            assertTrue(result);
            assertTrue(cart.isEmpty());
            assertEquals(0, cart.getTotalItems());
        }

        @Test
        @DisplayName("Should return false when product not found")
        void updateProductQuantity_ProductNotFound_ShouldReturnFalse() {
            // When
            boolean result = cart.updateProductQuantity(999L, 5);

            // Then
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Remove Product Tests")
    class RemoveProductTests {

        @Test
        @DisplayName("Should remove product successfully")
        void removeProduct_ExistingProduct_ShouldRemoveSuccessfully() {
            // Given
            cart.addProduct(product1, 2);
            cart.addProduct(product2, 1);

            // When
            boolean result = cart.removeProduct(product1.getProductId());

            // Then
            assertTrue(result);
            assertEquals(1, cart.getCartItems().size());
            assertEquals(1, cart.getTotalItems());
            assertFalse(cart.findCartItemByProduct(product1.getProductId()).isPresent());
        }

        @Test
        @DisplayName("Should return false when product not found")
        void removeProduct_ProductNotFound_ShouldReturnFalse() {
            // When
            boolean result = cart.removeProduct(999L);

            // Then
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Cart Calculations Tests")
    class CalculationTests {

        @Test
        @DisplayName("Should calculate total items correctly")
        void getTotalItems_MultipleProducts_ShouldCalculateCorrectly() {
            // Given
            cart.addProduct(product1, 2);
            cart.addProduct(product2, 3);

            // When
            int totalItems = cart.getTotalItems();

            // Then
            assertEquals(5, totalItems);
        }

        @Test
        @DisplayName("Should calculate total value correctly")
        void getTotalValue_MultipleProducts_ShouldCalculateCorrectly() {
            // Given
            cart.addProduct(product1, 2); // 2 * 100 = 200
            cart.addProduct(product2, 3); // 3 * 200 = 600

            // When
            long totalValue = cart.getTotalValue();

            // Then
            assertEquals(800, totalValue); // 200 + 600
        }

        @Test
        @DisplayName("Should calculate total weight correctly")
        void getTotalWeight_MultipleProducts_ShouldCalculateCorrectly() {
            // Given
            cart.addProduct(product1, 2); // 2 * 0.5 = 1.0
            cart.addProduct(product2, 1); // 1 * 1.0 = 1.0

            // When
            float totalWeight = cart.getTotalWeight();

            // Then
            assertEquals(2.0f, totalWeight, 0.01f); // 1.0 + 1.0
        }

        @Test
        @DisplayName("Should return zero for empty cart calculations")
        void emptyCart_Calculations_ShouldReturnZero() {
            // When & Then
            assertEquals(0, cart.getTotalItems());
            assertEquals(0, cart.getTotalValue());
            assertEquals(0.0f, cart.getTotalWeight(), 0.01f);
        }
    }

    @Nested
    @DisplayName("Cart Status Tests")
    class StatusTests {

        @Test
        @DisplayName("Should return true when cart is empty")
        void isEmpty_EmptyCart_ShouldReturnTrue() {
            // When & Then
            assertTrue(cart.isEmpty());
        }

        @Test
        @DisplayName("Should return false when cart has items")
        void isEmpty_CartWithItems_ShouldReturnFalse() {
            // Given
            cart.addProduct(product1, 1);

            // When & Then
            assertFalse(cart.isEmpty());
        }

        @Test
        @DisplayName("Should return true when all products are available")
        void areAllProductsAvailable_AllAvailable_ShouldReturnTrue() {
            // Given
            cart.addProduct(product1, 2); // product1 has quantity 10, requesting 2
            cart.addProduct(product2, 1); // product2 has quantity 5, requesting 1

            // When & Then
            assertTrue(cart.areAllProductsAvailable());
        }

        @Test
        @DisplayName("Should return false when some products are unavailable")
        void areAllProductsAvailable_SomeUnavailable_ShouldReturnFalse() {
            // Given
            cart.addProduct(product1, 2);
            cart.addProduct(outOfStockProduct, 1); // out of stock product

            // When & Then
            assertFalse(cart.areAllProductsAvailable());
        }

        @Test
        @DisplayName("Should return true when cart is valid")
        void isValid_ValidCart_ShouldReturnTrue() {
            // Given
            cart.addProduct(product1, 2);

            // When & Then
            assertTrue(cart.isValid());
        }

        @Test
        @DisplayName("Should return false when cart is invalid (empty)")
        void isValid_EmptyCart_ShouldReturnFalse() {
            // When & Then
            assertFalse(cart.isValid());
        }
    }

    @Nested
    @DisplayName("Rush Order Tests")
    class RushOrderTests {

        @Test
        @DisplayName("Should support rush order when all products support it")
        void supportsRushOrder_AllProductsSupport_ShouldReturnTrue() {
            // Given
            Product rushProduct1 = Product.builder()
                    .productId(10L)
                    .title("Rush Product 1")
                    .price(100)
                    .quantity(10)
                    .rushOrderSupported(true)
                    .build();

            Product rushProduct2 = Product.builder()
                    .productId(11L)
                    .title("Rush Product 2")
                    .price(200)
                    .quantity(5)
                    .rushOrderSupported(true)
                    .build();

            cart.addProduct(rushProduct1, 1);
            cart.addProduct(rushProduct2, 1);

            // When & Then
            assertTrue(cart.supportsRushOrder());
        }

        @Test
        @DisplayName("Should not support rush order when some products don't support it")
        void supportsRushOrder_SomeProductsDontSupport_ShouldReturnFalse() {
            // Given
            cart.addProduct(product1, 1); // supports rush order
            cart.addProduct(product2, 1); // doesn't support rush order

            // When & Then
            assertFalse(cart.supportsRushOrder());
        }

        @Test
        @DisplayName("Should return false for empty cart")
        void supportsRushOrder_EmptyCart_ShouldReturnFalse() {
            // When & Then
            assertFalse(cart.supportsRushOrder());
        }
    }

    @Nested
    @DisplayName("Search and Utility Tests")
    class SearchUtilityTests {

        @Test
        @DisplayName("Should find cart item by product ID")
        void findCartItemByProduct_ExistingProduct_ShouldReturnCartItem() {
            // Given
            cart.addProduct(product1, 2);

            // When
            Optional<CartItem> found = cart.findCartItemByProduct(product1.getProductId());

            // Then
            assertTrue(found.isPresent());
            assertEquals(product1, found.get().getProduct());
            assertEquals(2, found.get().getQuantity());
        }

        @Test
        @DisplayName("Should return empty when product not found")
        void findCartItemByProduct_ProductNotFound_ShouldReturnEmpty() {
            // When
            Optional<CartItem> found = cart.findCartItemByProduct(999L);

            // Then
            assertTrue(found.isEmpty());
        }

        @Test
        @DisplayName("Should get unavailable items correctly")
        void getUnavailableItems_WithUnavailableProducts_ShouldReturnUnavailableItems() {
            // Given
            cart.addProduct(product1, 2); // available
            cart.addProduct(outOfStockProduct, 1); // unavailable

            // When
            var unavailableItems = cart.getUnavailableItems();

            // Then
            assertEquals(1, unavailableItems.size());
            assertEquals(outOfStockProduct, unavailableItems.get(0).getProduct());
        }

        @Test
        @DisplayName("Should return empty list when all items are available")
        void getUnavailableItems_AllAvailable_ShouldReturnEmptyList() {
            // Given
            cart.addProduct(product1, 2);
            cart.addProduct(product2, 1);

            // When
            var unavailableItems = cart.getUnavailableItems();

            // Then
            assertTrue(unavailableItems.isEmpty());
        }
    }

    @Nested
    @DisplayName("Clear Cart Tests")
    class ClearCartTests {

        @Test
        @DisplayName("Should clear all items from cart")
        void clearCart_WithItems_ShouldClearAllItems() {
            // Given
            cart.addProduct(product1, 2);
            cart.addProduct(product2, 1);
            assertFalse(cart.isEmpty());

            // When
            cart.clearCart();

            // Then
            assertTrue(cart.isEmpty());
            assertEquals(0, cart.getTotalItems());
            assertEquals(0, cart.getTotalValue());
        }

        @Test
        @DisplayName("Should handle clearing empty cart")
        void clearCart_EmptyCart_ShouldHandleGracefully() {
            // When & Then (should not throw exception)
            assertDoesNotThrow(() -> cart.clearCart());
            assertTrue(cart.isEmpty());
        }
    }
}