package com.aims.presentation.web;

import com.aims.presentation.dto.ApiResponse;
import com.aims.presentation.dto.OrderRequest;
import com.aims.presentation.dto.OrderResponse;
import com.aims.domain.order.entity.Order;
import com.aims.domain.order.repository.OrderRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * REST Controller for Order operations
 * Unified controller following RESTful design principles
 * Based on sequence diagrams: Place Order, Cancel Order, etc.
 * 
 * Note: This is a simplified implementation. Full functionality will be added
 * when OrderApplicationService is created in future phases.
 */
@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    private static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    private static final String NOT_IMPLEMENTED = "NOT_IMPLEMENTED";
    private static final String INVALID_ORDER_ID = "Invalid order ID";

    // OrderApplicationService will be injected here when available in future phases
    private final com.aims.application.order.OrderApplicationService orderApplicationService;
    private final OrderRepository orderRepository;
    
    public OrderController(com.aims.application.order.OrderApplicationService orderApplicationService,
                          OrderRepository orderRepository) {
        this.orderApplicationService = orderApplicationService;
        this.orderRepository = orderRepository;
    }

    /**
     * Place order
     * POST /api/orders
     * Based on "Place Order" sequence diagram: Cart → PlaceOrderController → DeliveryForm → Order → Invoice
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(@RequestBody OrderRequest request) {
        try {
            // Validate request
            validateOrderRequest(request);

            // Convert request to command - create delivery address command first
            com.aims.application.commands.PlaceOrderCommand.DeliveryAddressCommand deliveryAddressCommand = 
                com.aims.application.commands.PlaceOrderCommand.DeliveryAddressCommand.builder()
                    .streetAddress(request.getDeliveryAddress())
                    .city(request.getDeliveryCity() != null ? request.getDeliveryCity() : "Default City")
                    .state(request.getDeliveryState() != null ? request.getDeliveryState() : "Default State")
                    .postalCode(request.getDeliveryPostalCode() != null ? request.getDeliveryPostalCode() : "00000")
                    .country(request.getDeliveryCountry() != null ? request.getDeliveryCountry() : "Vietnam")
                    .build();

            // Create command using the correct package and builder
            com.aims.application.commands.PlaceOrderCommand command = 
                com.aims.application.commands.PlaceOrderCommand.builder()
                    .customerId(String.valueOf(request.getCustomerId()))
                    .customerName(request.getCustomerName() != null ? request.getCustomerName() : "Default Customer")
                    .customerEmail(request.getCustomerEmail() != null ? request.getCustomerEmail() : "customer@example.com")
                    .customerPhone(request.getCustomerPhone() != null ? request.getCustomerPhone() : "0123456789")
                    .paymentMethod(request.getPaymentMethod())
                    .rushOrder(request.isRushOrder())
                    .deliveryAddress(deliveryAddressCommand)
                    .deliveryInstructions(request.getDeliveryInstructions())
                    .orderItems(convertOrderItemsToCommands(request.getOrderItems()))
                    .build();

            // Execute order placement
            com.aims.application.dto.order.OrderCreationResult result = orderApplicationService.placeOrder(command);

            if (result.isSuccess()) {
                OrderResponse response = new OrderResponse();
                response.setOrderId(Long.parseLong(result.getOrderId()));
                response.setOrderStatus(result.getOrderStatus());
                response.setTotalAmount(java.math.BigDecimal.valueOf(result.getTotalAmount()));
                
                return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Order placed successfully", response));
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(result.getMessage(), result.getErrorCode()));
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), VALIDATION_ERROR));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to place order", INTERNAL_ERROR));
        }
    }

    /**
     * Place rush order
     * POST /api/orders/rush
     * Based on rush order workflow from sequence diagrams
     */
    @PostMapping("/rush")
    public ResponseEntity<ApiResponse<OrderResponse>> placeRushOrder(@RequestBody OrderRequest request) {
        try {
            // Validate request
            validateOrderRequest(request);
            
            if (!request.isRushOrder()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Rush order flag must be set for rush orders", VALIDATION_ERROR));
            }

            // For now, return not implemented
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(ApiResponse.error("Rush order functionality not implemented yet", NOT_IMPLEMENTED));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), VALIDATION_ERROR));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to place rush order", INTERNAL_ERROR));
        }
    }

    /**
     * Get order details
     * GET /api/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable("orderId") Long orderId) {
        try {
            // Validate order ID
            if (orderId == null || orderId <= 0) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(INVALID_ORDER_ID, VALIDATION_ERROR));
            }

            // Delegate to application service
            com.aims.application.dto.order.OrderTrackingResult trackingResult = 
                orderApplicationService.trackOrder(String.valueOf(orderId));

            if (trackingResult.isSuccess()) {
                OrderResponse response = new OrderResponse();
                response.setOrderId(orderId);
                response.setOrderStatus(trackingResult.getCurrentStatus());
                
                return ResponseEntity.ok(ApiResponse.success(response));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Order not found", "ORDER_NOT_FOUND"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to get order", INTERNAL_ERROR));
        }
    }

    /**
     * List orders for customer
     * GET /api/orders?customerId={customerId}
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersForCustomer(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        try {
            // Validate customer ID if provided
            if (customerId != null && customerId <= 0) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Valid customer ID is required", VALIDATION_ERROR));
            }
            
            // Customer ID is required for listing orders
            if (customerId == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Customer ID is required for listing orders", VALIDATION_ERROR));
            }
            
            // Validate pagination parameters
            if (page < 0) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Page number cannot be negative", VALIDATION_ERROR));
            }
            if (size <= 0 || size > 100) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Page size must be between 1 and 100", VALIDATION_ERROR));
            }
            
            // Get orders from repository using the available method
            List<Order> orders = orderRepository.findByCustomerId(customerId);
            
            // Apply pagination manually (since we don't have Pageable support yet)
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, orders.size());
            
            List<Order> paginatedOrders;
            if (startIndex >= orders.size()) {
                paginatedOrders = new java.util.ArrayList<>();
            } else {
                paginatedOrders = orders.subList(startIndex, endIndex);
            }
            
            // Convert to OrderResponse DTOs
            List<OrderResponse> ordersList = paginatedOrders.stream()
                .map(this::convertToOrderResponse)
                .toList();
            
            log.info("Retrieved {} orders for customer: {} (page: {}, size: {})", 
                    ordersList.size(), customerId, page, size);
            
            return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", ordersList));

        } catch (Exception e) {
            log.error("Failed to get orders for customer: {}", customerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to get orders", INTERNAL_ERROR));
        }
    }

    /**
     * Cancel order
     * DELETE /api/orders/{orderId}
     * Based on "Cancel Order" sequence diagram from CancelOrderController workflow
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(@PathVariable("orderId") Long orderId) {
        try {
            // Validate order ID
            if (orderId == null || orderId <= 0) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(INVALID_ORDER_ID, VALIDATION_ERROR));
            }

            // Create cancel order command
            com.aims.application.commands.CancelOrderCommand command = 
                com.aims.application.commands.CancelOrderCommand.builder()
                    .orderId(String.valueOf(orderId))
                    .customerId("1") // Simplified for Phase 4 - will be enhanced with proper customer identification
                    .cancellationReason("Customer request") // Simplified for Phase 4 - will be enhanced
                    .processRefund(true)
                    .build();

            // Delegate to application service
            com.aims.application.dto.order.CancellationResult result = 
                orderApplicationService.cancelOrder(command);

            if (result.isSuccess()) {
                return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", null));
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(result.getMessage(), result.getErrorCode()));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to cancel order", INTERNAL_ERROR));
        }
    }

    /**
     * Process order (admin/system operation)
     * PUT /api/orders/{orderId}/process
     */
    @PutMapping("/{orderId}/process")
    public ResponseEntity<ApiResponse<OrderResponse>> processOrder(@PathVariable("orderId") Long orderId) {
        try {
            // Validate order ID
            if (orderId == null || orderId <= 0) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(INVALID_ORDER_ID, VALIDATION_ERROR));
            }

            // For now, return not implemented
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(ApiResponse.error("Process order functionality not implemented yet", NOT_IMPLEMENTED));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to process order", INTERNAL_ERROR));
        }
    }

    /**
     * Place order using Command Pattern (Phase 3)
     * POST /api/orders/v2
     * Uses the new CommandBus implementation
     */
    @PostMapping("/v2")
    public ResponseEntity<ApiResponse<com.aims.application.command.order.OrderCreationResult>> placeOrderV2(
            @RequestBody OrderRequest request) {
        try {
            // Validate request
            validateOrderRequest(request);
            
            // Convert to domain objects
            java.util.List<com.aims.domain.order.entity.OrderItem> items = convertToOrderItems(request);
            com.aims.domain.order.entity.DeliveryInfo deliveryInfo = convertToDeliveryInfo(request);
            
            // Use CommandBus to place order
            com.aims.application.command.order.OrderCreationResult result = 
                orderApplicationService.placeOrderWithCommandBus(
                    String.valueOf(request.getCustomerId()),
                    items,
                    deliveryInfo
                );
            
            return ResponseEntity.ok(ApiResponse.success("Order placed successfully using CommandBus", result));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), VALIDATION_ERROR));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to place order: " + e.getMessage(), INTERNAL_ERROR));
        }
    }
    
    /**
     * Cancel order using Command Pattern (Phase 3)
     * DELETE /api/orders/{orderId}/v2
     * Uses the new CommandBus implementation
     */
    @DeleteMapping("/{orderId}/v2")
    public ResponseEntity<ApiResponse<com.aims.application.command.order.CancellationResult>> cancelOrderV2(
            @PathVariable String orderId,
            @RequestParam String requestedBy,
            @RequestParam(required = false, defaultValue = "Customer request") String reason) {
        try {
            // Validate inputs
            if (orderId == null || orderId.trim().isEmpty()) {
                throw new IllegalArgumentException("Order ID is required");
            }
            if (requestedBy == null || requestedBy.trim().isEmpty()) {
                throw new IllegalArgumentException("RequestedBy is required");
            }
            
            // Use CommandBus to cancel order
            com.aims.application.command.order.CancellationResult result = 
                orderApplicationService.cancelOrderWithCommandBus(orderId, requestedBy, reason);
            
            return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully using CommandBus", result));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), VALIDATION_ERROR));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to cancel order: " + e.getMessage(), INTERNAL_ERROR));
        }
    }
    
    // Helper methods to convert request objects to domain objects
    @SuppressWarnings("unused")
    private java.util.List<com.aims.domain.order.entity.OrderItem> convertToOrderItems(OrderRequest request) {
        // This is a simplified conversion - in real implementation, you'd need proper mapping
        return java.util.Collections.emptyList(); // Placeholder
    }
    
    @SuppressWarnings("unused")
    private com.aims.domain.order.entity.DeliveryInfo convertToDeliveryInfo(OrderRequest request) {
        // This is a simplified conversion - in real implementation, you'd need proper mapping
        return new com.aims.domain.order.entity.DeliveryInfo(); // Placeholder
    }

    /**
     * Convert presentation layer OrderItemRequest to application layer OrderItemCommand
     */
    private java.util.List<com.aims.application.commands.PlaceOrderCommand.OrderItemCommand> convertOrderItemsToCommands(
            java.util.List<com.aims.presentation.dto.OrderItemRequest> orderItems) {
        
        if (orderItems == null || orderItems.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        
        return orderItems.stream()
            .map(item -> com.aims.application.commands.PlaceOrderCommand.OrderItemCommand.builder()
                .productId(String.valueOf(item.getProductId()))
                .productName(item.getProductTitle())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice() != null ? item.getUnitPrice().doubleValue() : 0.0)
                .variant(item.getProductType()) // Use product type as variant
                .build())
            .toList();
    }

    // Private helper methods

    private void validateOrderRequest(OrderRequest request) {
        validateBasicOrderFields(request);
        validateOrderItems(request.getOrderItems());
    }
    
    private void validateBasicOrderFields(OrderRequest request) {
        if (request.getCustomerId() == null || request.getCustomerId() <= 0) {
            throw new IllegalArgumentException("Valid customer ID is required");
        }
        if (request.getDeliveryAddress() == null || request.getDeliveryAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Delivery address is required");
        }
        if (request.getPaymentMethod() == null || request.getPaymentMethod().trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method is required");
        }
    }
    
    private void validateOrderItems(java.util.List<com.aims.presentation.dto.OrderItemRequest> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return; // Order items are optional for some endpoints
        }
        
        for (com.aims.presentation.dto.OrderItemRequest item : orderItems) {
            validateSingleOrderItem(item);
        }
    }
    
    private void validateSingleOrderItem(com.aims.presentation.dto.OrderItemRequest item) {
        if (item.getProductId() == null || item.getProductId() <= 0) {
            throw new IllegalArgumentException("Valid product ID is required for all order items");
        }
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new IllegalArgumentException("Valid quantity is required for all order items");
        }
        if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valid unit price is required for all order items");
        }
    }
    
    /**
     * Convert Order domain entity to OrderResponse DTO
     */
    private OrderResponse convertToOrderResponse(Order order) {
        if (order == null) {
            return null;
        }
        
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getOrderId());
        response.setCustomerId(Long.valueOf(order.getCustomerId())); // Convert String to Long
        response.setOrderStatus(order.getStatus() != null ? order.getStatus().toString() : "UNKNOWN");
        
        // Convert totalAfterVAT from Long to BigDecimal
        if (order.getTotalAfterVAT() != null) {
            response.setTotalAmount(new java.math.BigDecimal(order.getTotalAfterVAT()));
        }
        
        // Set delivery address if available - using deliveryInfo
        if (order.getDeliveryInfo() != null && order.getDeliveryInfo().getAddress() != null) {
            response.setDeliveryAddress(order.getDeliveryInfo().getAddress());
        }
        
        // Set timestamps if available
        if (order.getCreatedAt() != null) {
            response.setOrderDate(order.getCreatedAt());
        }
        
        // Convert order items if available
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            List<com.aims.presentation.dto.OrderItemResponse> orderItems = order.getOrderItems().stream()
                .map(this::convertToOrderItemResponse)
                .toList();
            response.setOrderItems(orderItems);
        }
        
        return response;
    }
    
    /**
     * Convert OrderItem domain entity to OrderItemResponse DTO
     */
    private com.aims.presentation.dto.OrderItemResponse convertToOrderItemResponse(
            com.aims.domain.order.entity.OrderItem orderItem) {
        
        if (orderItem == null) {
            return null;
        }
        
        com.aims.presentation.dto.OrderItemResponse response = new com.aims.presentation.dto.OrderItemResponse();
        response.setOrderItemId(orderItem.getOrderItemId());
        response.setProductId(orderItem.getProductId());
        response.setProductTitle(orderItem.getProductTitle());
        response.setQuantity(orderItem.getQuantity());
        
        // Convert prices from Integer/long to BigDecimal
        if (orderItem.getUnitPrice() != null) {
            response.setUnitPrice(new java.math.BigDecimal(orderItem.getUnitPrice()));
        }
        // totalPrice is a primitive long, so it's never null
        response.setTotalPrice(java.math.BigDecimal.valueOf(orderItem.getTotalPrice()));
        
        return response;
    }
}
