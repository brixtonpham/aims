package com.aims.domain.order.model;

import com.aims.domain.order.entity.OrderItem;
import com.aims.domain.order.entity.DeliveryInfo;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Request for creating an order using Command pattern
 */
@Data
@Builder
public class CreateOrderRequest {
    private String customerId;
    private List<OrderItem> items;
    private DeliveryInfo deliveryInfo;
    private boolean isRushOrder;
}
