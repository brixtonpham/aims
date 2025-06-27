package com.aims.infrastructure.product;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple implementation of ProductService
 * For Phase 2, this provides basic functionality to support order domain service
 * Later phases will enhance this with proper repository integration
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 2 - Task 2.1
 */
@Service
public class ProductServiceImpl implements ProductService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
    
    @Override
    public boolean isProductAvailable(Long productId, int quantity) {
        logger.debug("Checking availability for product {} with quantity {}", productId, quantity);
        
        // For Phase 2: Simple validation - assume products are available
        // In later phases, this will check actual inventory
        if (productId == null || quantity <= 0) {
            return false;
        }
        
        // Mock logic: Products with ID > 1000 are out of stock
        if (productId > 1000) {
            logger.warn("Product {} is out of stock", productId);
            return false;
        }
        
        // Mock logic: Quantities > 100 are not available
        if (quantity > 100) {
            logger.warn("Insufficient stock for product {}, requested: {}", productId, quantity);
            return false;
        }
        
        logger.debug("Product {} is available with quantity {}", productId, quantity);
        return true;
    }
    
    @Override
    public float getProductWeight(Long productId) {
        logger.debug("Getting weight for product {}", productId);
        
        if (productId == null) {
            return 0.0f;
        }
        
        // Mock logic: Weight calculation based on product ID
        // In later phases, this will fetch from actual product repository
        float weight = 0.5f + (productId % 10) * 0.1f; // Between 0.5kg and 1.4kg
        
        logger.debug("Product {} weight: {}kg", productId, weight);
        return weight;
    }
    
    @Override
    public int getProductPrice(Long productId) {
        logger.debug("Getting price for product {}", productId);
        
        if (productId == null) {
            return 0;
        }
        
        // Mock logic: Price calculation based on product ID
        // In later phases, this will fetch from actual product repository
        int price = 10000 + (int)(productId * 1000); // Base price + variable
        
        logger.debug("Product {} price: {}", productId, price);
        return price;
    }
    
    @Override
    public void reduceProductQuantity(Long productId, int quantity) {
        logger.info("Reducing quantity for product {} by {}", productId, quantity);
        
        if (productId == null || quantity <= 0) {
            logger.warn("Invalid parameters for reducing product quantity: productId={}, quantity={}", 
                productId, quantity);
            return;
        }
        
        // For Phase 2: Just log the action
        // In later phases, this will update actual inventory
        logger.info("Product {} quantity reduced by {}", productId, quantity);
    }
    
    @Override
    public void restoreProductQuantity(Long productId, int quantity) {
        logger.info("Restoring quantity for product {} by {}", productId, quantity);
        
        if (productId == null || quantity <= 0) {
            logger.warn("Invalid parameters for restoring product quantity: productId={}, quantity={}", 
                productId, quantity);
            return;
        }
        
        // For Phase 2: Just log the action
        // In later phases, this will update actual inventory
        logger.info("Product {} quantity restored by {}", productId, quantity);
    }
}
