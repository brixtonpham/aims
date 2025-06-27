package com.aims.infrastructure.product;

/**
 * Product Service interface for domain layer
 * Provides product-related operations needed by order domain service
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 2 - Task 2.1
 */
public interface ProductService {
    
    /**
     * Check if product is available in requested quantity
     * 
     * @param productId The product ID
     * @param quantity The requested quantity
     * @return true if product is available in sufficient quantity
     */
    boolean isProductAvailable(Long productId, int quantity);
    
    /**
     * Get product weight for delivery calculation
     * 
     * @param productId The product ID
     * @return Product weight in kg
     */
    float getProductWeight(Long productId);
    
    /**
     * Get product price
     * 
     * @param productId The product ID
     * @return Product price
     */
    int getProductPrice(Long productId);
    
    /**
     * Update product quantity after order placement
     * 
     * @param productId The product ID
     * @param quantity The quantity to reduce
     */
    void reduceProductQuantity(Long productId, int quantity);
    
    /**
     * Restore product quantity after order cancellation
     * 
     * @param productId The product ID
     * @param quantity The quantity to restore
     */
    void restoreProductQuantity(Long productId, int quantity);
}
