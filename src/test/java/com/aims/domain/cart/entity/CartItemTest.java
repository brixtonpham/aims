package com.aims.domain.cart.entity;

import com.aims.domain.product.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CartItem entity
 * Tests business logic and validation without external dependencies
 */
@DisplayName("CartItem Entity Tests")
class CartItemTest {

    private CartItem cartItem;
    private Product testProduct;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        // Create test product
        testProduct = Product.builder()
            .productId(1L)
            .title("Test Product")
            .price(50000) // 50,000 VND
            .quantity(10)
            .weight(0.5f)
            .rushOrderSupported(true)
            .build();

        // Create test cart
        testCart = Cart.builder()
            .cartId(1L)
            .customerId("customer123")
            .build();

        // Create test cart item
        cartItem = CartItem.builder()
            .cartItemId(1L)
            .cart(testCart)
            .product(testProduct)
            .quantity(2)
            .build();
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should calculate total price correctly")
        void getTotalPrice_ValidData_CalculatesCorrectly() {
            // When
            long totalPrice = cartItem.getTotalPrice();

            // Then
            assertEquals(100000L, totalPrice); // 2 * 50,000 = 100,000
        }

        @Test
        @DisplayName("Should return zero when product is null")
        void getTotalPrice_NullProduct_ReturnsZero() {
            // Given
            cartItem.setProduct(null);

            // When
            long totalPrice = cartItem.getTotalPrice();

            // Then
            assertEquals(0L, totalPrice);
        }

        @Test
        @DisplayName("Should return zero when quantity is null")
        void getTotalPrice_NullQuantity_ReturnsZero() {
            // Given
            cartItem.setQuantity(null);

            // When
            long totalPrice = cartItem.getTotalPrice();

            // Then
            assertEquals(0L, totalPrice);
        }

        @Test
        @DisplayName("Should update quantity successfully")
        void updateQuantity_ValidQuantity_UpdatesSuccessfully() {
            // When
            cartItem.updateQuantity(5);

            // Then
            assertEquals(5, cartItem.getQuantity());
        }

        @Test
        @DisplayName("Should throw exception for zero quantity")
        void updateQuantity_ZeroQuantity_ThrowsException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cartItem.updateQuantity(0)
            );
            
            assertEquals("Quantity must be positive", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for negative quantity")
        void updateQuantity_NegativeQuantity_ThrowsException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cartItem.updateQuantity(-1)
            );
            
            assertEquals("Quantity must be positive", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when insufficient stock")
        void updateQuantity_InsufficientStock_ThrowsException() {
            // Given - product has only 10 in stock
            testProduct.setQuantity(5);

            // When & Then
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> cartItem.updateQuantity(15) // Request more than available
            );
            
            assertEquals("Insufficient stock for requested quantity", exception.getMessage());
        }

        @Test
        @DisplayName("Should check availability correctly")
        void isAvailable_SufficientStock_ReturnsTrue() {
            // Given - product has 10 in stock, cart item wants 2
            // When
            boolean available = cartItem.isAvailable();

            // Then
            assertTrue(available);
        }

        @Test
        @DisplayName("Should return false when insufficient stock")
        void isAvailable_InsufficientStock_ReturnsFalse() {
            // Given
            testProduct.setQuantity(1); // Less than cart item quantity (2)

            // When
            boolean available = cartItem.isAvailable();

            // Then
            assertFalse(available);
        }

        @Test
        @DisplayName("Should return false when product is null")
        void isAvailable_NullProduct_ReturnsFalse() {
            // Given
            cartItem.setProduct(null);

            // When
            boolean available = cartItem.isAvailable();

            // Then
            assertFalse(available);
        }

        @Test
        @DisplayName("Should get unit price from product")
        void getUnitPrice_ValidProduct_ReturnsProductPrice() {
            // When
            Integer unitPrice = cartItem.getUnitPrice();

            // Then
            assertEquals(50000, unitPrice);
        }

        @Test
        @DisplayName("Should return zero when product is null")
        void getUnitPrice_NullProduct_ReturnsZero() {
            // Given
            cartItem.setProduct(null);

            // When
            Integer unitPrice = cartItem.getUnitPrice();

            // Then
            assertEquals(0, unitPrice);
        }

        @Test
        @DisplayName("Should get product title")
        void getProductTitle_ValidProduct_ReturnsTitle() {
            // When
            String title = cartItem.getProductTitle();

            // Then
            assertEquals("Test Product", title);
        }

        @Test
        @DisplayName("Should return default title when product is null")
        void getProductTitle_NullProduct_ReturnsDefault() {
            // Given
            cartItem.setProduct(null);

            // When
            String title = cartItem.getProductTitle();

            // Then
            assertEquals("Unknown Product", title);
        }

        @Test
        @DisplayName("Should check rush order support")
        void supportsRushOrder_ProductSupportsRush_ReturnsTrue() {
            // When
            boolean supportsRush = cartItem.supportsRushOrder();

            // Then
            assertTrue(supportsRush);
        }

        @Test
        @DisplayName("Should return false when product doesn't support rush")
        void supportsRushOrder_ProductDoesNotSupportRush_ReturnsFalse() {
            // Given
            testProduct.setRushOrderSupported(false);

            // When
            boolean supportsRush = cartItem.supportsRushOrder();

            // Then
            assertFalse(supportsRush);
        }

        @Test
        @DisplayName("Should calculate total weight correctly")
        void getTotalWeight_ValidData_CalculatesCorrectly() {
            // When
            float totalWeight = cartItem.getTotalWeight();

            // Then
            assertEquals(1.0f, totalWeight); // 2 * 0.5 = 1.0
        }

        @Test
        @DisplayName("Should return zero weight when product is null")
        void getTotalWeight_NullProduct_ReturnsZero() {
            // Given
            cartItem.setProduct(null);

            // When
            float totalWeight = cartItem.getTotalWeight();

            // Then
            assertEquals(0.0f, totalWeight);
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should be valid with all required fields")
        void isValid_AllFieldsValid_ReturnsTrue() {
            // When
            boolean valid = cartItem.isValid();

            // Then
            assertTrue(valid);
        }

        @Test
        @DisplayName("Should be invalid when product is null")
        void isValid_NullProduct_ReturnsFalse() {
            // Given
            cartItem.setProduct(null);

            // When
            boolean valid = cartItem.isValid();

            // Then
            assertFalse(valid);
        }

        @Test
        @DisplayName("Should be invalid when quantity is null")
        void isValid_NullQuantity_ReturnsFalse() {
            // Given
            cartItem.setQuantity(null);

            // When
            boolean valid = cartItem.isValid();

            // Then
            assertFalse(valid);
        }

        @Test
        @DisplayName("Should be invalid when quantity is zero")
        void isValid_ZeroQuantity_ReturnsFalse() {
            // Given
            cartItem.setQuantity(0);

            // When
            boolean valid = cartItem.isValid();

            // Then
            assertFalse(valid);
        }

        @Test
        @DisplayName("Should be invalid when quantity is negative")
        void isValid_NegativeQuantity_ReturnsFalse() {
            // Given
            cartItem.setQuantity(-1);

            // When
            boolean valid = cartItem.isValid();

            // Then
            assertFalse(valid);
        }

        @Test
        @DisplayName("Should be invalid when product is not available")
        void isValid_ProductNotAvailable_ReturnsFalse() {
            // Given - set product quantity to less than cart item quantity
            testProduct.setQuantity(1); // Cart item has quantity 2

            // When
            boolean valid = cartItem.isValid();

            // Then
            assertFalse(valid);
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("Should create cart item with valid parameters")
        void create_ValidParameters_CreatesCartItem() {
            // When
            CartItem created = CartItem.create(testCart, testProduct, 3);

            // Then
            assertNotNull(created);
            assertEquals(testCart, created.getCart());
            assertEquals(testProduct, created.getProduct());
            assertEquals(3, created.getQuantity());
        }

        @Test
        @DisplayName("Should throw exception when cart is null")
        void create_NullCart_ThrowsException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> CartItem.create(null, testProduct, 2)
            );
            
            assertEquals("Cart cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when product is null")
        void create_NullProduct_ThrowsException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> CartItem.create(testCart, null, 2)
            );
            
            assertEquals("Product cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when quantity is zero")
        void create_ZeroQuantity_ThrowsException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> CartItem.create(testCart, testProduct, 0)
            );
            
            assertEquals("Quantity must be positive", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when quantity is negative")
        void create_NegativeQuantity_ThrowsException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> CartItem.create(testCart, testProduct, -1)
            );
            
            assertEquals("Quantity must be positive", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when product not available")
        void create_ProductNotAvailable_ThrowsException() {
            // Given
            testProduct.setQuantity(1); // Less than requested quantity

            // When & Then
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> CartItem.create(testCart, testProduct, 5)
            );
            
            assertEquals("Product not available in requested quantity", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should build cart item with builder pattern")
        void builder_ValidData_CreatesCartItem() {
            // When
            CartItem built = CartItem.builder()
                .cartItemId(2L)
                .cart(testCart)
                .product(testProduct)
                .quantity(3)
                .build();

            // Then
            assertNotNull(built);
            assertEquals(2L, built.getCartItemId());
            assertEquals(testCart, built.getCart());
            assertEquals(testProduct, built.getProduct());
            assertEquals(3, built.getQuantity());
        }

        @Test
        @DisplayName("Should create cart item with no-args constructor")
        void noArgsConstructor_CreatesCartItem() {
            // When
            CartItem created = new CartItem();

            // Then
            assertNotNull(created);
            assertNull(created.getCartItemId());
            assertNull(created.getCart());
            assertNull(created.getProduct());
            assertNull(created.getQuantity());
        }

        @Test
        @DisplayName("Should create cart item with all-args constructor")
        void allArgsConstructor_ValidData_CreatesCartItem() {
            // When
            CartItem created = new CartItem(
                3L, testCart, testProduct, 4, 
                null, null // createdAt, updatedAt
            );

            // Then
            assertNotNull(created);
            assertEquals(3L, created.getCartItemId());
            assertEquals(testCart, created.getCart());
            assertEquals(testProduct, created.getProduct());
            assertEquals(4, created.getQuantity());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle large quantities")
        void getTotalPrice_LargeQuantity_HandlesCorrectly() {
            // Given
            cartItem.setQuantity(1000);

            // When
            long totalPrice = cartItem.getTotalPrice();

            // Then
            assertEquals(50000000L, totalPrice); // 1000 * 50,000 = 50,000,000
        }

        @Test
        @DisplayName("Should handle zero product price")
        void getTotalPrice_ZeroPrice_ReturnsZero() {
            // Given
            testProduct.setPrice(0);

            // When
            long totalPrice = cartItem.getTotalPrice();

            // Then
            assertEquals(0L, totalPrice);
        }

        @Test
        @DisplayName("Should handle null product price")
        void getTotalPrice_NullPrice_ReturnsZero() {
            // Given
            testProduct.setPrice(null);

            // When
            long totalPrice = cartItem.getTotalPrice();

            // Then
            assertEquals(0L, totalPrice);
        }

        @Test
        @DisplayName("Should handle product with zero weight")
        void getTotalWeight_ZeroWeight_ReturnsZero() {
            // Given
            testProduct.setWeight(0.0f);

            // When
            float totalWeight = cartItem.getTotalWeight();

            // Then
            assertEquals(0.0f, totalWeight);
        }

        @Test
        @DisplayName("Should handle null quantity in weight calculation")
        void getTotalWeight_NullQuantity_ReturnsZero() {
            // Given
            cartItem.setQuantity(null);

            // When
            float totalWeight = cartItem.getTotalWeight();

            // Then
            assertEquals(0.0f, totalWeight);
        }
    }
}