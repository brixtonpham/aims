package com.aims.presentation.web;

import com.aims.application.commands.AddProductCommand;
import com.aims.application.commands.UpdateProductCommand;
import com.aims.application.commands.SearchProductQuery;
import com.aims.application.services.ProductApplicationService;
import com.aims.domain.product.entity.Product;
import com.aims.domain.product.entity.Book;
import com.aims.domain.product.entity.CD;
import com.aims.domain.product.entity.DVD;
import com.aims.presentation.dto.ProductRequest;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController Unit Tests")
class ProductControllerTest {

    @Mock
    private ProductApplicationService productApplicationService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    
    private Product testProduct;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
        objectMapper = new ObjectMapper();
        
        // Setup test data
        testProduct = Product.builder()
            .productId(1L)
            .title("Test Product")
            .type("book")
            .price(50000)
            .quantity(100)
            .imageUrl("http://example.com/image.jpg")
            .introduction("Test description")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        productRequest = new ProductRequest();
        productRequest.setTitle("Test Product");
        productRequest.setType("Book");
        productRequest.setPrice(new BigDecimal("50000"));
        productRequest.setQuantity(100);
        productRequest.setDescription("Test description");
        productRequest.setImageUrl("http://example.com/image.jpg");
    }

    @Test
    @DisplayName("Should create product successfully")
    void createProduct_ValidRequest_ReturnsCreated() throws Exception {
        // Given
        when(productApplicationService.addProduct(any(AddProductCommand.class))).thenReturn(1L);

        // When & Then
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Product created successfully"))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(productApplicationService).addProduct(any(AddProductCommand.class));
    }

    @Test
    @DisplayName("Should handle create product validation error")
    void createProduct_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Given
        when(productApplicationService.addProduct(any(AddProductCommand.class)))
            .thenThrow(new IllegalArgumentException("Invalid product data"));

        // When & Then
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid product data"))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Should update product successfully")
    void updateProduct_ValidRequest_ReturnsOk() throws Exception {
        // Given
        productRequest.setId(1L);
        when(productApplicationService.updateProduct(any(UpdateProductCommand.class)))
            .thenReturn(testProduct);

        // When & Then
        mockMvc.perform(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Product updated successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Test Product"));

        verify(productApplicationService).updateProduct(any(UpdateProductCommand.class));
    }

    @Test
    @DisplayName("Should get product by ID successfully")
    void getProduct_ValidId_ReturnsProduct() throws Exception {
        // Given
        when(productApplicationService.viewProduct(1L)).thenReturn(Optional.of(testProduct));

        // When & Then
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Test Product"))
                .andExpect(jsonPath("$.data.type").value("book"));

        verify(productApplicationService).viewProduct(1L);
    }

    @Test
    @DisplayName("Should return not found for non-existent product")
    void getProduct_NonExistentId_ReturnsNotFound() throws Exception {
        // Given
        when(productApplicationService.viewProduct(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Product not found"))
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));

        verify(productApplicationService).viewProduct(999L);
    }

    @Test
    @DisplayName("Should return bad request for invalid product ID")
    void getProduct_InvalidId_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/products/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid product ID"))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        verify(productApplicationService, never()).viewProduct(anyLong());
    }

    @Test
    @DisplayName("Should get all products successfully")
    void getAllProducts_ValidRequest_ReturnsProductList() throws Exception {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productApplicationService.getAllProducts()).thenReturn(products);

        // When & Then
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Test Product"));

        verify(productApplicationService).getAllProducts();
    }

    @Test
    @DisplayName("Should search products with title parameter")
    void searchProducts_WithTitle_ReturnsFilteredProducts() throws Exception {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productApplicationService.searchProducts(any(SearchProductQuery.class)))
            .thenReturn(products);

        // When & Then
        mockMvc.perform(get("/api/products/search")
                .param("title", "Test")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].title").value("Test Product"));

        verify(productApplicationService).searchProducts(any(SearchProductQuery.class));
    }

    @Test
    @DisplayName("Should search products with multiple parameters")
    void searchProducts_WithMultipleParams_ReturnsFilteredProducts() throws Exception {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productApplicationService.searchProducts(any(SearchProductQuery.class)))
            .thenReturn(products);

        // When & Then
        mockMvc.perform(get("/api/products/search")
                .param("title", "Test")
                .param("type", "book")
                .param("minPrice", "10000")
                .param("maxPrice", "100000")
                .param("inStock", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        verify(productApplicationService).searchProducts(argThat(query -> 
            "Test".equals(query.getTitle()) && 
            "book".equals(query.getType()) &&
            query.getMinPrice().equals(10000) &&
            query.getMaxPrice().equals(100000) &&
            query.getInStock().equals(true)
        ));
    }

    @Test
    @DisplayName("Should return not implemented for delete product")
    void deleteProduct_ValidId_ReturnsNotImplemented() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNotImplemented())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Delete functionality not implemented"))
                .andExpect(jsonPath("$.errorCode").value("NOT_IMPLEMENTED"));

        verify(productApplicationService, never()).viewProduct(anyLong());
    }

    @Test
    @DisplayName("Should handle internal server error gracefully")
    void getProduct_ServiceThrowsException_ReturnsInternalServerError() throws Exception {
        // Given
        when(productApplicationService.viewProduct(1L))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to get product"))
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"));
    }

    @Test
    @DisplayName("Should map Book-specific fields correctly")
    void createProduct_BookType_MapsFieldsCorrectly() throws Exception {
        // Given
        ProductRequest bookRequest = new ProductRequest();
        bookRequest.setTitle("Clean Code");
        bookRequest.setType("Book");
        bookRequest.setPrice(new BigDecimal("45000"));
        bookRequest.setQuantity(50);
        bookRequest.setAuthor("Robert C. Martin");
        bookRequest.setGenre("Programming");
        bookRequest.setPublisher("Prentice Hall");
        bookRequest.setNumberOfPages(464);

        when(productApplicationService.addProduct(any(AddProductCommand.class))).thenReturn(2L);

        // When & Then
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        verify(productApplicationService).addProduct(argThat(command -> {
            Product product = command.getProduct();
            return product instanceof Book &&
                   "Clean Code".equals(product.getTitle()) &&
                   "Robert C. Martin".equals(((Book) product).getAuthors());
        }));
    }

    @Test
    @DisplayName("Should map CD-specific fields correctly")
    void createProduct_CDType_MapsFieldsCorrectly() throws Exception {
        // Given
        ProductRequest cdRequest = new ProductRequest();
        cdRequest.setTitle("Test Album");
        cdRequest.setType("CD");
        cdRequest.setPrice(new BigDecimal("25000"));
        cdRequest.setQuantity(30);
        cdRequest.setArtist("Test Artist");
        cdRequest.setRecordLabel("Test Records");
        cdRequest.setMusicGenre("Rock");
        cdRequest.setReleaseDate("2023");

        when(productApplicationService.addProduct(any(AddProductCommand.class))).thenReturn(3L);

        // When & Then
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cdRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        verify(productApplicationService).addProduct(argThat(command -> {
            Product product = command.getProduct();
            return product instanceof CD &&
                   "Test Album".equals(product.getTitle()) &&
                   "Test Artist".equals(((CD) product).getArtist());
        }));
    }

    @Test
    @DisplayName("Should map DVD-specific fields correctly")
    void createProduct_DVDType_MapsFieldsCorrectly() throws Exception {
        // Given
        ProductRequest dvdRequest = new ProductRequest();
        dvdRequest.setTitle("Test Movie");
        dvdRequest.setType("DVD");
        dvdRequest.setPrice(new BigDecimal("35000"));
        dvdRequest.setQuantity(20);
        dvdRequest.setDirector("Test Director");
        dvdRequest.setStudio("Test Studios");
        dvdRequest.setRuntime(120);
        dvdRequest.setSubtitles("English, Vietnamese");
        dvdRequest.setFilmGenre("Action");

        when(productApplicationService.addProduct(any(AddProductCommand.class))).thenReturn(4L);

        // When & Then
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dvdRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        verify(productApplicationService).addProduct(argThat(command -> {
            Product product = command.getProduct();
            return product instanceof DVD &&
                   "Test Movie".equals(product.getTitle()) &&
                   "Test Director".equals(((DVD) product).getDirector());
        }));
    }

    @Test
    @DisplayName("Should handle search with empty results")
    void searchProducts_NoResults_ReturnsEmptyList() throws Exception {
        // Given
        when(productApplicationService.searchProducts(any(SearchProductQuery.class)))
            .thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/products/search")
                .param("title", "NonExistentProduct"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Should return response with correct product type mapping")
    void getProduct_BookType_ReturnsBookSpecificFields() throws Exception {
        // Given
        Book book = Book.builder()
            .productId(1L)
            .title("Test Book")
            .type("book")
            .price(40000)
            .quantity(25)
            .authors("Test Author")
            .genre("Fiction")
            .publishers("Test Publisher")
            .pageCount(300)
            .build();

        when(productApplicationService.viewProduct(1L)).thenReturn(Optional.of(book));

        // When & Then
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.author").value("Test Author"))
                .andExpect(jsonPath("$.data.genre").value("Fiction"))
                .andExpect(jsonPath("$.data.publisher").value("Test Publisher"))
                .andExpect(jsonPath("$.data.numberOfPages").value(300));
    }
}