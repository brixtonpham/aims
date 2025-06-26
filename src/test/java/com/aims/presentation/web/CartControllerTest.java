package com.aims.presentation.web;

import com.aims.application.commands.AddProductToCartCommand;
import com.aims.application.commands.UpdateCartCommand;
import com.aims.application.commands.ViewCartQuery;
import com.aims.application.services.CartApplicationService;
import com.aims.domain.cart.entity.Cart;
import com.aims.domain.cart.entity.CartItem;
import com.aims.domain.product.entity.Product;
import com.aims.presentation.dto.CartRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for CartController
 * Tests REST endpoints for cart operations without external dependencies
 * 
 * Required test dependencies:
 * - spring-boot-starter-test (includes MockMvc, Mockito, JUnit 5)
 * - jackson-databind (for JSON serialization)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CartController Unit Tests")
class CartControllerTest {

    @Mock
    private CartApplicationService cartApplicationService;

    @InjectMocks
    private CartController cartController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private CartRequest validCartRequest;
    private Cart testCart;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cartController).build();
        objectMapper = new ObjectMapper();

        // Setup test data
        validCartRequest = new CartRequest();
        validCartRequest.setCustomerId(1L);
        validCartRequest.setProductId(1L);
        validCartRequest.setQuantity(2);

        testProduct = Product.builder()
            .productId(1L)
            .title("Test Product")
            .price(10000)
            .quantity(50)
            .type("book")
            .imageUrl("test.jpg")
            .build();

        CartItem cartItem = CartItem.builder()
            .cartItemId(1L)
            .product(testProduct)
            .quantity(2)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        testCart = Cart.builder()
            .cartId(1L)
            .customerId("1")
            .cartItems(new ArrayList<>())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        testCart.getCartItems().add(cartItem);
        cartItem.setCart(testCart);
    }

    @Test
    @DisplayName("Should successfully add product to cart")
    void addProductToCart_ValidRequest_ReturnsSuccess() throws Exception {
        // Given
        when(cartApplicationService.addProductToCart(any(AddProductToCartCommand.class)))
            .thenReturn(testCart);

        // When & Then
        mockMvc.perform(post("/api/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCartRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Product added to cart successfully"))
                .andExpect(jsonPath("$.data.cartId").value(1L))
                .andExpect(jsonPath("$.data.customerId").value(1L))
                .andExpect(jsonPath("$.data.totalItems").value(1));

        verify(cartApplicationService).addProductToCart(any(AddProductToCartCommand.class));
    }

    @Test
    @DisplayName("Should return validation error for invalid customer ID")
    void addProductToCart_InvalidCustomerId_ReturnsValidationError() throws Exception {
        // Given
        validCartRequest.setCustomerId(null);

        // When & Then
        mockMvc.perform(post("/api/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCartRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Valid customer ID is required"));

        verify(cartApplicationService, never()).addProductToCart(any());
    }

    @Test
    @DisplayName("Should return validation error for invalid product ID")
    void addProductToCart_InvalidProductId_ReturnsValidationError() throws Exception {
        // Given
        validCartRequest.setProductId(0L);

        // When & Then
        mockMvc.perform(post("/api/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCartRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Valid product ID is required"));

        verify(cartApplicationService, never()).addProductToCart(any());
    }

    @Test
    @DisplayName("Should return validation error for invalid quantity")
    void addProductToCart_InvalidQuantity_ReturnsValidationError() throws Exception {
        // Given
        validCartRequest.setQuantity(0);

        // When & Then
        mockMvc.perform(post("/api/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCartRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Valid quantity is required"));

        verify(cartApplicationService, never()).addProductToCart(any());
    }

    @Test
    @DisplayName("Should handle service exception when adding product to cart")
    void addProductToCart_ServiceException_ReturnsInternalError() throws Exception {
        // Given
        when(cartApplicationService.addProductToCart(any(AddProductToCartCommand.class)))
            .thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(post("/api/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCartRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("Failed to add product to cart"));

        verify(cartApplicationService).addProductToCart(any(AddProductToCartCommand.class));
    }

    @Test
    @DisplayName("Should successfully view cart for existing customer")
    void viewCart_ExistingCustomer_ReturnsCart() throws Exception {
        // Given
        when(cartApplicationService.viewCart(any(ViewCartQuery.class)))
            .thenReturn(Optional.of(testCart));

        // When & Then
        mockMvc.perform(get("/api/cart/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.cartId").value(1L))
                .andExpect(jsonPath("$.data.customerId").value(1L))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items").isNotEmpty());

        verify(cartApplicationService).viewCart(any(ViewCartQuery.class));
    }

    @Test
    @DisplayName("Should return empty cart for non-existent customer")
    void viewCart_NonExistentCustomer_ReturnsEmptyCart() throws Exception {
        // Given
        when(cartApplicationService.viewCart(any(ViewCartQuery.class)))
            .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/cart/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.customerId").value(999L))
                .andExpect(jsonPath("$.data.totalAmount").value(0))
                .andExpect(jsonPath("$.data.totalItems").value(0));

        verify(cartApplicationService).viewCart(any(ViewCartQuery.class));
    }

    @Test
    @DisplayName("Should return validation error for invalid customer ID in view cart")
    void viewCart_InvalidCustomerId_ReturnsValidationError() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/cart/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Invalid customer ID"));

        verify(cartApplicationService, never()).viewCart(any());
    }

    @Test
    @DisplayName("Should successfully update cart item quantity")
    void updateCartItem_ValidRequest_ReturnsSuccess() throws Exception {
        // Given
        CartRequest updateRequest = new CartRequest();
        updateRequest.setQuantity(5);
        
        when(cartApplicationService.updateCartItem(any(UpdateCartCommand.class)))
            .thenReturn(testCart);

        // When & Then
        mockMvc.perform(put("/api/cart/items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cart item updated successfully"))
                .andExpect(jsonPath("$.data.cartId").value(1L));

        verify(cartApplicationService).updateCartItem(any(UpdateCartCommand.class));
    }

    @Test
    @DisplayName("Should return validation error for invalid cart item ID")
    void updateCartItem_InvalidItemId_ReturnsValidationError() throws Exception {
        // Given
        CartRequest updateRequest = new CartRequest();
        updateRequest.setQuantity(5);

        // When & Then
        mockMvc.perform(put("/api/cart/items/0")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Invalid cart item ID"));

        verify(cartApplicationService, never()).updateCartItem(any());
    }

    @Test
    @DisplayName("Should return validation error for negative quantity")
    void updateCartItem_NegativeQuantity_ReturnsValidationError() throws Exception {
        // Given
        CartRequest updateRequest = new CartRequest();
        updateRequest.setQuantity(-1);

        // When & Then
        mockMvc.perform(put("/api/cart/items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Valid quantity is required"));

        verify(cartApplicationService, never()).updateCartItem(any());
    }

    @Test
    @DisplayName("Should handle UnsupportedOperationException in update cart item")
    void updateCartItem_UnsupportedOperation_ReturnsNotImplemented() throws Exception {
        // Given
        CartRequest updateRequest = new CartRequest();
        updateRequest.setQuantity(5);
        
        when(cartApplicationService.updateCartItem(any(UpdateCartCommand.class)))
            .thenThrow(new UnsupportedOperationException("Operation not implemented"));

        // When & Then
        mockMvc.perform(put("/api/cart/items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotImplemented())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("NOT_IMPLEMENTED"))
                .andExpect(jsonPath("$.message").value("Operation not implemented"));

        verify(cartApplicationService).updateCartItem(any(UpdateCartCommand.class));
    }

    @Test
    @DisplayName("Should return not implemented for remove cart item")
    void removeCartItem_AnyRequest_ReturnsNotImplemented() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/cart/items/1"))
                .andExpect(status().isNotImplemented())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("NOT_IMPLEMENTED"))
                .andExpect(jsonPath("$.message").value("Remove cart item not implemented"));

        // No service method is called since this endpoint is not implemented
        verifyNoInteractions(cartApplicationService);
    }

    @Test
    @DisplayName("Should successfully clear cart")
    void clearCart_ValidCustomerId_ReturnsSuccess() throws Exception {
        // Given
        doNothing().when(cartApplicationService).clearCart("1");

        // When & Then
        mockMvc.perform(delete("/api/cart/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cart cleared successfully"));

        verify(cartApplicationService).clearCart("1");
    }

    @Test
    @DisplayName("Should return validation error for invalid customer ID in clear cart")
    void clearCart_InvalidCustomerId_ReturnsValidationError() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/cart/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Invalid customer ID"));

        verify(cartApplicationService, never()).clearCart(any());
    }

    @Test
    @DisplayName("Should handle service exception when clearing cart")
    void clearCart_ServiceException_ReturnsInternalError() throws Exception {
        // Given
        doThrow(new RuntimeException("Service error"))
            .when(cartApplicationService).clearCart("1");

        // When & Then
        mockMvc.perform(delete("/api/cart/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("Failed to clear cart"));

        verify(cartApplicationService).clearCart("1");
    }

    @Test
    @DisplayName("Should properly map cart to response DTO")
    void mapToCartResponse_ValidCart_ReturnsCorrectMapping() throws Exception {
        // Given
        when(cartApplicationService.addProductToCart(any(AddProductToCartCommand.class)))
            .thenReturn(testCart);

        // When & Then
        mockMvc.perform(post("/api/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCartRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cartId").value(testCart.getCartId()))
                .andExpect(jsonPath("$.data.customerId").value(Long.parseLong(testCart.getCustomerId())))
                .andExpect(jsonPath("$.data.items[0].cartItemId").value(1L))
                .andExpect(jsonPath("$.data.items[0].productId").value(1L))
                .andExpect(jsonPath("$.data.items[0].productTitle").value("Test Product"))
                .andExpect(jsonPath("$.data.items[0].productType").value("book"))
                .andExpect(jsonPath("$.data.items[0].unitPrice").value(10000))
                .andExpect(jsonPath("$.data.items[0].quantity").value(2))
                .andExpect(jsonPath("$.data.items[0].totalPrice").value(20000))
                .andExpect(jsonPath("$.data.items[0].imageUrl").value("test.jpg"))
                .andExpect(jsonPath("$.data.items[0].available").value(true));
    }
}