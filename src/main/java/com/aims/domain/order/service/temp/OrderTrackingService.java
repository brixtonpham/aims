package com.aims.domain.order.service.temp;

import com.aims.domain.order.entity.Order;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * TEMPORARY FLATTENED SERVICE - Business logic extracted from PlaceOrder module
 * 
 * Source: PlaceOrder/Service/ProcessTrackingInfo.java
 * Business Logic: Order tracking, status information, delivery details
 * 
 * PHASE 1 TASK 1.3: Flatten existing tracking business logic
 */
@Service
public class OrderTrackingService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderTrackingService.class);
    
    /**
     * Get order tracking information - BUSINESS LOGIC EXTRACTED
     * 
     * Original Business Rules from PlaceOrder/Service/ProcessTrackingInfo:
     * - Compile order basic information
     * - Include current status and timestamps
     * - Add order details (items, amounts, delivery info)
     * - Format for customer consumption
     */
    public Map<String, Object> getOrderTrackingInfo(Order order) {
        logger.info("Getting tracking info for order: {}", order.getOrderId());
        
        Map<String, Object> trackingInfo = new HashMap<>();
        
        // Basic tracking information (from original ProcessTrackingInfo)
        trackingInfo.put("order_id", order.getOrderId());
        trackingInfo.put("current_status", order.getStatus().getDisplayName());
        trackingInfo.put("order_date", order.getOrderTime());
        
        // Order details (flattened from original logic)
        Map<String, Object> orderDetails = new HashMap<>();
        orderDetails.put("total_amount", order.getTotalAfterVAT());
        orderDetails.put("payment_method", order.getPaymentMethod());
        
        // Future enhancement: delivery address and items
        // Will be added in Phase 2 when relationships are established
        orderDetails.put("delivery_address", "To be implemented in Phase 2");
        orderDetails.put("items", "To be implemented in Phase 2");
        
        trackingInfo.put("order_details", orderDetails);
        
        logger.info("Tracking info compiled for order: {}", order.getOrderId());
        return trackingInfo;
    }
    
    /**
     * Get order status display information - BUSINESS LOGIC EXTRACTED
     * 
     * Original Business Rules: Convert internal status to customer-friendly format
     */
    public String getStatusDisplayName(Order order) {
        return order.getStatus().getDisplayName();
    }
    
    /**
     * Check if order is trackable - BUSINESS LOGIC EXTRACTED
     * 
     * Business Rules:
     * - Only confirmed, shipped, or delivered orders are trackable
     * - Cancelled orders show cancellation info
     * - Pending orders show waiting status
     */
    public boolean isOrderTrackable(Order order) {
        Order.OrderStatus status = order.getStatus();
        return status == Order.OrderStatus.CONFIRMED || 
               status == Order.OrderStatus.SHIPPED || 
               status == Order.OrderStatus.DELIVERED;
    }
    
    /**
     * Get estimated delivery information - BUSINESS LOGIC EXTRACTED
     * 
     * Business Rules from original tracking logic:
     * - Confirmed orders: 2-3 business days
     * - Shipped orders: Based on shipping method
     * - Delivered orders: Show delivery date
     */
    public String getEstimatedDelivery(Order order) {
        switch (order.getStatus()) {
            case CONFIRMED:
                return "2-3 business days";
            case SHIPPED:
                return "1-2 business days";
            case DELIVERED:
                return "Delivered";
            default:
                return "Not available";
        }
    }
}
