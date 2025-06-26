package com.aims.domain.product.repository;

import com.aims.domain.product.entity.Product;
import java.util.List;
import java.util.Optional;

/**
 * Product repository interface following Domain-Driven Design principles.
 * Defines essential operations for Product domain without exposing infrastructure concerns.
 */
public interface ProductRepository {
    
    /**
     * Save a product entity
     * @param product the product to save
     * @return the saved product with generated ID
     */
    Product save(Product product);
    
    /**
     * Find a product by its ID
     * @param productId the product ID
     * @return Optional containing the product if found
     */
    Optional<Product> findById(Long productId);
    
    /**
     * Find all products
     * @return list of all products
     */
    List<Product> findAll();
    
    /**
     * Find products by type (book, cd, dvd)
     * @param type the product type
     * @return list of products of the specified type
     */
    List<Product> findByType(String type);
    
    /**
     * Find products by title containing search term
     * @param title the search term
     * @return list of products matching the search
     */
    List<Product> findByTitleContaining(String title);
    
    /**
     * Check if product exists by ID
     * @param productId the product ID
     * @return true if product exists
     */
    boolean existsById(Long productId);
    
    /**
     * Check if sufficient quantity is available for a product
     * @param productId the product ID
     * @param quantity the required quantity
     * @return true if sufficient quantity is available
     */
    boolean isAvailable(Long productId, int quantity);
    
    /**
     * Update product information
     * @param product the product to update
     * @return the updated product
     */
    Product update(Product product);
    
    /**
     * Delete a product by ID
     * @param productId the product ID to delete
     */
    void deleteById(Long productId);
    
    /**
     * Find products with low stock (quantity below threshold)
     * @param threshold the minimum quantity threshold
     * @return list of products with low stock
     */
    List<Product> findLowStockProducts(int threshold);
    
    /**
     * Get total product count
     * @return total number of products
     */
    long count();
}
