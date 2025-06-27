package com.aims.application.command.handler;

import com.aims.application.command.CommandHandler;
import com.aims.application.command.order.PlaceOrderCommand;
import com.aims.application.command.order.OrderCreationResult;
import com.aims.domain.order.service.OrderDomainService;
import com.aims.domain.order.service.event.DomainEventPublisher;
import com.aims.domain.order.service.event.OrderCreatedEvent;
import com.aims.domain.order.dto.CreateOrderRequest;
import com.aims.domain.order.entity.Order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Handler for PlaceOrderCommand
 */
@Component
public class PlaceOrderCommandHandler implements CommandHandler<PlaceOrderCommand, OrderCreationResult> {
    
    private static final Logger logger = LoggerFactory.getLogger(PlaceOrderCommandHandler.class);
    
    private final OrderDomainService orderDomainService;
    private final DomainEventPublisher eventPublisher;
    
    @Autowired
    public PlaceOrderCommandHandler(OrderDomainService orderDomainService, 
                                   DomainEventPublisher eventPublisher) {
        this.orderDomainService = orderDomainService;
        this.eventPublisher = eventPublisher;
    }
    
    @Override
    public OrderCreationResult handle(PlaceOrderCommand command) {
        logger.info("Handling place order command for customer: {}", command.getCustomerId());
        
        try {
            // Convert command to domain request
            CreateOrderRequest request = new CreateOrderRequest();
            request.setCustomerId(command.getCustomerId());
            request.setOrderItems(convertToOrderItemRequests(command.getItems()));
            request.setDeliveryInfo(convertToDeliveryInfoRequest(command.getDeliveryInfo()));
            request.setIsRushOrder(command.isRushOrder());
            
            // Create order through domain service
            Order order = orderDomainService.createOrder(request);
            
            // Publish order created event
            OrderCreatedEvent event = new OrderCreatedEvent(
                order.getOrderId(),
                order.getCustomerId(),
                order.getTotalAfterVAT()
            );
            eventPublisher.publish(event);
            
            logger.info("Order created successfully: {}", order.getOrderId());
            
            return OrderCreationResult.builder()
                .success(true)
                .orderId(order.getOrderId().toString())
                .totalAmount(order.getTotalAfterVAT())
                .message("Order created successfully")
                .build();
                
        } catch (Exception e) {
            logger.error("Failed to place order for customer: {}", command.getCustomerId(), e);
            
            return OrderCreationResult.builder()
                .success(false)
                .message("Failed to create order: " + e.getMessage())
                .build();
        }
    }
    
    @Override
    public boolean supports(String commandType) {
        return "PLACE_ORDER".equals(commandType);
    }
    
    @Override
    public int getPriority() {
        return 10;
    }
    
    private List<CreateOrderRequest.OrderItemRequest> convertToOrderItemRequests(
            List<com.aims.domain.order.entity.OrderItem> items) {
        return items.stream()
            .map(item -> {
                var request = new CreateOrderRequest.OrderItemRequest();
                request.setProductId(item.getProductId());
                request.setProductTitle(item.getProductTitle());
                request.setQuantity(item.getQuantity());
                request.setUnitPrice(item.getUnitPrice());
                return request;
            })
            .toList();
    }
    
    private CreateOrderRequest.DeliveryInfoRequest convertToDeliveryInfoRequest(
            com.aims.domain.order.entity.DeliveryInfo deliveryInfo) {
        return new CreateOrderRequest.DeliveryInfoRequest(
            deliveryInfo.getName(),
            deliveryInfo.getPhone(),
            deliveryInfo.getEmail(),
            deliveryInfo.getAddress(),
            deliveryInfo.getProvince()
        );
    }
}
