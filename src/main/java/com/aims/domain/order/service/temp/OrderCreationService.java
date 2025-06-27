package com.aims.domain.order.service.temp;

import com.aims.domain.order.entity.Order;
import com.aims.domain.cart.entity.Cart;
import com.aims.domain.cart.entity.CartItem;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TEMPORARY FLATTENED SERVICE - Business logic extracted from PlaceOrder module
 * 
 * Source: PlaceOrder/Service/OrderService_PlaceOrder.java
 * Business Logic: Order creation, calculation, validation
 * 
 * PHASE 1 TASK 1.3: Flatten existing business logic
 */
@Service
public class OrderCreationService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderCreationService.class);
    private static final int VAT_RATE = 10; // 10% VAT as integer for entity
    
    /**
     * Create order from cart - BUSINESS LOGIC EXTRACTED FROM PlaceOrder
     * 
     * Original Business Rules from PlaceOrder/Entity/Order.createOrder():
     * - Calculate totals before and after VAT (10%)
     * - Set initial status as "pending"
     * - Create orderlines from cart items
     * - Apply VAT calculation: afterVAT = beforeVAT * 1.10
     */
    public Order createOrderFromCart(Cart cart, String customerId) {
        logger.info("Creating order from cart with {} items", cart.getCartItems().size());
        
        Order order = Order.builder()
            .customerId(customerId)
            .status(Order.OrderStatus.PENDING)
            .vatRate(VAT_RATE)
            .build();
        
        // Business logic flattened from PlaceOrder/Entity/Order.createOrder()
        long totalBeforeVAT = 0;
        
        for (CartItem cartItem : cart.getCartItems()) {
            long itemTotal = (long) cartItem.getQuantity() * cartItem.getProduct().getPrice();
            totalBeforeVAT += itemTotal;
        }
        
        long totalAfterVAT = totalBeforeVAT * (100 + VAT_RATE) / 100;
        
        order.setTotalBeforeVAT(totalBeforeVAT);
        order.setTotalAfterVAT(totalAfterVAT);
        
        logger.info("Order created with total before VAT: {}, after VAT: {}", 
            totalBeforeVAT, totalAfterVAT);
        
        return order;
    }
    
    /**
     * Validate order data - BUSINESS LOGIC EXTRACTED FROM PlaceOrder
     * 
     * Original Business Rules from PlaceOrder validation:
     * - Total must be positive
     * - Status must be valid  
     * - Customer ID required
     */
    public void validateOrder(Order order) {
        if (order.getTotalBeforeVAT() <= 0) {
            throw new IllegalArgumentException("Order total must be positive");
        }
        
        if (order.getCustomerId() == null || order.getCustomerId().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        
        if (order.getStatus() == null) {
            throw new IllegalArgumentException("Order status is required");
        }
        
        logger.info("Order validation passed for customer: {}", order.getCustomerId());
    }
    
    /**
     * Calculate delivery fee - BUSINESS LOGIC EXTRACTED 
     * Source: PlaceOrder/Service/DeliveryFeeCalculating.java
     * 
     * Original Business Rules:
     * - Base delivery fee: 30,000 VND
     * - Future: Rush order surcharge based on order type
     */
    public long calculateDeliveryFee() {
        // Base delivery fee from original DeliveryFeeCalculating logic
        return 30000L;
    }
}
