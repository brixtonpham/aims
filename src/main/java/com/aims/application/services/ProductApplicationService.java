package com.aims.application.services;

import com.aims.application.commands.AddProductCommand;
import com.aims.application.commands.UpdateProductCommand;
import com.aims.application.commands.SearchProductQuery;
import com.aims.application.exceptions.ProductApplicationException;
import com.aims.domain.product.entity.Product;
import com.aims.domain.product.service.ProductService;
import com.aims.domain.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Application Service for Product domain orchestration.
 * Handles complex workflows and coordinates between multiple domain services.
 * Follows Clean Architecture principles - orchestrates domain services without business logic.
 */
@Service
@Transactional
public class ProductApplicationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductApplicationService.class);
    
    private final ProductService productService;
    private final ProductRepository productRepository;
    
    @Autowired
    public ProductApplicationService(ProductService productService, 
                                   ProductRepository productRepository) {
        this.productService = productService;
        this.productRepository = productRepository;
    }
    
    /**
     * Add Product workflow orchestration
     * Implements the Add Product sequence from requirements
     */
    public Long addProduct(AddProductCommand command) {
        try {
            // Step 1: Validate command first
            validateAddProductCommand(command);
            
            logger.info("Starting add product workflow for: {}", command.getProduct().getTitle());
            
            // Step 2: Delegate to domain service for business logic
            Product savedProduct = productService.createProduct(command.getProduct());
            
            // Step 3: Handle post-creation activities (orchestration)
            if (command.isMakeAvailableImmediately()) {
                handleProductAvailability(savedProduct);
            }
            
            // Step 4: Audit logging (orchestration concern)
            logProductCreation(savedProduct, command);
            
            logger.info("Product added successfully with ID: {}", savedProduct.getProductId());
            return savedProduct.getProductId();
            
        } catch (IllegalArgumentException e) {
            logger.error("Validation failed for add product: {}", e.getMessage());
            throw new ProductApplicationException("Validation failed: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Failed to add product: {}", e.getMessage(), e);
            throw new ProductApplicationException("Failed to add product: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update Product workflow orchestration
     * Implements the Update Product sequence from requirements
     */
    public Product updateProduct(UpdateProductCommand command) {
        try {
            // Step 1: Validate command first
            validateUpdateProductCommand(command);
            
            logger.info("Starting update product workflow for ID: {}", command.getProductId());
            
            // Step 2: Check if product exists (orchestration validation)
            if (!productRepository.existsById(command.getProductId())) {
                throw new IllegalArgumentException("Product not found with ID: " + command.getProductId());
            }
            
            // Step 3: Get current product state for comparison
            Product currentProduct = productRepository.findById(command.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
            
            // Step 4: Delegate to domain service for business logic
            Product updatedProduct = productService.updateProduct(command.getProductId(), command.getProduct());
            
            // Step 5: Handle inventory change notifications (orchestration)
            if (command.isNotifyInventoryChanges()) {
                handleInventoryChangeNotification(currentProduct, updatedProduct);
            }
            
            // Step 6: Audit logging (orchestration concern)
            logProductUpdate(updatedProduct, command);
            
            logger.info("Product updated successfully with ID: {}", command.getProductId());
            return updatedProduct;
            
        } catch (IllegalArgumentException e) {
            logger.error("Validation failed for update product ID {}: {}", command != null ? command.getProductId() : "null", e.getMessage());
            throw new ProductApplicationException("Validation failed: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Failed to update product ID {}: {}", command != null ? command.getProductId() : "null", e.getMessage(), e);
            throw new ProductApplicationException("Failed to update product: " + e.getMessage(), e);
        }
    }
    
    /**
     * View Product workflow orchestration
     * Implements the View Product sequence from requirements
     */
    public Optional<Product> viewProduct(Long productId) {
        logger.debug("Starting view product workflow for ID: {}", productId);
        
        try {
            // Step 1: Validate input
            if (productId == null || productId <= 0) {
                throw new IllegalArgumentException("Invalid product ID: " + productId);
            }
            
            // Step 2: Delegate to domain service
            Optional<Product> product = productService.getProductById(productId);
            
            // Step 3: Log access for analytics (orchestration concern)
            if (product.isPresent()) {
                logProductAccess(product.get());
            }
            
            return product;
            
        } catch (Exception e) {
            logger.error("Failed to view product ID {}: {}", productId, e.getMessage(), e);
            throw new ProductApplicationException("Failed to view product: " + e.getMessage(), e);
        }
    }
    
    /**
     * Search Products workflow orchestration
     * Implements the Search Products sequence from requirements
     */
    public List<Product> searchProducts(SearchProductQuery query) {
        logger.debug("Starting search products workflow with criteria: {}", query);
        
        try {
            // Step 1: Validate query
            validateSearchQuery(query);
            
            // Step 2: Orchestrate search based on criteria
            List<Product> results = executeSearch(query);
            
            // Step 3: Apply business rules and filtering
            results = applySearchBusinessRules(results, query);
            
            // Step 4: Handle pagination (orchestration concern)
            results = applyPagination(results, query);
            
            // Step 5: Log search activity (orchestration concern)
            logSearchActivity(query, results.size());
            
            logger.debug("Search completed. Found {} products", results.size());
            return results;
            
        } catch (Exception e) {
            logger.error("Failed to search products: {}", e.getMessage(), e);
            throw new ProductApplicationException("Failed to search products: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get all products with orchestration concerns
     */
    public List<Product> getAllProducts() {
        logger.debug("Starting get all products workflow");
        
        try {
            // Delegate to domain service
            List<Product> products = productService.getAllProducts();
            
            // Apply any application-level filtering if needed
            products = applyApplicationFiltering(products);
            
            logger.debug("Retrieved {} products", products.size());
            return products;
            
        } catch (Exception e) {
            logger.error("Failed to get all products: {}", e.getMessage(), e);
            throw new ProductApplicationException("Failed to get all products: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check product availability workflow
     */
    public boolean checkProductAvailability(Long productId, int quantity) {
        logger.debug("Checking product availability: ID={}, quantity={}", productId, quantity);
        
        try {
            // Validate inputs
            if (productId == null || productId <= 0) {
                throw new IllegalArgumentException("Invalid product ID: " + productId);
            }
            
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive: " + quantity);
            }
            
            // Delegate to domain service
            return productService.checkProductAvailability(productId, quantity);
            
        } catch (Exception e) {
            logger.error("Failed to check product availability: {}", e.getMessage(), e);
            return false; // Fail safe - assume not available if check fails
        }
    }
    
    /**
     * Delete Product workflow orchestration
     * Implements the Delete Product sequence with business validation
     */
    public void deleteProduct(Long productId) {
        logger.debug("Starting delete product workflow for ID: {}", productId);
        
        try {
            // Step 1: Validate input
            if (productId == null || productId <= 0) {
                throw new IllegalArgumentException("Invalid product ID: " + productId);
            }
            
            // Step 2: Check if product exists (orchestration concern)
            Optional<Product> product = productService.getProductById(productId);
            if (product.isEmpty()) {
                throw new IllegalArgumentException("Product not found with ID: " + productId);
            }
            
            // Step 3: Additional business validation (orchestration concern)
            validateProductCanBeDeleted(product.get());
            
            // Step 4: Delegate to domain service
            productService.deleteProduct(productId);
            
            // Step 5: Log deletion activity (orchestration concern)
            logProductDeletion(product.get());
            
            logger.debug("Product deletion workflow completed for ID: {}", productId);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Product deletion failed - validation error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to delete product: {}", e.getMessage(), e);
            throw new ProductApplicationException("Failed to delete product: " + e.getMessage(), e);
        }
    }
    
    // Private orchestration methods
    
    private void validateAddProductCommand(AddProductCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Add product command cannot be null");
        }
        
        if (!command.isValid()) {
            throw new IllegalArgumentException("Invalid add product command");
        }
    }
    
    private void validateUpdateProductCommand(UpdateProductCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Update product command cannot be null");
        }
        
        if (!command.isValid()) {
            throw new IllegalArgumentException("Invalid update product command");
        }
    }
    
    private void validateSearchQuery(SearchProductQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("Search query cannot be null");
        }
        
        if (!query.isPriceRangeValid()) {
            throw new IllegalArgumentException("Invalid price range in search query");
        }
    }
    
    private void handleProductAvailability(Product product) {
        // Orchestration logic for making product immediately available
        // This could involve updating inventory systems, triggering notifications, etc.
        logger.debug("Handling product availability for: {}", product.getProductId());
    }
    
    private void handleInventoryChangeNotification(Product oldProduct, Product newProduct) {
        // Orchestration logic for inventory change notifications
        // This could involve sending alerts for low stock, price changes, etc.
        // Compare oldProduct with newProduct to determine what changed
        if (oldProduct.getQuantity() != null && newProduct.getQuantity() != null &&
            !oldProduct.getQuantity().equals(newProduct.getQuantity())) {
            logger.debug("Inventory quantity changed for product: {} from {} to {}", 
                        newProduct.getProductId(), oldProduct.getQuantity(), newProduct.getQuantity());
        }
        
        logger.debug("Handling inventory change notification for: {}", newProduct.getProductId());
    }
    
    private List<Product> executeSearch(SearchProductQuery query) {
        // Orchestrate different search strategies based on query criteria
        if (query.getTitle() != null && !query.getTitle().trim().isEmpty()) {
            return productService.searchProductsByTitle(query.getTitle());
        }
        
        if (query.getType() != null && !query.getType().trim().isEmpty()) {
            return productService.getProductsByType(query.getType());
        }
        
        // Default to getting all products and filtering
        return productService.getAllProducts();
    }
    
    private List<Product> applySearchBusinessRules(List<Product> products, SearchProductQuery query) {
        // Apply application-level filtering based on query criteria
        return products.stream()
            .filter(product -> matchesPriceFilter(product, query))
            .filter(product -> matchesStockFilter(product, query))
            .filter(product -> matchesRushOrderFilter(product, query))
            .toList();
    }
    
    private List<Product> applyPagination(List<Product> products, SearchProductQuery query) {
        // Simple in-memory pagination (in real app, this would be done at DB level)
        int start = query.getPage() * query.getSize();
        int end = Math.min(start + query.getSize(), products.size());
        
        if (start >= products.size()) {
            return List.of();
        }
        
        return products.subList(start, end);
    }
    
    private List<Product> applyApplicationFiltering(List<Product> products) {
        // Apply any application-level filtering rules
        // For example, hide inactive products, apply user permissions, etc.
        return products.stream()
            .filter(Product::isValid)
            .toList();
    }
    
    private boolean matchesPriceFilter(Product product, SearchProductQuery query) {
        if (query.getMinPrice() != null && product.getPrice() < query.getMinPrice()) {
            return false;
        }
        return query.getMaxPrice() == null || product.getPrice() <= query.getMaxPrice();
    }
    
    private boolean matchesStockFilter(Product product, SearchProductQuery query) {
        if (query.getInStock() != null) {
            return query.getInStock().equals(product.isInStock());
        }
        return true;
    }
    
    private boolean matchesRushOrderFilter(Product product, SearchProductQuery query) {
        if (query.getRushOrderSupported() != null) {
            return query.getRushOrderSupported().equals(product.supportsRushOrder());
        }
        return true;
    }
    
    // Audit and logging methods (orchestration concerns)
    
    private void logProductCreation(Product product, AddProductCommand command) {
        logger.info("Product created - ID: {}, Title: {}, Created by: {}, Source: {}", 
                   product.getProductId(), product.getTitle(), 
                   command.getCreatedBy(), command.getSource());
    }
    
    private void logProductUpdate(Product product, UpdateProductCommand command) {
        logger.info("Product updated - ID: {}, Title: {}, Updated by: {}, Source: {}", 
                   product.getProductId(), product.getTitle(), 
                   command.getUpdatedBy(), command.getSource());
    }
    
    private void logProductAccess(Product product) {
        logger.debug("Product accessed - ID: {}, Title: {}", 
                    product.getProductId(), product.getTitle());
    }
    
    private void logSearchActivity(SearchProductQuery query, int resultCount) {
        logger.debug("Search performed - Criteria: {}, Results: {}", query, resultCount);
    }
    
    private void logProductDeletion(Product product) {
        logger.info("Product deleted - ID: {}, Title: {}, Type: {}", 
                   product.getProductId(), product.getTitle(), product.getType());
    }
    
    private void validateProductCanBeDeleted(Product product) {
        // Additional application-level validation for deletion
        // For example: check if product is part of active orders, etc.
        logger.debug("Validating product can be deleted: ID={}, Title={}", 
                    product.getProductId(), product.getTitle());
        
        // Future: Add validation for active orders, cart items, etc.
    }
}
