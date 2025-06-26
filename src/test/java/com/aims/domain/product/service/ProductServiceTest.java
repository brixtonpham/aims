package com.aims.domain.product.service;

import com.aims.domain.product.entity.Product;
import com.aims.domain.product.entity.Book;
import com.aims.domain.product.entity.CD;
import com.aims.domain.product.entity.DVD;
import com.aims.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductService
 * Tests the domain business logic without external dependencies
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private Book testBook;
    private CD testCD;
    private DVD testDVD;

    @BeforeEach
    void setUp() {
        // Setup basic product
        testProduct = Product.builder()
            .productId(1L)
            .title("Test Product")
            .price(50000)
            .quantity(100)
            .weight(0.5f)
            .rushOrderSupported(false)
            .imageUrl("test-image.jpg")
            .introduction("Test introduction")
            .build();

        // Setup book
        testBook = Book.builder()
            .productId(2L)
            .title("Clean Code")
            .price(45000)
            .quantity(50)
            .weight(0.6f)
            .authors("Robert C. Martin")
            .genre("Programming")
            .pageCount(464)
            .publishers("Prentice Hall")
            .coverType("Paperback")
            .publicationDate(LocalDate.of(2008, 8, 1))
            .build();

        // Setup CD
        testCD = CD.builder()
            .productId(3L)
            .title("Greatest Hits")
            .price(25000)
            .quantity(30)
            .artist("Test Artist")
            .genre("Rock")
            .recordLabel("Test Records")
            .releaseDate("2023")
            .trackCount(12)
            .build();

        // Setup DVD
        testDVD = DVD.builder()
            .productId(4L)
            .title("Test Movie")
            .price(35000)
            .quantity(20)
            .director("Test Director")
            .genre("Action")
            .studio("Test Studios")
            .runtimeMinutes(120)
            .subtitleLanguages("English, Vietnamese")
            .releaseDate("2023")
            .build();
    }

    @Test
    @DisplayName("Should successfully create a new product")
    void createProduct_ValidProduct_ShouldReturnSavedProduct() {
        // Given
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        Product result = productService.createProduct(testProduct);

        // Then
        assertNotNull(result);
        assertEquals(testProduct.getTitle(), result.getTitle());
        assertEquals(testProduct.getPrice(), result.getPrice());
        verify(productRepository).save(testProduct);
    }

    @Test
    @DisplayName("Should throw exception when creating product with null input")
    void createProduct_NullProduct_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> productService.createProduct(null)
        );

        assertEquals("Product cannot be null", exception.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when creating invalid product")
    void createProduct_InvalidProduct_ShouldThrowException() {
        // Given - Product without required fields
        Product invalidProduct = Product.builder()
            .price(-100) // Invalid negative price
            .quantity(-1) // Invalid negative quantity
            .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> productService.createProduct(invalidProduct)
        );

        assertEquals("Product validation failed", exception.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully update existing product")
    void updateProduct_ValidProduct_ShouldReturnUpdatedProduct() {
        // Given
        Long productId = 1L;
        Product updatedProduct = Product.builder()
            .title("Updated Product")
            .price(60000)
            .quantity(150)
            .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(productRepository.update(any(Product.class))).thenReturn(updatedProduct);

        // When
        Product result = productService.updateProduct(productId, updatedProduct);

        // Then
        assertNotNull(result);
        assertEquals(updatedProduct.getTitle(), result.getTitle());
        assertEquals(updatedProduct.getPrice(), result.getPrice());
        verify(productRepository).findById(productId);
        verify(productRepository).update(updatedProduct);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent product")
    void updateProduct_ProductNotFound_ShouldThrowException() {
        // Given
        Long productId = 999L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> productService.updateProduct(productId, testProduct)
        );

        assertEquals("Product not found with ID: 999", exception.getMessage());
        verify(productRepository).findById(productId);
        verify(productRepository, never()).update(any());
    }

    @Test
    @DisplayName("Should successfully get product by ID")
    void getProductById_ExistingProduct_ShouldReturnProduct() {
        // Given
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // When
        Optional<Product> result = productService.getProductById(productId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testProduct, result.get());
        verify(productRepository).findById(productId);
    }

    @Test
    @DisplayName("Should return empty when product not found")
    void getProductById_NonExistentProduct_ShouldReturnEmpty() {
        // Given
        Long productId = 999L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When
        Optional<Product> result = productService.getProductById(productId);

        // Then
        assertTrue(result.isEmpty());
        verify(productRepository).findById(productId);
    }

    @Test
    @DisplayName("Should successfully get all products")
    void getAllProducts_ShouldReturnAllProducts() {
        // Given
        List<Product> products = Arrays.asList(testProduct, testBook, testCD, testDVD);
        when(productRepository.findAll()).thenReturn(products);

        // When
        List<Product> result = productService.getAllProducts();

        // Then
        assertNotNull(result);
        assertEquals(4, result.size());
        assertTrue(result.contains(testProduct));
        assertTrue(result.contains(testBook));
        verify(productRepository).findAll();
    }

    @Test
    @DisplayName("Should successfully search products by title")
    void searchProductsByTitle_ValidTitle_ShouldReturnMatchingProducts() {
        // Given
        String searchTitle = "Test";
        List<Product> expectedProducts = Arrays.asList(testProduct, testCD, testDVD);
        when(productRepository.findByTitleContaining(searchTitle)).thenReturn(expectedProducts);

        // When
        List<Product> result = productService.searchProductsByTitle(searchTitle);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(productRepository).findByTitleContaining(searchTitle);
    }

    @Test
    @DisplayName("Should throw exception when searching with empty title")
    void searchProductsByTitle_EmptyTitle_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> productService.searchProductsByTitle("")
        );

        assertEquals("Search title cannot be empty", exception.getMessage());
        verify(productRepository, never()).findByTitleContaining(any());
    }

    @Test
    @DisplayName("Should successfully get products by type")
    void getProductsByType_ValidType_ShouldReturnProductsOfType() {
        // Given
        String productType = "book";
        List<Product> expectedBooks = Arrays.asList(testBook);
        when(productRepository.findByType(productType)).thenReturn(expectedBooks);

        // When
        List<Product> result = productService.getProductsByType(productType);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testBook, result.get(0));
        verify(productRepository).findByType(productType);
    }

    @Test
    @DisplayName("Should throw exception for invalid product type")
    void getProductsByType_InvalidType_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> productService.getProductsByType("invalid_type")
        );

        assertEquals("Invalid product type: invalid_type", exception.getMessage());
        verify(productRepository, never()).findByType(any());
    }

    @Test
    @DisplayName("Should check product availability correctly")
    void checkProductAvailability_SufficientStock_ShouldReturnTrue() {
        // Given
        Long productId = 1L;
        int requestedQuantity = 5;
        when(productRepository.isAvailable(productId, requestedQuantity)).thenReturn(true);

        // When
        boolean result = productService.checkProductAvailability(productId, requestedQuantity);

        // Then
        assertTrue(result);
        verify(productRepository).isAvailable(productId, requestedQuantity);
    }

    @Test
    @DisplayName("Should return false when insufficient stock")
    void checkProductAvailability_InsufficientStock_ShouldReturnFalse() {
        // Given
        Long productId = 1L;
        int requestedQuantity = 200;
        when(productRepository.isAvailable(productId, requestedQuantity)).thenReturn(false);

        // When
        boolean result = productService.checkProductAvailability(productId, requestedQuantity);

        // Then
        assertFalse(result);
        verify(productRepository).isAvailable(productId, requestedQuantity);
    }

    @Test
    @DisplayName("Should throw exception for invalid quantity in availability check")
    void checkProductAvailability_InvalidQuantity_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> productService.checkProductAvailability(1L, -1)
        );

        assertEquals("Quantity must be positive", exception.getMessage());
        verify(productRepository, never()).isAvailable(any(), anyInt());
    }

    @Test
    @DisplayName("Should successfully reserve product quantity")
    void reserveProductQuantity_SufficientStock_ShouldReserveAndReturnTrue() {
        // Given
        Long productId = 1L;
        int quantity = 10;
        Product productWithStock = Product.builder()
            .productId(productId)
            .title("Test Product")
            .price(50000)
            .quantity(100)
            .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(productWithStock));
        when(productRepository.update(any(Product.class))).thenReturn(productWithStock);

        // When
        boolean result = productService.reserveProductQuantity(productId, quantity);

        // Then
        assertTrue(result);
        verify(productRepository).findById(productId);
        verify(productRepository).update(productWithStock);
    }

    @Test
    @DisplayName("Should fail to reserve when insufficient stock")
    void reserveProductQuantity_InsufficientStock_ShouldReturnFalse() {
        // Given
        Long productId = 1L;
        int quantity = 200; // More than available
        Product productWithLowStock = Product.builder()
            .productId(productId)
            .title("Test Product")
            .price(50000)
            .quantity(50) // Less than requested
            .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(productWithLowStock));

        // When
        boolean result = productService.reserveProductQuantity(productId, quantity);

        // Then
        assertFalse(result);
        verify(productRepository).findById(productId);
        verify(productRepository, never()).update(any());
    }

    @Test
    @DisplayName("Should successfully release reserved quantity")
    void releaseProductQuantity_ValidRelease_ShouldIncreaseQuantity() {
        // Given
        Long productId = 1L;
        int quantityToRelease = 10;
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(productRepository.update(any(Product.class))).thenReturn(testProduct);

        // When
        productService.releaseProductQuantity(productId, quantityToRelease);

        // Then
        verify(productRepository).findById(productId);
        verify(productRepository).update(testProduct);
    }

    @Test
    @DisplayName("Should successfully get low stock products")
    void getLowStockProducts_ValidThreshold_ShouldReturnLowStockProducts() {
        // Given
        int threshold = 10;
        List<Product> lowStockProducts = Arrays.asList(testCD, testDVD);
        when(productRepository.findLowStockProducts(threshold)).thenReturn(lowStockProducts);

        // When
        List<Product> result = productService.getLowStockProducts(threshold);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository).findLowStockProducts(threshold);
    }

    @Test
    @DisplayName("Should throw exception for negative threshold")
    void getLowStockProducts_NegativeThreshold_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> productService.getLowStockProducts(-1)
        );

        assertEquals("Threshold must be non-negative", exception.getMessage());
        verify(productRepository, never()).findLowStockProducts(anyInt());
    }

    @Test
    @DisplayName("Should successfully delete existing product")
    void deleteProduct_ExistingProduct_ShouldDeleteSuccessfully() {
        // Given
        Long productId = 1L;
        when(productRepository.existsById(productId)).thenReturn(true);

        // When
        productService.deleteProduct(productId);

        // Then
        verify(productRepository).existsById(productId);
        verify(productRepository).deleteById(productId);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent product")
    void deleteProduct_NonExistentProduct_ShouldThrowException() {
        // Given
        Long productId = 999L;
        when(productRepository.existsById(productId)).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> productService.deleteProduct(productId)
        );

        assertEquals("Product not found with ID: 999", exception.getMessage());
        verify(productRepository).existsById(productId);
        verify(productRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should handle Book-specific business logic")
    void createProduct_Book_ShouldHandleBookSpecificLogic() {
        // Given
        when(productRepository.save(any(Book.class))).thenReturn(testBook);

        // When
        Product result = productService.createProduct(testBook);

        // Then
        assertNotNull(result);
        assertInstanceOf(Book.class, result);
        Book bookResult = (Book) result;
        assertEquals("Robert C. Martin", bookResult.getAuthors());
        assertEquals("Programming", bookResult.getGenre());
        verify(productRepository).save(testBook);
    }

    @Test
    @DisplayName("Should handle CD-specific business logic")
    void createProduct_CD_ShouldHandleCDSpecificLogic() {
        // Given
        when(productRepository.save(any(CD.class))).thenReturn(testCD);

        // When
        Product result = productService.createProduct(testCD);

        // Then
        assertNotNull(result);
        assertInstanceOf(CD.class, result);
        CD cdResult = (CD) result;
        assertEquals("Test Artist", cdResult.getArtist());
        assertEquals("Rock", cdResult.getGenre());
        verify(productRepository).save(testCD);
    }

    @Test
    @DisplayName("Should handle DVD-specific business logic")
    void createProduct_DVD_ShouldHandleDVDSpecificLogic() {
        // Given
        when(productRepository.save(any(DVD.class))).thenReturn(testDVD);

        // When
        Product result = productService.createProduct(testDVD);

        // Then
        assertNotNull(result);
        assertInstanceOf(DVD.class, result);
        DVD dvdResult = (DVD) result;
        assertEquals("Test Director", dvdResult.getDirector());
        assertEquals("Action", dvdResult.getGenre());
        verify(productRepository).save(testDVD);
    }
}