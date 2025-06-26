package com.aims.domain.product.entity;

import com.aims.domain.product.service.ProductService;
import com.aims.domain.product.repository.ProductRepository;
import com.aims.application.services.ProductApplicationService;
import com.aims.application.commands.AddProductCommand;
import com.aims.application.commands.UpdateProductCommand;
import com.aims.application.commands.SearchProductQuery;
import com.aims.application.exceptions.ProductApplicationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for Product domain
 * Tests Product entities, ProductService, and ProductApplicationService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Product Domain Tests")
class ProductTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductService mockProductService;

    private ProductService productService;

    // Test data
    private Product baseProduct;
    private Book book;
    private CD cd;
    private DVD dvd;

    @BeforeEach
    void setUp() {
        // Initialize the service with the mocked repository
        productService = new ProductService(productRepository);
        
        // Setup base product
        baseProduct = Product.builder()
                .productId(1L)
                .title("Test Product")
                .price(100)
                .quantity(10)
                .weight(1.5f)
                .rushOrderSupported(true)
                .imageUrl("http://example.com/image.jpg")
                .barcode("1234567890")
                .introduction("Test introduction")
                .createdAt(LocalDateTime.now())
                .build();

        // Setup book
        book = Book.builder()
                .productId(2L)
                .title("Java Programming")
                .price(500)
                .quantity(20)
                .genre("Programming")
                .pageCount(350)
                .publicationDate(LocalDate.of(2023, 1, 1))
                .authors("John Doe")
                .publishers("Tech Books")
                .coverType("paperback")
                .build();

        // Setup CD
        cd = CD.builder()
                .productId(3L)
                .title("Greatest Hits")
                .price(200)
                .quantity(15)
                .genre("Rock")
                .artist("Test Artist")
                .recordLabel("Music Records")
                .releaseDate("2023-01-01")
                .trackCount(12)
                .build();

        // Setup DVD
        dvd = DVD.builder()
                .productId(4L)
                .title("Action Movie")
                .price(300)
                .quantity(8)
                .genre("Action")
                .director("Test Director")
                .studio("Film Studio")
                .releaseDate("2023-01-01")
                .runtimeMinutes(120)
                .subtitleLanguages("English, Spanish")
                .dubbingLanguages("English")
                .build();
    }

    @Nested
    @DisplayName("Product Entity Tests")
    class ProductEntityTests {

        @Test
        @DisplayName("Should create product with valid data")
        void shouldCreateProductWithValidData() {
            assertNotNull(baseProduct);
            assertEquals("Test Product", baseProduct.getTitle());
            assertEquals(100, baseProduct.getPrice());
            assertEquals(10, baseProduct.getQuantity());
            assertTrue(baseProduct.getRushOrderSupported());
        }

        @Test
        @DisplayName("Should validate product correctly")
        void shouldValidateProductCorrectly() {
            // Valid product
            assertTrue(baseProduct.isValid());

            // Invalid product - null title
            Product invalidProduct = Product.builder()
                    .title(null)
                    .price(100)
                    .quantity(10)
                    .build();
            assertFalse(invalidProduct.isValid());

            // Invalid product - negative price
            Product negativePrice = Product.builder()
                    .title("Test")
                    .price(-50)
                    .quantity(10)
                    .build();
            assertFalse(negativePrice.isValid());

            // Invalid product - negative quantity
            Product negativeQuantity = Product.builder()
                    .title("Test")
                    .price(100)
                    .quantity(-5)
                    .build();
            assertFalse(negativeQuantity.isValid());
        }

        @Test
        @DisplayName("Should check availability correctly")
        void shouldCheckAvailabilityCorrectly() {
            // Available quantity
            assertTrue(baseProduct.isAvailable(5));
            assertTrue(baseProduct.isAvailable(10));

            // Not available quantity
            assertFalse(baseProduct.isAvailable(15));
            assertFalse(baseProduct.isAvailable(0));
            assertFalse(baseProduct.isAvailable(-1));
        }

        @Test
        @DisplayName("Should reserve quantity correctly")
        void shouldReserveQuantityCorrectly() {
            // Successful reservation
            assertTrue(baseProduct.reserveQuantity(3));
            assertEquals(7, baseProduct.getQuantity());

            // Unsuccessful reservation - insufficient stock
            assertFalse(baseProduct.reserveQuantity(10));
            assertEquals(7, baseProduct.getQuantity()); // Should remain unchanged
        }

        @Test
        @DisplayName("Should release quantity correctly")
        void shouldReleaseQuantityCorrectly() {
            baseProduct.setQuantity(5);
            baseProduct.releaseQuantity(3);
            assertEquals(8, baseProduct.getQuantity());

            // Should handle zero release
            baseProduct.releaseQuantity(0);
            assertEquals(8, baseProduct.getQuantity());

            // Should ignore negative release
            baseProduct.releaseQuantity(-1);
            assertEquals(8, baseProduct.getQuantity());
        }

        @Test
        @DisplayName("Should check stock status correctly")
        void shouldCheckStockStatusCorrectly() {
            assertTrue(baseProduct.isInStock());

            baseProduct.setQuantity(0);
            assertFalse(baseProduct.isInStock());

            baseProduct.setQuantity(null);
            assertFalse(baseProduct.isInStock());
        }

        @Test
        @DisplayName("Should calculate total stock value correctly")
        void shouldCalculateTotalStockValueCorrectly() {
            assertEquals(1000L, baseProduct.getTotalStockValue()); // 10 * 100

            baseProduct.setQuantity(0);
            assertEquals(0L, baseProduct.getTotalStockValue());

            baseProduct.setQuantity(null);
            assertEquals(0L, baseProduct.getTotalStockValue());

            baseProduct.setPrice(null);
            assertEquals(0L, baseProduct.getTotalStockValue());
        }

        @Test
        @DisplayName("Should support rush order correctly")
        void shouldSupportRushOrderCorrectly() {
            assertTrue(baseProduct.supportsRushOrder());

            baseProduct.setRushOrderSupported(false);
            assertFalse(baseProduct.supportsRushOrder());

            baseProduct.setRushOrderSupported(null);
            assertFalse(baseProduct.supportsRushOrder());
        }

        @Test
        @DisplayName("Should get shipping weight correctly")
        void shouldGetShippingWeightCorrectly() {
            assertEquals(1.5f, baseProduct.getShippingWeight());

            baseProduct.setWeight(null);
            assertEquals(0.0f, baseProduct.getShippingWeight());
        }
    }

    @Nested
    @DisplayName("Book Entity Tests")
    class BookEntityTests {

        @Test
        @DisplayName("Should create book with specific fields")
        void shouldCreateBookWithSpecificFields() {
            assertEquals("book", book.getType());
            assertEquals("Programming", book.getGenre());
            assertEquals(350, book.getPageCount());
            assertEquals("John Doe", book.getAuthors());
            assertEquals("Tech Books", book.getPublishers());
        }

        @Test
        @DisplayName("Should calculate book shipping weight")
        void shouldCalculateBookShippingWeight() {
            // Test with page count
            float expectedWeight = 350 * 0.5f / 1000; // paperback calculation
            assertEquals(expectedWeight, book.getShippingWeight(), 0.001f);

            // Test hardcover
            book.setCoverType("hardcover");
            float hardcoverWeight = 350 * 1.0f / 1000;
            assertEquals(hardcoverWeight, book.getShippingWeight(), 0.001f);

            // Test without page count
            book.setPageCount(null);
            assertEquals(0.5f, book.getShippingWeight()); // default weight

            // Test with manual weight override
            book.setWeight(2.0f);
            assertEquals(2.0f, book.getShippingWeight());
        }

        @Test
        @DisplayName("Should check book availability")
        void shouldCheckBookAvailability() {
            assertTrue(book.isBookAvailable());

            // Test with future publication date
            book.setPublicationDate(LocalDate.now().plusDays(30));
            assertFalse(book.isBookAvailable());

            // Test with no stock
            book.setPublicationDate(LocalDate.now().minusDays(30));
            book.setQuantity(0);
            assertFalse(book.isBookAvailable());
        }

        @Test
        @DisplayName("Should identify new releases")
        void shouldIdentifyNewReleases() {
            // Recent publication
            book.setPublicationDate(LocalDate.now().minusMonths(3));
            assertTrue(book.isNewRelease());

            // Old publication
            book.setPublicationDate(LocalDate.now().minusYears(2));
            assertFalse(book.isNewRelease());

            // No publication date
            book.setPublicationDate(null);
            assertFalse(book.isNewRelease());
        }
    }

    @Nested
    @DisplayName("CD Entity Tests")
    class CDEntityTests {

        @Test
        @DisplayName("Should create CD with specific fields")
        void shouldCreateCDWithSpecificFields() {
            assertEquals("cd", cd.getType());
            assertEquals("Rock", cd.getGenre());
            assertEquals("Test Artist", cd.getArtist());
            assertEquals("Music Records", cd.getRecordLabel());
            assertEquals(12, cd.getTrackCount());
        }

        @Test
        @DisplayName("Should calculate CD shipping weight")
        void shouldCalculateCDShippingWeight() {
            assertEquals(0.15f, cd.getShippingWeight()); // Standard CD weight

            // Test with manual weight override
            cd.setWeight(0.2f);
            assertEquals(0.2f, cd.getShippingWeight());
        }
    }

    @Nested
    @DisplayName("DVD Entity Tests")
    class DVDEntityTests {

        @Test
        @DisplayName("Should create DVD with specific fields")
        void shouldCreateDVDWithSpecificFields() {
            assertEquals("dvd", dvd.getType());
            assertEquals("Action", dvd.getGenre());
            assertEquals("Test Director", dvd.getDirector());
            assertEquals("Film Studio", dvd.getStudio());
            assertEquals(120, dvd.getRuntimeMinutes());
        }

        @Test
        @DisplayName("Should calculate DVD shipping weight")
        void shouldCalculateDVDShippingWeight() {
            assertEquals(0.1f, dvd.getShippingWeight()); // Standard DVD weight

            // Test with manual weight override
            dvd.setWeight(0.15f);
            assertEquals(0.15f, dvd.getShippingWeight());
        }
    }

    @Nested
    @DisplayName("Product Service Tests")
    class ProductServiceTests {

        @Test
        @DisplayName("Should create product successfully")
        void shouldCreateProductSuccessfully() {
            when(productRepository.save(any(Product.class))).thenReturn(baseProduct);

            Product result = productService.createProduct(baseProduct);

            assertNotNull(result);
            assertEquals(baseProduct.getTitle(), result.getTitle());
            verify(productRepository).save(baseProduct);
        }

        @Test
        @DisplayName("Should throw exception when creating invalid product")
        void shouldThrowExceptionWhenCreatingInvalidProduct() {
            Product invalidProduct = Product.builder().build();

            assertThrows(IllegalArgumentException.class, 
                () -> productService.createProduct(invalidProduct));
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when creating null product")
        void shouldThrowExceptionWhenCreatingNullProduct() {
            assertThrows(IllegalArgumentException.class, 
                () -> productService.createProduct(null));
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should update product successfully")
        void shouldUpdateProductSuccessfully() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(baseProduct));
            when(productRepository.update(any(Product.class))).thenReturn(baseProduct);

            Product result = productService.updateProduct(1L, baseProduct);

            assertNotNull(result);
            verify(productRepository).findById(1L);
            verify(productRepository).update(baseProduct);
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent product")
        void shouldThrowExceptionWhenUpdatingNonExistentProduct() {
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, 
                () -> productService.updateProduct(999L, baseProduct));
            verify(productRepository).findById(999L);
            verify(productRepository, never()).update(any());
        }

        @Test
        @DisplayName("Should get product by ID successfully")
        void shouldGetProductByIdSuccessfully() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(baseProduct));

            Optional<Product> result = productService.getProductById(1L);

            assertTrue(result.isPresent());
            assertEquals(baseProduct, result.get());
            verify(productRepository).findById(1L);
        }

        @Test
        @DisplayName("Should return empty when product not found")
        void shouldReturnEmptyWhenProductNotFound() {
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<Product> result = productService.getProductById(999L);

            assertFalse(result.isPresent());
            verify(productRepository).findById(999L);
        }

        @Test
        @DisplayName("Should get all products")
        void shouldGetAllProducts() {
            List<Product> products = Arrays.asList(baseProduct, book, cd, dvd);
            when(productRepository.findAll()).thenReturn(products);

            List<Product> result = productService.getAllProducts();

            assertEquals(4, result.size());
            verify(productRepository).findAll();
        }

        @Test
        @DisplayName("Should search products by title")
        void shouldSearchProductsByTitle() {
            List<Product> products = Arrays.asList(baseProduct);
            when(productRepository.findByTitleContaining("Test")).thenReturn(products);

            List<Product> result = productService.searchProductsByTitle("Test");

            assertEquals(1, result.size());
            assertEquals("Test Product", result.get(0).getTitle());
            verify(productRepository).findByTitleContaining("Test");
        }

        @Test
        @DisplayName("Should throw exception when searching with empty title")
        void shouldThrowExceptionWhenSearchingWithEmptyTitle() {
            assertThrows(IllegalArgumentException.class, 
                () -> productService.searchProductsByTitle(""));
            assertThrows(IllegalArgumentException.class, 
                () -> productService.searchProductsByTitle(null));
            verify(productRepository, never()).findByTitleContaining(any());
        }

        @Test
        @DisplayName("Should get products by type")
        void shouldGetProductsByType() {
            List<Product> books = Arrays.asList(book);
            when(productRepository.findByType("book")).thenReturn(books);

            List<Product> result = productService.getProductsByType("book");

            assertEquals(1, result.size());
            verify(productRepository).findByType("book");
        }

        @Test
        @DisplayName("Should throw exception for invalid product type")
        void shouldThrowExceptionForInvalidProductType() {
            assertThrows(IllegalArgumentException.class, 
                () -> productService.getProductsByType("invalid"));
            verify(productRepository, never()).findByType(any());
        }

        @Test
        @DisplayName("Should check product availability")
        void shouldCheckProductAvailability() {
            when(productRepository.isAvailable(1L, 5)).thenReturn(true);
            when(productRepository.isAvailable(1L, 15)).thenReturn(false);

            assertTrue(productService.checkProductAvailability(1L, 5));
            assertFalse(productService.checkProductAvailability(1L, 15));

            verify(productRepository).isAvailable(1L, 5);
            verify(productRepository).isAvailable(1L, 15);
        }

        @Test
        @DisplayName("Should throw exception for invalid quantity check")
        void shouldThrowExceptionForInvalidQuantityCheck() {
            assertThrows(IllegalArgumentException.class, 
                () -> productService.checkProductAvailability(1L, 0));
            assertThrows(IllegalArgumentException.class, 
                () -> productService.checkProductAvailability(1L, -1));
        }

        @Test
        @DisplayName("Should reserve product quantity successfully")
        void shouldReserveProductQuantitySuccessfully() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(baseProduct));
            when(productRepository.update(any(Product.class))).thenReturn(baseProduct);

            boolean result = productService.reserveProductQuantity(1L, 5);

            assertTrue(result);
            assertEquals(5, baseProduct.getQuantity()); // 10 - 5 = 5
            verify(productRepository).findById(1L);
            verify(productRepository).update(baseProduct);
        }

        @Test
        @DisplayName("Should fail to reserve insufficient quantity")
        void shouldFailToReserveInsufficientQuantity() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(baseProduct));

            boolean result = productService.reserveProductQuantity(1L, 15);

            assertFalse(result);
            assertEquals(10, baseProduct.getQuantity()); // Should remain unchanged
            verify(productRepository).findById(1L);
            verify(productRepository, never()).update(any());
        }

        @Test
        @DisplayName("Should release product quantity")
        void shouldReleaseProductQuantity() {
            baseProduct.setQuantity(5);
            when(productRepository.findById(1L)).thenReturn(Optional.of(baseProduct));
            when(productRepository.update(any(Product.class))).thenReturn(baseProduct);

            productService.releaseProductQuantity(1L, 3);

            assertEquals(8, baseProduct.getQuantity()); // 5 + 3 = 8
            verify(productRepository).findById(1L);
            verify(productRepository).update(baseProduct);
        }

        @Test
        @DisplayName("Should get low stock products")
        void shouldGetLowStockProducts() {
            List<Product> lowStockProducts = Arrays.asList(baseProduct);
            when(productRepository.findLowStockProducts(5)).thenReturn(lowStockProducts);

            List<Product> result = productService.getLowStockProducts(5);

            assertEquals(1, result.size());
            verify(productRepository).findLowStockProducts(5);
        }

        @Test
        @DisplayName("Should throw exception for negative threshold")
        void shouldThrowExceptionForNegativeThreshold() {
            assertThrows(IllegalArgumentException.class, 
                () -> productService.getLowStockProducts(-1));
        }

        @Test
        @DisplayName("Should delete product successfully")
        void shouldDeleteProductSuccessfully() {
            when(productRepository.existsById(1L)).thenReturn(true);

            assertDoesNotThrow(() -> productService.deleteProduct(1L));

            verify(productRepository).existsById(1L);
            verify(productRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent product")
        void shouldThrowExceptionWhenDeletingNonExistentProduct() {
            when(productRepository.existsById(999L)).thenReturn(false);

            assertThrows(IllegalArgumentException.class, 
                () -> productService.deleteProduct(999L));
            verify(productRepository).existsById(999L);
            verify(productRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("Product Application Service Tests")
    class ProductApplicationServiceTests {

        private ProductApplicationService appService;

        @BeforeEach
        void setUp() {
            appService = new ProductApplicationService(mockProductService, productRepository);
        }

        @Test
        @DisplayName("Should add product successfully")
        void shouldAddProductSuccessfully() {
            AddProductCommand command = AddProductCommand.builder()
                    .product(baseProduct)
                    .makeAvailableImmediately(true)
                    .createdBy("admin")
                    .source("ADMIN_PANEL")
                    .build();

            when(mockProductService.createProduct(baseProduct)).thenReturn(baseProduct);

            Long result = appService.addProduct(command);

            assertEquals(1L, result);
            verify(mockProductService).createProduct(baseProduct);
        }

        @Test
        @DisplayName("Should throw exception for invalid add command")
        void shouldThrowExceptionForInvalidAddCommand() {
            // Null command
            assertThrows(ProductApplicationException.class, 
                () -> appService.addProduct(null));

            // Invalid command
            AddProductCommand invalidCommand = AddProductCommand.builder()
                    .product(null)
                    .build();
            assertThrows(ProductApplicationException.class, 
                () -> appService.addProduct(invalidCommand));
        }

        @Test
        @DisplayName("Should update product successfully")
        void shouldUpdateProductSuccessfully() {
            UpdateProductCommand command = UpdateProductCommand.builder()
                    .productId(1L)
                    .product(baseProduct)
                    .updatedBy("admin")
                    .source("ADMIN_PANEL")
                    .notifyInventoryChanges(true)
                    .build();

            when(productRepository.existsById(1L)).thenReturn(true);
            when(productRepository.findById(1L)).thenReturn(Optional.of(baseProduct));
            when(mockProductService.updateProduct(1L, baseProduct)).thenReturn(baseProduct);

            Product result = appService.updateProduct(command);

            assertNotNull(result);
            assertEquals(baseProduct.getTitle(), result.getTitle());
            verify(mockProductService).updateProduct(1L, baseProduct);
        }

        @Test
        @DisplayName("Should throw exception for non-existent product update")
        void shouldThrowExceptionForNonExistentProductUpdate() {
            UpdateProductCommand command = UpdateProductCommand.builder()
                    .productId(999L)
                    .product(baseProduct)
                    .build();

            when(productRepository.existsById(999L)).thenReturn(false);

            assertThrows(ProductApplicationException.class, 
                () -> appService.updateProduct(command));
        }

        @Test
        @DisplayName("Should view product successfully")
        void shouldViewProductSuccessfully() {
            when(mockProductService.getProductById(1L)).thenReturn(Optional.of(baseProduct));

            Optional<Product> result = appService.viewProduct(1L);

            assertTrue(result.isPresent());
            assertEquals(baseProduct, result.get());
        }

        @Test
        @DisplayName("Should throw exception for invalid product ID")
        void shouldThrowExceptionForInvalidProductId() {
            assertThrows(ProductApplicationException.class, 
                () -> appService.viewProduct(null));
            assertThrows(ProductApplicationException.class, 
                () -> appService.viewProduct(0L));
            assertThrows(ProductApplicationException.class, 
                () -> appService.viewProduct(-1L));
        }

        @Test
        @DisplayName("Should search products successfully")
        void shouldSearchProductsSuccessfully() {
            SearchProductQuery query = SearchProductQuery.builder()
                    .title("Test")
                    .type("book")
                    .minPrice(50)
                    .maxPrice(200)
                    .inStock(true)
                    .page(0)
                    .size(10)
                    .build();

            // Create a product that matches the search criteria
            Product matchingProduct = Product.builder()
                    .productId(99L)
                    .title("Test Book")
                    .price(100) // Within range 50-200
                    .quantity(10) // In stock
                    .rushOrderSupported(false)
                    .build();

            List<Product> mockResults = Arrays.asList(matchingProduct);
            when(mockProductService.searchProductsByTitle("Test")).thenReturn(mockResults);

            List<Product> result = appService.searchProducts(query);

            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("Should search by type when title is not provided")
        void shouldSearchByTypeWhenTitleNotProvided() {
            SearchProductQuery query = SearchProductQuery.builder()
                    .type("book")
                    .build();

            List<Product> mockResults = Arrays.asList(book);
            when(mockProductService.getProductsByType("book")).thenReturn(mockResults);

            List<Product> result = appService.searchProducts(query);

            assertNotNull(result);
            verify(mockProductService).getProductsByType("book");
        }

        @Test
        @DisplayName("Should return all products when no criteria specified")
        void shouldReturnAllProductsWhenNoCriteriaSpecified() {
            SearchProductQuery query = SearchProductQuery.builder()
                    .build();

            List<Product> mockResults = Arrays.asList(baseProduct, book, cd, dvd);
            when(mockProductService.getAllProducts()).thenReturn(mockResults);

            List<Product> result = appService.searchProducts(query);

            assertEquals(4, result.size());
            verify(mockProductService).getAllProducts();
        }

        @Test
        @DisplayName("Should throw exception for invalid price range")
        void shouldThrowExceptionForInvalidPriceRange() {
            SearchProductQuery query = SearchProductQuery.builder()
                    .minPrice(200)
                    .maxPrice(100) // Invalid: min > max
                    .build();

            assertThrows(ProductApplicationException.class, 
                () -> appService.searchProducts(query));
        }

        @Test
        @DisplayName("Should get all products with filtering")
        void shouldGetAllProductsWithFiltering() {
            List<Product> mockProducts = Arrays.asList(baseProduct, book);
            when(mockProductService.getAllProducts()).thenReturn(mockProducts);

            List<Product> result = appService.getAllProducts();

            assertEquals(2, result.size());
            // Verify only valid products are returned
            assertTrue(result.stream().allMatch(Product::isValid));
        }

        @Test
        @DisplayName("Should check product availability")
        void shouldCheckProductAvailability() {
            when(mockProductService.checkProductAvailability(1L, 5)).thenReturn(true);

            boolean result = appService.checkProductAvailability(1L, 5);

            assertTrue(result);
            verify(mockProductService).checkProductAvailability(1L, 5);
        }

        @Test
        @DisplayName("Should return false for availability check errors")
        void shouldReturnFalseForAvailabilityCheckErrors() {
            // Invalid product ID
            boolean result1 = appService.checkProductAvailability(null, 5);
            assertFalse(result1);

            // Invalid quantity
            boolean result2 = appService.checkProductAvailability(1L, 0);
            assertFalse(result2);

            // Negative quantity
            boolean result3 = appService.checkProductAvailability(1L, -1);
            assertFalse(result3);
        }
    }

    @Nested
    @DisplayName("Command and Query Tests")
    class CommandAndQueryTests {

        @Test
        @DisplayName("Should validate AddProductCommand")
        void shouldValidateAddProductCommand() {
            AddProductCommand validCommand = AddProductCommand.builder()
                    .product(baseProduct)
                    .makeAvailableImmediately(true)
                    .createdBy("admin")
                    .source("ADMIN_PANEL")
                    .build();

            assertTrue(validCommand.isValid());

            // Invalid command - null product
            AddProductCommand invalidCommand = AddProductCommand.builder()
                    .product(null)
                    .build();
            assertFalse(invalidCommand.isValid());
        }

        @Test
        @DisplayName("Should validate UpdateProductCommand")
        void shouldValidateUpdateProductCommand() {
            UpdateProductCommand validCommand = UpdateProductCommand.builder()
                    .productId(1L)
                    .product(baseProduct)
                    .updatedBy("admin")
                    .source("ADMIN_PANEL")
                    .build();

            assertTrue(validCommand.isValid());

            // Invalid command - null product ID
            UpdateProductCommand invalidCommand1 = UpdateProductCommand.builder()
                    .productId(null)
                    .product(baseProduct)
                    .build();
            assertFalse(invalidCommand1.isValid());

            // Invalid command - negative product ID
            UpdateProductCommand invalidCommand2 = UpdateProductCommand.builder()
                    .productId(-1L)
                    .product(baseProduct)
                    .build();
            assertFalse(invalidCommand2.isValid());
        }

        @Test
        @DisplayName("Should validate SearchProductQuery")
        void shouldValidateSearchProductQuery() {
            SearchProductQuery validQuery = SearchProductQuery.builder()
                    .title("Test")
                    .type("book")
                    .minPrice(50)
                    .maxPrice(200)
                    .build();

            assertTrue(validQuery.isPriceRangeValid());
            assertTrue(validQuery.hasSearchCriteria());

            // Invalid price range
            SearchProductQuery invalidQuery = SearchProductQuery.builder()
                    .minPrice(200)
                    .maxPrice(100)
                    .build();
            assertFalse(invalidQuery.isPriceRangeValid());

            // No search criteria
            SearchProductQuery emptyQuery = SearchProductQuery.builder().build();
            assertFalse(emptyQuery.hasSearchCriteria());
        }

        @Test
        @DisplayName("Should build commands with default values")
        void shouldBuildCommandsWithDefaultValues() {
            AddProductCommand command = AddProductCommand.builder()
                    .product(baseProduct)
                    .build();

            assertTrue(command.isMakeAvailableImmediately()); // Default should be true

            SearchProductQuery query = SearchProductQuery.builder()
                    .title("Test")
                    .build();

            assertEquals(0, query.getPage()); // Default page
            assertEquals(20, query.getSize()); // Default size
            assertEquals("title", query.getSortBy()); // Default sort field
            assertEquals("ASC", query.getSortDirection()); // Default sort direction
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        private ProductApplicationService appService;

        @BeforeEach
        void setUp() {
            appService = new ProductApplicationService(mockProductService, productRepository);
        }

        @Test
        @DisplayName("Should handle complete product lifecycle")
        void shouldHandleCompleteProductLifecycle() {
            // Create
            when(mockProductService.createProduct(any(Product.class))).thenReturn(baseProduct);
            AddProductCommand createCommand = AddProductCommand.builder()
                    .product(baseProduct)
                    .build();
            Long productId = appService.addProduct(createCommand);
            assertEquals(1L, productId);

            // Read
            when(mockProductService.getProductById(1L)).thenReturn(Optional.of(baseProduct));
            Optional<Product> retrieved = appService.viewProduct(1L);
            assertTrue(retrieved.isPresent());

            // Update
            when(productRepository.existsById(1L)).thenReturn(true);
            when(productRepository.findById(1L)).thenReturn(Optional.of(baseProduct));
            when(mockProductService.updateProduct(eq(1L), any(Product.class))).thenReturn(baseProduct);
            
            UpdateProductCommand updateCommand = UpdateProductCommand.builder()
                    .productId(1L)
                    .product(baseProduct)
                    .build();
            Product updated = appService.updateProduct(updateCommand);
            assertNotNull(updated);

            // Verify all interactions
            verify(mockProductService).createProduct(any(Product.class));
            verify(mockProductService).getProductById(1L);
            verify(mockProductService).updateProduct(eq(1L), any(Product.class));
        }

        @Test
        @DisplayName("Should handle product search with multiple criteria")
        void shouldHandleProductSearchWithMultipleCriteria() {
            SearchProductQuery complexQuery = SearchProductQuery.builder()
                    .title("Java")
                    .type("book")
                    .minPrice(100)
                    .maxPrice(600)
                    .inStock(true)
                    .rushOrderSupported(true)
                    .page(0)
                    .size(10)
                    .build();

            when(mockProductService.searchProductsByTitle("Java")).thenReturn(Arrays.asList(book));

            List<Product> results = appService.searchProducts(complexQuery);

            assertNotNull(results);
            verify(mockProductService).searchProductsByTitle("Java");
        }
    }
}