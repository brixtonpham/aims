package com.aims.integration;

import com.aims.application.services.CartApplicationService;
import com.aims.application.commands.AddProductToCartCommand;
import com.aims.domain.product.entity.Product;
import com.aims.domain.cart.entity.Cart;
import com.aims.infrastructure.persistence.jpa.JpaProductRepository;
import com.aims.infrastructure.persistence.jpa.JpaCartRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for workflow components that exist
 * Tests the existing parts of the system integration
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderWorkflowIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CartApplicationService cartApplicationService;

    @Autowired
    private JpaProductRepository productRepository;

    @Autowired
    private JpaCartRepository cartRepository;

    @Test
    @DisplayName("Product and Cart Integration Test")
    void testProductCartIntegration() {
        // Given: A product exists in the system
        Product product = createTestProduct();
        product = productRepository.save(product);

        // And: A customer adds the product to cart
        Long customerId = 1L;
        AddProductToCartCommand cartCommand = new AddProductToCartCommand();
        cartCommand.setCustomerId(customerId.toString());
        cartCommand.setProductId(product.getProductId());
        cartCommand.setQuantity(2);

        // When: Adding product to cart
        cartApplicationService.addProductToCart(cartCommand);

        // Then: Cart should be created and contain the product
        Optional<Cart> cartOpt = cartRepository.findByCustomerId(customerId);
        assertTrue(cartOpt.isPresent());
        Cart cart = cartOpt.get();
        assertNotNull(cart.getCartItems());
        assertFalse(cart.getCartItems().isEmpty());
    }

    @Test
    @DisplayName("Product Service Integration Test")
    void testProductServiceIntegration() {
        // Given: A product
        Product product = createTestProduct();
        
        // When: Saving through repository
        Product savedProduct = productRepository.save(product);

        // Then: Product should be saved with ID
        assertNotNull(savedProduct);
        assertNotNull(savedProduct.getProductId());
        assertEquals("Test Product", savedProduct.getTitle());
    }

    private Product createTestProduct() {
        Product product = new Product();
        product.setTitle("Test Product");
        product.setPrice(9999); // Price in cents
        product.setQuantity(10);
        product.setType("PRODUCT");
        return product;
    }
}
