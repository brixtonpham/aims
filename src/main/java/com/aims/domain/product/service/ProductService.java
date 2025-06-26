package com.aims.domain.product.service;

import com.aims.domain.product.entity.Product;
import com.aims.domain.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Domain service for Product business logic.
 * Contains pure business logic without orchestration concerns.
 * Follows Domain-Driven Design principles.
 */
@Service
@Transactional
public class ProductService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private static final String PRODUCT_NOT_FOUND_MESSAGE = "Product not found with ID: ";
    
    private final ProductRepository productRepository;
    
    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    /**
     * Create a new product with business validation
     */
    public Product createProduct(Product product) {
        // Domain validation
        validateProductForCreation(product);
        
        logger.debug("Creating new product: {}", product.getTitle());
        
        // Business rule: Ensure product has proper defaults
        prepareProductForCreation(product);
        
        Product savedProduct = productRepository.save(product);
        
        if (savedProduct == null) {
            throw new IllegalStateException("Product repository returned null after save operation");
        }
        
        logger.info("Product created successfully with ID: {}", savedProduct.getProductId());
        
        return savedProduct;
    }
    
    /**
     * Update an existing product with business validation
     */
    public Product updateProduct(Long productId, Product updatedProduct) {
        logger.debug("Updating product with ID: {}", productId);
        
        // Verify product exists
        Product existingProduct = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException(PRODUCT_NOT_FOUND_MESSAGE + productId));
        
        // Domain validation
        validateProductForUpdate(updatedProduct);
        
        // Business rule: Preserve creation data
        updatedProduct.setProductId(productId);
        updatedProduct.setCreatedAt(existingProduct.getCreatedAt());
        
        Product savedProduct = productRepository.update(updatedProduct);
        logger.info("Product updated successfully with ID: {}", productId);
        
        return savedProduct;
    }
    
    /**
     * Get product by ID with business context
     */
    public Optional<Product> getProductById(Long productId) {
        logger.debug("Retrieving product with ID: {}", productId);
        return productRepository.findById(productId);
    }
    
    /**
     * Get all products with business filtering
     */
    public List<Product> getAllProducts() {
        logger.debug("Retrieving all products");
        return productRepository.findAll();
    }
    
    /**
     * Search products by title
     */
    public List<Product> searchProductsByTitle(String title) {
        logger.debug("Searching products by title: {}", title);
        
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Search title cannot be empty");
        }
        
        return productRepository.findByTitleContaining(title.trim());
    }
    
    /**
     * Get products by type with validation
     */
    public List<Product> getProductsByType(String type) {
        logger.debug("Retrieving products by type: {}", type);
        
        if (!isValidProductType(type)) {
            throw new IllegalArgumentException("Invalid product type: " + type);
        }
        
        return productRepository.findByType(type);
    }
    
    /**
     * Check product availability for ordering
     */
    public boolean checkProductAvailability(Long productId, int quantity) {
        logger.debug("Checking availability for product ID: {} with quantity: {}", productId, quantity);
        
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        
        return productRepository.isAvailable(productId, quantity);
    }
    
    /**
     * Reserve product quantity for order
     */
    public boolean reserveProductQuantity(Long productId, int quantity) {
        logger.debug("Reserving quantity {} for product ID: {}", quantity, productId);
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException(PRODUCT_NOT_FOUND_MESSAGE + productId));
        
        if (product.reserveQuantity(quantity)) {
            productRepository.update(product);
            logger.info("Reserved {} units for product ID: {}", quantity, productId);
            return true;
        }
        
        logger.warn("Failed to reserve {} units for product ID: {}", quantity, productId);
        return false;
    }
    
    /**
     * Release reserved product quantity (e.g., when order is cancelled)
     */
    public void releaseProductQuantity(Long productId, int quantity) {
        logger.debug("Releasing quantity {} for product ID: {}", quantity, productId);
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException(PRODUCT_NOT_FOUND_MESSAGE + productId));
        
        product.releaseQuantity(quantity);
        productRepository.update(product);
        
        logger.info("Released {} units for product ID: {}", quantity, productId);
    }
    
    /**
     * Get products with low stock
     */
    public List<Product> getLowStockProducts(int threshold) {
        logger.debug("Retrieving products with stock below: {}", threshold);
        
        if (threshold < 0) {
            throw new IllegalArgumentException("Threshold must be non-negative");
        }
        
        return productRepository.findLowStockProducts(threshold);
    }
    
    /**
     * Delete product with business validation
     */
    public void deleteProduct(Long productId) {
        logger.debug("Deleting product with ID: {}", productId);
        
        // Business rule: Only delete if product exists
        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException(PRODUCT_NOT_FOUND_MESSAGE + productId);
        }
        
        // Additional business rule: Check if product is referenced in orders
        // This would require integration with order domain in full implementation
        
        productRepository.deleteById(productId);
        logger.info("Product deleted successfully with ID: {}", productId);
    }
    
    // Private helper methods for business validation
    
    private void validateProductForCreation(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        
        if (!product.isValid()) {
            throw new IllegalArgumentException("Product validation failed");
        }
        
        // Business rule: Title must be unique (simplified for demo)
        // In real implementation, this would check for duplicate titles
    }
    
    private void validateProductForUpdate(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        
        if (!product.isValid()) {
            throw new IllegalArgumentException("Product validation failed");
        }
        
        // Additional validation for updates: ID should not be changed in the payload
        // This is a different business rule from creation
    }
    
    private void prepareProductForCreation(Product product) {
        // Business rule: Set default values
        if (product.getRushOrderSupported() == null) {
            product.setRushOrderSupported(false);
        }
        
        if (product.getQuantity() == null) {
            product.setQuantity(0);
        }
    }
    
    private boolean isValidProductType(String type) {
        return type != null && (type.equalsIgnoreCase("book") || 
                               type.equalsIgnoreCase("cd") || 
                               type.equalsIgnoreCase("dvd"));
    }
}
