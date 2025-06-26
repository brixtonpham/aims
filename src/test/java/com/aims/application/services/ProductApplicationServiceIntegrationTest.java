package com.aims.application.services;

import com.aims.application.commands.AddProductCommand;
import com.aims.application.commands.UpdateProductCommand;
import com.aims.application.commands.SearchProductQuery;
import com.aims.domain.product.entity.Product;
import com.aims.domain.product.entity.Book;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ProductApplicationService Integration Tests")
class ProductApplicationServiceIntegrationTest {
    
    @Autowired
    private ProductApplicationService productApplicationService;
    
    @Test
    @DisplayName("Should complete full product lifecycle workflow")
    void productLifecycle_FullWorkflow_Success() {
        // Step 1: Create a new product
        Book book = Book.builder()
            .title("Clean Architecture")
            .price(50000)
            .quantity(100)
            .weight(0.5f)
            .rushOrderSupported(true)
            .authors("Robert C. Martin")
            .genre("Programming")
            .pageCount(432)
            .publishers("Prentice Hall")
            .coverType("Paperback")
            .build();
        
        AddProductCommand addCommand = AddProductCommand.builder()
            .product(book)
            .makeAvailableImmediately(true)
            .createdBy("integration-test")
            .source("INTEGRATION_TEST")
            .build();
        
        // Add the product
        Long productId = productApplicationService.addProduct(addCommand);
        assertNotNull(productId);
        assertTrue(productId > 0);
        
        // Step 2: View the created product
        Optional<Product> retrievedProduct = productApplicationService.viewProduct(productId);
        assertTrue(retrievedProduct.isPresent());
        assertEquals("Clean Architecture", retrievedProduct.get().getTitle());
        assertEquals(50000, retrievedProduct.get().getPrice());
        
        // Step 3: Update the product
        Product updatedBook = retrievedProduct.get();
        updatedBook.setPrice(45000); // Discount
        updatedBook.setQuantity(150); // Restock
        
        UpdateProductCommand updateCommand = UpdateProductCommand.builder()
            .productId(productId)
            .product(updatedBook)
            .updatedBy("integration-test")
            .source("INTEGRATION_TEST")
            .notifyInventoryChanges(true)
            .build();
        
        Product updated = productApplicationService.updateProduct(updateCommand);
        assertEquals(45000, updated.getPrice());
        assertEquals(150, updated.getQuantity());
        
        // Step 4: Search for the product
        SearchProductQuery searchQuery = SearchProductQuery.builder()
            .title("Clean")
            .minPrice(40000)
            .maxPrice(50000)
            .inStock(true)
            .rushOrderSupported(true)
            .build();
        
        List<Product> searchResults = productApplicationService.searchProducts(searchQuery);
        assertFalse(searchResults.isEmpty());
        assertTrue(searchResults.stream()
            .anyMatch(p -> p.getTitle().contains("Clean Architecture")));
        
        // Step 5: Check availability
        boolean available = productApplicationService.checkProductAvailability(productId, 50);
        assertTrue(available);
        
        boolean notAvailable = productApplicationService.checkProductAvailability(productId, 200);
        assertFalse(notAvailable);
        
        // Step 6: Get all products (should include our product)
        List<Product> allProducts = productApplicationService.getAllProducts();
        assertTrue(allProducts.stream()
            .anyMatch(p -> p.getProductId().equals(productId)));
    }
    
    @Test
    @DisplayName("Should handle search with different criteria")
    void searchProducts_DifferentCriteria_WorksCorrectly() {
        // Create test products
        Book book1 = Book.builder()
            .title("Java Programming")
            .price(40000)
            .quantity(50)
            .rushOrderSupported(true)
            .authors("Test Author")
            .genre("Programming")
            .pageCount(300)
            .build();
        
        Book book2 = Book.builder()
            .title("Python Basics")
            .price(35000)
            .quantity(0) // Out of stock
            .rushOrderSupported(false)
            .authors("Test Author")
            .genre("Programming")
            .pageCount(250)
            .build();
        
        // Add both products
        Long id1 = productApplicationService.addProduct(
            AddProductCommand.builder().product(book1).build());
        Long id2 = productApplicationService.addProduct(
            AddProductCommand.builder().product(book2).build());
        
        // Search by type
        SearchProductQuery typeQuery = SearchProductQuery.builder()
            .type("book")
            .build();
        List<Product> typeResults = productApplicationService.searchProducts(typeQuery);
        assertTrue(typeResults.size() >= 2);
        
        // Search by price range
        SearchProductQuery priceQuery = SearchProductQuery.builder()
            .minPrice(38000)
            .maxPrice(42000)
            .build();
        List<Product> priceResults = productApplicationService.searchProducts(priceQuery);
        assertTrue(priceResults.stream()
            .anyMatch(p -> p.getProductId().equals(id1)));
        assertFalse(priceResults.stream()
            .anyMatch(p -> p.getProductId().equals(id2))); // Price too low
        
        // Search by stock status
        SearchProductQuery stockQuery = SearchProductQuery.builder()
            .inStock(true)
            .build();
        List<Product> stockResults = productApplicationService.searchProducts(stockQuery);
        assertTrue(stockResults.stream()
            .anyMatch(p -> p.getProductId().equals(id1)));
        assertFalse(stockResults.stream()
            .anyMatch(p -> p.getProductId().equals(id2))); // Out of stock
        
        // Search by rush order support
        SearchProductQuery rushQuery = SearchProductQuery.builder()
            .rushOrderSupported(true)
            .build();
        List<Product> rushResults = productApplicationService.searchProducts(rushQuery);
        assertTrue(rushResults.stream()
            .anyMatch(p -> p.getProductId().equals(id1)));
        assertFalse(rushResults.stream()
            .anyMatch(p -> p.getProductId().equals(id2))); // No rush support
    }
    
    @Test
    @DisplayName("Should handle pagination in search results")
    void searchProducts_WithPagination_WorksCorrectly() {
        // Create multiple test products
        for (int i = 1; i <= 5; i++) {
            Book book = Book.builder()
                .title("Test Book " + i)
                .price(30000 + i * 1000)
                .quantity(10)
                .authors("Test Author " + i)
                .genre("Test")
                .pageCount(200 + i * 10)
                .build();
            
            productApplicationService.addProduct(
                AddProductCommand.builder().product(book).build());
        }
        
        // Search with pagination - first page
        SearchProductQuery page1Query = SearchProductQuery.builder()
            .title("Test Book")
            .page(0)
            .size(2)
            .build();
        List<Product> page1Results = productApplicationService.searchProducts(page1Query);
        assertEquals(2, page1Results.size());
        
        // Search with pagination - second page
        SearchProductQuery page2Query = SearchProductQuery.builder()
            .title("Test Book")
            .page(1)
            .size(2)
            .build();
        List<Product> page2Results = productApplicationService.searchProducts(page2Query);
        assertEquals(2, page2Results.size());
        
        // Verify different results on different pages
        assertNotEquals(page1Results.get(0).getProductId(), 
                       page2Results.get(0).getProductId());
    }
}
