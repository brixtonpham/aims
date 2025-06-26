package com.aims.application.services;

import com.aims.application.commands.AddProductCommand;
import com.aims.application.commands.UpdateProductCommand;
import com.aims.application.commands.SearchProductQuery;
import com.aims.application.exceptions.ProductApplicationException;
import com.aims.domain.product.entity.Product;
import com.aims.domain.product.service.ProductService;
import com.aims.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductApplicationService Unit Tests")
class ProductApplicationServiceTest {
    
    @Mock
    private ProductService productService;
    
    @Mock
    private ProductRepository productRepository;
    
    @InjectMocks
    private ProductApplicationService productApplicationService;
    
    private Product testProduct;
    private AddProductCommand addProductCommand;
    private UpdateProductCommand updateProductCommand;
    
    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
            .productId(1L)
            .title("Test Product")
            .price(10000)
            .quantity(100)
            .rushOrderSupported(false)
            .build();
        
        addProductCommand = AddProductCommand.builder()
            .product(testProduct)
            .makeAvailableImmediately(true)
            .createdBy("test-user")
            .source("TEST")
            .build();
        
        updateProductCommand = UpdateProductCommand.builder()
            .productId(1L)
            .product(testProduct)
            .updatedBy("test-user")
            .source("TEST")
            .notifyInventoryChanges(false)
            .build();
    }
    
    @Test
    @DisplayName("Should successfully add product")
    void addProduct_ValidCommand_ReturnsProductId() {
        // Given
        when(productService.createProduct(any(Product.class))).thenReturn(testProduct);
        
        // When
        Long result = productApplicationService.addProduct(addProductCommand);
        
        // Then
        assertEquals(1L, result);
        verify(productService).createProduct(testProduct);
    }
    
    @Test
    @DisplayName("Should throw exception when add product command is null")
    void addProduct_NullCommand_ThrowsException() {
        // When & Then
        assertThrows(ProductApplicationException.class, 
                    () -> productApplicationService.addProduct(null));
        
        verify(productService, never()).createProduct(any());
    }
    
    @Test
    @DisplayName("Should throw exception when add product command is invalid")
    void addProduct_InvalidCommand_ThrowsException() {
        // Given
        addProductCommand.setProduct(null); // Make command invalid
        
        // When & Then
        assertThrows(ProductApplicationException.class, 
                    () -> productApplicationService.addProduct(addProductCommand));
        
        verify(productService, never()).createProduct(any());
    }
    
    @Test
    @DisplayName("Should successfully update product")
    void updateProduct_ValidCommand_ReturnsUpdatedProduct() {
        // Given
        when(productRepository.existsById(1L)).thenReturn(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productService.updateProduct(1L, testProduct)).thenReturn(testProduct);
        
        // When
        Product result = productApplicationService.updateProduct(updateProductCommand);
        
        // Then
        assertEquals(testProduct, result);
        verify(productService).updateProduct(1L, testProduct);
    }
    
    @Test
    @DisplayName("Should throw exception when updating non-existent product")
    void updateProduct_NonExistentProduct_ThrowsException() {
        // Given
        when(productRepository.existsById(1L)).thenReturn(false);
        
        // When & Then
        assertThrows(ProductApplicationException.class, 
                    () -> productApplicationService.updateProduct(updateProductCommand));
        
        verify(productService, never()).updateProduct(anyLong(), any());
    }
    
    @Test
    @DisplayName("Should successfully view product")
    void viewProduct_ValidId_ReturnsProduct() {
        // Given
        when(productService.getProductById(1L)).thenReturn(Optional.of(testProduct));
        
        // When
        Optional<Product> result = productApplicationService.viewProduct(1L);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(testProduct, result.get());
        verify(productService).getProductById(1L);
    }
    
    @Test
    @DisplayName("Should return empty when product not found")
    void viewProduct_NonExistentId_ReturnsEmpty() {
        // Given
        when(productService.getProductById(999L)).thenReturn(Optional.empty());
        
        // When
        Optional<Product> result = productApplicationService.viewProduct(999L);
        
        // Then
        assertTrue(result.isEmpty());
        verify(productService).getProductById(999L);
    }
    
    @Test
    @DisplayName("Should throw exception when viewing product with invalid ID")
    void viewProduct_InvalidId_ThrowsException() {
        // When & Then
        assertThrows(ProductApplicationException.class, 
                    () -> productApplicationService.viewProduct(null));
        
        assertThrows(ProductApplicationException.class, 
                    () -> productApplicationService.viewProduct(0L));
        
        verify(productService, never()).getProductById(any());
    }
    
    @Test
    @DisplayName("Should successfully search products")
    void searchProducts_ValidQuery_ReturnsProducts() {
        // Given
        SearchProductQuery query = SearchProductQuery.builder()
            .title("Test")
            .build();
        
        List<Product> expectedProducts = Arrays.asList(testProduct);
        when(productService.searchProductsByTitle("Test")).thenReturn(expectedProducts);
        
        // When
        List<Product> result = productApplicationService.searchProducts(query);
        
        // Then
        assertEquals(1, result.size());
        assertEquals(testProduct, result.get(0));
        verify(productService).searchProductsByTitle("Test");
    }
    
    @Test
    @DisplayName("Should search by type when title is not specified")
    void searchProducts_TypeQuery_ReturnsProducts() {
        // Given
        SearchProductQuery query = SearchProductQuery.builder()
            .type("book")
            .build();
        
        List<Product> expectedProducts = Arrays.asList(testProduct);
        when(productService.getProductsByType("book")).thenReturn(expectedProducts);
        
        // When
        List<Product> result = productApplicationService.searchProducts(query);
        
        // Then
        assertEquals(1, result.size());
        verify(productService).getProductsByType("book");
    }
    
    @Test
    @DisplayName("Should get all products when no search criteria")
    void searchProducts_NoSearchCriteria_ReturnsAllProducts() {
        // Given
        SearchProductQuery query = SearchProductQuery.builder().build();
        
        List<Product> expectedProducts = Arrays.asList(testProduct);
        when(productService.getAllProducts()).thenReturn(expectedProducts);
        
        // When
        List<Product> result = productApplicationService.searchProducts(query);
        
        // Then
        assertEquals(1, result.size());
        verify(productService).getAllProducts();
    }
    
    @Test
    @DisplayName("Should successfully get all products")
    void getAllProducts_ValidCall_ReturnsProducts() {
        // Given
        List<Product> expectedProducts = Arrays.asList(testProduct);
        when(productService.getAllProducts()).thenReturn(expectedProducts);
        
        // When
        List<Product> result = productApplicationService.getAllProducts();
        
        // Then
        assertEquals(1, result.size());
        assertEquals(testProduct, result.get(0));
        verify(productService).getAllProducts();
    }
    
    @Test
    @DisplayName("Should successfully check product availability")
    void checkProductAvailability_ValidRequest_ReturnsTrue() {
        // Given
        when(productService.checkProductAvailability(1L, 5)).thenReturn(true);
        
        // When
        boolean result = productApplicationService.checkProductAvailability(1L, 5);
        
        // Then
        assertTrue(result);
        verify(productService).checkProductAvailability(1L, 5);
    }
    
    @Test
    @DisplayName("Should return false for invalid availability parameters")
    void checkProductAvailability_InvalidParameters_ReturnsFalse() {
        // When & Then
        assertFalse(productApplicationService.checkProductAvailability(null, 5));
        assertFalse(productApplicationService.checkProductAvailability(1L, 0));
        assertFalse(productApplicationService.checkProductAvailability(1L, -1));
        
        verify(productService, never()).checkProductAvailability(any(), anyInt());
    }
    
    @Test
    @DisplayName("Should handle service exceptions gracefully")
    void addProduct_ServiceException_ThrowsApplicationException() {
        // Given
        when(productService.createProduct(any(Product.class)))
            .thenThrow(new RuntimeException("Database error"));
        
        // When & Then
        ProductApplicationException exception = assertThrows(ProductApplicationException.class, 
                    () -> productApplicationService.addProduct(addProductCommand));
        
        assertTrue(exception.getMessage().contains("Failed to add product"));
        assertTrue(exception.getCause() instanceof RuntimeException);
    }
}
