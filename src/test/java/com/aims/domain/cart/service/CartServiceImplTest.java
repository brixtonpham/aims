package com.aims.domain.cart.service;

import com.aims.domain.cart.entity.Cart;
import com.aims.domain.cart.entity.CartItem;
import com.aims.domain.cart.repository.CartRepository;
import com.aims.domain.product.entity.Product;
import com.aims.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CartServiceImpl
 * Validates domain service business logic without external dependencies
 */
@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart testCart;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        // Create test product
        testProduct = Product.builder()
            .productId(1L)
            .title("Test Product")
            .price(100)
            .quantity(10)
            .build();

        // Create test cart
        testCart = Cart.builder()
            .cartId(1L)
            .customerId("customer123")
            .build();
    }

    @Test
    void addProductToCart_NewProduct_ShouldAddSuccessfully() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        Cart result = cartService.addProductToCart(testCart, 1L, 2);

        // Assert
        assertNotNull(result);
        verify(productRepository).findById(1L);
        verify(cartRepository).save(testCart);
    }

    @Test
    void addProductToCart_InvalidProductId_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> cartService.addProductToCart(testCart, null, 2)
        );
        
        assertEquals("Invalid product ID: null", exception.getMessage());
    }

    @Test
    void addProductToCart_ProductNotFound_ShouldThrowException() {
        // Arrange
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> cartService.addProductToCart(testCart, 99L, 2)
        );
        
        assertEquals("Product not found: 99", exception.getMessage());
    }

    @Test
    void calculateCartTotal_EmptyCart_ShouldReturnZero() {
        // Arrange
        Cart emptyCart = Cart.builder()
            .cartId(1L)
            .customerId("customer123")
            .build();

        // Act
        Integer total = cartService.calculateCartTotal(emptyCart);

        // Assert
        assertEquals(0, total);
    }

    @Test
    void clearCart_ShouldClearAllItems() {
        // Arrange
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        cartService.clearCart(testCart);

        // Assert
        verify(cartRepository).save(testCart);
    }

    @Test
    void updateCartItemQuantity_ZeroQuantity_ShouldRemoveItem() {
        // Arrange
        CartItem cartItem = CartItem.builder()
            .cartItemId(1L)
            .product(testProduct)
            .quantity(2)
            .build();
        
        testCart.getCartItems().add(cartItem);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        Cart result = cartService.updateCartItemQuantity(testCart, 1L, 0);

        // Assert
        assertNotNull(result);
        verify(cartRepository).save(testCart);
    }

    @Test
    void updateCartItemQuantity_NegativeQuantity_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> cartService.updateCartItemQuantity(testCart, 1L, -1)
        );
        
        assertEquals("Quantity cannot be negative", exception.getMessage());
    }
}
