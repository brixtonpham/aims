package com.aims.domain.order.service;

import com.aims.domain.order.entity.Order;

import com.aims.domain.order.entity.Order.OrderStatus;
import com.aims.domain.order.entity.DeliveryInfo;
import com.aims.domain.order.entity.DeliveryInfo.DeliveryType;
import com.aims.domain.order.repository.OrderRepository;
import com.aims.domain.order.dto.CreateOrderRequest;
import com.aims.domain.order.dto.OrderTotal;
import com.aims.domain.order.dto.OrderValidationResult;
import com.aims.domain.order.exception.OrderDomainException;
import com.aims.domain.order.service.event.OrderCreatedEvent;
import com.aims.domain.order.service.event.OrderCancelledEvent;
import com.aims.domain.order.service.event.OrderStatusChangedEvent;
import com.aims.domain.order.service.event.DomainEventPublisher;
import com.aims.infrastructure.product.ProductService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;


/**
 * OrderDomainServiceImpl - Implementation of Order Domain Service
 * 
 * This implementation contains all core business logic extracted from existing 
 * PlaceOrder, CancelOrder, and related services, following Clean Architecture principles.
 * 
 * Business Rules Implemented:
 * - Order creation with comprehensive validation
 * - VAT calculation (10% default)
 * - Delivery fee calculation based on province and weight
 * - Rush order surcharge handling
 * - Order cancellation eligibility rules
 * - Status transition validation
 * - Product availability checking
 * - Business constraint validation
 * 
 * @author AIMS Development Team
 * @version 1.0
 * @since Phase 2 - Task 2.1
 */
@Service
@Transactional
public class OrderDomainServiceImpl implements OrderDomainService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderDomainServiceImpl.class);
    
    // Business Constants
    private static final int DEFAULT_VAT_RATE = 10; // 10%
    private static final int MINIMUM_ORDER_AMOUNT = 10000; // 10,000 VND
    private static final long FREE_SHIPPING_THRESHOLD = 100000L; // 100,000 VND
    private static final int RUSH_ORDER_SURCHARGE = 10000; // 10,000 VND
    private static final int DELIVERY_DISCOUNT = 25000; // 25,000 VND discount for orders > 100k
    
    // Province Constants
    private static final String HANOI = "Hanoi";
    private static final String HO_CHI_MINH_CITY = "Ho Chi Minh City";
    
    // Major city delivery fees
    private static final int MAJOR_CITY_BASE_FEE = 22000; // 22,000 VND
    private static final int MAJOR_CITY_WEIGHT_THRESHOLD = 3; // 3kg
    private static final int OTHER_PROVINCE_BASE_FEE = 30000; // 30,000 VND
    private static final int OTHER_PROVINCE_WEIGHT_THRESHOLD = 1; // 0.5kg
    private static final int ADDITIONAL_WEIGHT_FEE = 2500; // 2,500 VND per 0.5kg
    
    // Dependencies
    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final DomainEventPublisher eventPublisher;
    
    @Autowired
    public OrderDomainServiceImpl(
            OrderRepository orderRepository,
            ProductService productService,
            DomainEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.eventPublisher = eventPublisher;
    }
    
    @Override
    public Order createOrder(CreateOrderRequest request) throws OrderDomainException {
        logger.info("Creating order for customer: {}", request.getCustomerId());
        
        try {
            // Step 1: Validate request
            OrderValidationResult validation = validateOrder(request);
            if (!validation.isValid()) {
                throw new OrderDomainException("Order validation failed: " + validation.getErrorMessage());
            }
            
            // Step 2: Create order entity with initial data
            Order order = Order.builder()
                .customerId(request.getCustomerId())
                .status(Order.OrderStatus.PENDING)
                .vatRate(DEFAULT_VAT_RATE)
                .paymentMethod(request.getPaymentMethod())
                .isRushOrder(request.isRushOrder())
                .orderTime(LocalDateTime.now())
                .build();
            
            // Step 3: Add order items with business validation
            for (CreateOrderRequest.OrderItemRequest itemRequest : request.getOrderItems()) {
                addOrderItemWithValidation(order, itemRequest);
            }
            
            // Step 4: Create and calculate delivery information
            DeliveryInfo deliveryInfo = createDeliveryInfo(request, order);
            order.setDeliveryInfo(deliveryInfo);
            
            // Step 5: Calculate final totals
            order.recalculateTotals();
            applyDeliveryFeeToTotal(order);
            
            // Step 6: Apply business rules
            applyBusinessRules(order, request);
            
            // Step 7: Save order
            Order savedOrder = orderRepository.save(order);
            
            // Step 8: Publish domain event
            eventPublisher.publish(new OrderCreatedEvent(savedOrder.getOrderId(), 
                savedOrder.getCustomerId(), savedOrder.getTotalAfterVAT()));
            
            logger.info("Order created successfully with ID: {}", savedOrder.getOrderId());
            return savedOrder;
            
        } catch (Exception e) {
            logger.error("Error creating order for customer {}: {}", request.getCustomerId(), e.getMessage());
            if (e instanceof OrderDomainException) {
                throw e;
            }
            throw new OrderDomainException("Failed to create order: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Order getOrderById(String orderId) throws OrderDomainException {
        logger.debug("Retrieving order: {}", orderId);
        
        validateOrderId(orderId);
        
        return orderRepository.findById(Long.parseLong(orderId))
            .orElseThrow(() -> new OrderDomainException("Order not found: " + orderId));
    }
    
    @Override
    public void cancelOrder(String orderId) throws OrderDomainException {
        logger.info("Cancelling order: {}", orderId);
        
        try {
            validateOrderId(orderId);
            
            Order order = getOrderById(orderId);
            
            // Business rule: Check if order can be cancelled
            if (!canCancelOrderByStatus(order)) {
                throw new OrderDomainException(
                    "Order cannot be cancelled. Only pending orders can be cancelled. Current status: " + 
                    order.getStatus().getDisplayName());
            }
            
            // Apply cancellation business logic
            order.cancel(); // This will change status to CANCELLED
            
            Order savedOrder = orderRepository.save(order);
            
            // Publish cancellation event
            eventPublisher.publish(new OrderCancelledEvent(savedOrder.getOrderId(), 
                savedOrder.getCustomerId(), savedOrder.getTotalAfterVAT()));
            
            logger.info("Order {} cancelled successfully", orderId);
            
        } catch (Exception e) {
            logger.error("Error cancelling order {}: {}", orderId, e.getMessage());
            if (e instanceof OrderDomainException) {
                throw e;
            }
            throw new OrderDomainException("Failed to cancel order: " + e.getMessage(), e);
        }
    }
    
    @Override
    public OrderStatus getOrderStatus(String orderId) throws OrderDomainException {
        logger.debug("Getting status for order: {}", orderId);
        
        Order order = getOrderById(orderId);
        return order.getStatus();
    }
    
    @Override
    public List<Order> getOrdersByCustomer(String customerId) throws OrderDomainException {
        logger.debug("Retrieving orders for customer: {}", customerId);
        
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new OrderDomainException("Customer ID cannot be null or empty");
        }
        
        try {
            // Business rule: Return orders sorted by creation date (newest first)
            List<Order> orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
            
            logger.debug("Found {} orders for customer: {}", orders.size(), customerId);
            return orders;
            
        } catch (Exception e) {
            logger.error("Error retrieving orders for customer {}: {}", customerId, e.getMessage());
            throw new OrderDomainException("Failed to retrieve customer orders: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean canModifyOrder(String orderId) throws OrderDomainException {
        logger.debug("Checking if order {} can be modified", orderId);
        
        Order order = getOrderById(orderId);
        
        // Business rules for modification
        boolean canModify = order.getStatus() == OrderStatus.PENDING && 
                           !isOrderTooOld(order);
        
        logger.debug("Order {} can be modified: {}", orderId, canModify);
        return canModify;
    }
    
    @Override
    public void updateOrderStatus(String orderId, OrderStatus newStatus, String reason) throws OrderDomainException {
        logger.info("Updating order {} status to {} with reason: {}", orderId, newStatus, reason);
        
        try {
            Order order = getOrderById(orderId);
            OrderStatus oldStatus = order.getStatus();
            
            // Validate status transition
            if (!isValidStatusTransition(oldStatus, newStatus)) {
                throw new OrderDomainException(
                    String.format("Invalid status transition from %s to %s", 
                        oldStatus.getDisplayName(), newStatus.getDisplayName()));
            }
            
            // Apply status change using entity business logic
            switch (newStatus) {
                case CONFIRMED:
                    order.confirm();
                    break;
                case SHIPPED:
                    order.ship();
                    break;
                case DELIVERED:
                    order.deliver();
                    break;
                case CANCELLED:
                    order.cancel();
                    break;
                default:
                    throw new OrderDomainException("Unsupported status transition: " + newStatus);
            }
            
            Order savedOrder = orderRepository.save(order);
            
            // Publish status change event
            eventPublisher.publish(new OrderStatusChangedEvent(savedOrder.getOrderId(), 
                oldStatus, newStatus, reason));
            
            logger.info("Order {} status updated from {} to {}", orderId, oldStatus, newStatus);
            
        } catch (Exception e) {
            logger.error("Error updating order {} status: {}", orderId, e.getMessage());
            if (e instanceof OrderDomainException) {
                throw e;
            }
            throw new OrderDomainException("Failed to update order status: " + e.getMessage(), e);
        }
    }
    
    @Override
    public OrderTotal calculateOrderTotal(CreateOrderRequest request) throws OrderDomainException {
        logger.debug("Calculating order total for customer: {}", request.getCustomerId());
        
        try {
            long subtotal = 0L;
            float totalWeight = 0.0f;
            
            // Calculate items subtotal and weight
            for (CreateOrderRequest.OrderItemRequest item : request.getOrderItems()) {
                // Validate product availability
                if (!productService.isProductAvailable(item.getProductId(), item.getQuantity())) {
                    throw new OrderDomainException("Product not available: " + item.getProductId());
                }
                
                long itemTotal = (long) item.getQuantity() * item.getUnitPrice();
                subtotal += itemTotal;
                
                // Get product weight for delivery calculation
                float productWeight = productService.getProductWeight(item.getProductId());
                totalWeight += productWeight * item.getQuantity();
            }
            
            // Calculate VAT
            long vatAmount = subtotal * DEFAULT_VAT_RATE / 100;
            long totalWithVat = subtotal + vatAmount;
            
            // Calculate delivery fee
            int deliveryFee = calculateDeliveryFee(request.getDeliveryInfo().getProvince(), 
                totalWeight, request.isRushOrder(), subtotal);
                
            long grandTotal = totalWithVat + deliveryFee;
            
            OrderTotal total = OrderTotal.builder()
                .subtotal((double) subtotal)
                .taxAmount((double) vatAmount)
                .deliveryFee((double) deliveryFee)
                .totalAmount((double) grandTotal)
                .currency("VND")
                .build();
                
            logger.debug("Order total calculated: {}", total);
            return total;
            
        } catch (Exception e) {
            logger.error("Error calculating order total: {}", e.getMessage());
            if (e instanceof OrderDomainException) {
                throw e;
            }
            throw new OrderDomainException("Failed to calculate order total: " + e.getMessage(), e);
        }
    }
    
    @Override
    public OrderValidationResult validateOrder(CreateOrderRequest request) {
        logger.debug("Validating order request");
        
        List<String> errors = new ArrayList<>();
        
        try {
            // Validate customer
            if (request.getCustomerId() == null || request.getCustomerId().trim().isEmpty()) {
                errors.add("Customer ID is required");
            }
            
            // Validate order items
            if (request.getOrderItems() == null || request.getOrderItems().isEmpty()) {
                errors.add("Order must contain at least one item");
            } else {
                validateOrderItems(request.getOrderItems(), errors);
            }
            
            // Validate delivery information
            if (request.getDeliveryInfo() == null) {
                errors.add("Delivery information is required");
            } else {
                validateDeliveryInfo(request.getDeliveryInfo(), errors);
            }
            
            // Validate payment method
            if (request.getPaymentMethod() == null || request.getPaymentMethod().trim().isEmpty()) {
                errors.add("Payment method is required");
            }
            
            // Calculate and validate minimum order amount
            if (!errors.isEmpty()) {
                // Skip business validation if basic validation fails
                return OrderValidationResult.invalid(String.join(", ", errors));
            }
            
            OrderTotal total = calculateOrderTotal(request);
            if (total.getSubtotal() < MINIMUM_ORDER_AMOUNT) {
                errors.add("Order amount must be at least " + MINIMUM_ORDER_AMOUNT + " VND");
            }
            
        } catch (Exception e) {
            logger.error("Error during order validation: {}", e.getMessage());
            errors.add("Validation error: " + e.getMessage());
        }
        
        boolean isValid = errors.isEmpty();
        String errorMessage = isValid ? null : String.join(", ", errors);
        
        logger.debug("Order validation result: valid={}, errors={}", isValid, errorMessage);
        return isValid ? OrderValidationResult.valid() : OrderValidationResult.invalid(errorMessage);
    }
    
    // Private helper methods implementing specific business logic
    
    private void addOrderItemWithValidation(Order order, CreateOrderRequest.OrderItemRequest itemRequest) 
            throws OrderDomainException {
        
        // Business validation: Check product availability
        if (!productService.isProductAvailable(itemRequest.getProductId(), itemRequest.getQuantity())) {
            throw new OrderDomainException("Insufficient stock for product: " + itemRequest.getProductId());
        }
        
        // Business validation: Validate quantity
        if (itemRequest.getQuantity() <= 0) {
            throw new OrderDomainException("Product quantity must be positive: " + itemRequest.getProductId());
        }
        
        // Add item to order
        order.addOrderItem(
            itemRequest.getProductId(),
            itemRequest.getProductTitle(),
            itemRequest.getQuantity(),
            itemRequest.getUnitPrice()
        );
    }
    
    private DeliveryInfo createDeliveryInfo(CreateOrderRequest request, Order order) {
        CreateOrderRequest.DeliveryInfoRequest deliveryRequest = request.getDeliveryInfo();
        
        DeliveryInfo deliveryInfo = DeliveryInfo.builder()
            .name(deliveryRequest.getName())
            .phone(deliveryRequest.getPhone())
            .email(deliveryRequest.getEmail())
            .address(deliveryRequest.getAddress())
            .province(deliveryRequest.getProvince())
            .deliveryMessage(deliveryRequest.getDeliveryMessage())
            .deliveryType(request.isRushOrder() ? DeliveryType.RUSH : DeliveryType.STANDARD)
            .build();
        
        // Calculate delivery fee based on business rules
        float totalWeight = order.getTotalWeight();
        int deliveryFee = calculateDeliveryFee(deliveryInfo.getProvince(), 
            totalWeight, request.isRushOrder(), order.getTotalBeforeVAT());
        
        deliveryInfo.setDeliveryFee(deliveryFee);
        
        return deliveryInfo;
    }
    
    /**
     * Calculate delivery fee based on existing business logic from DeliveryFeeCalculating service
     */
    private int calculateDeliveryFee(String province, float totalWeight, boolean isRushOrder, long orderTotal) {
        logger.debug("Calculating delivery fee for province: {}, weight: {}, rush: {}", 
            province, totalWeight, isRushOrder);
        
        int normalFee = 0;
        int rushFee = 0;
        
        // Major cities vs other provinces
        boolean isMajorCity = HANOI.equalsIgnoreCase(province) || HO_CHI_MINH_CITY.equalsIgnoreCase(province);
        
        if (isMajorCity) {
            // Major city calculation
            normalFee = MAJOR_CITY_BASE_FEE;
            float extraWeight = totalWeight - MAJOR_CITY_WEIGHT_THRESHOLD;
            
            while (extraWeight > 0.5f) {
                normalFee += ADDITIONAL_WEIGHT_FEE;
                extraWeight -= 0.5f;
            }
        } else {
            // Other provinces calculation
            normalFee = OTHER_PROVINCE_BASE_FEE;
            float extraWeight = totalWeight - OTHER_PROVINCE_WEIGHT_THRESHOLD;
            
            while (extraWeight > 0.5f) {
                normalFee += ADDITIONAL_WEIGHT_FEE;
                extraWeight -= 0.5f;
            }
        }
        
        // Rush order surcharge
        if (isRushOrder) {
            rushFee = normalFee + RUSH_ORDER_SURCHARGE;
        }
        
        // Free shipping discount for orders over threshold
        if (orderTotal > FREE_SHIPPING_THRESHOLD) {
            normalFee = Math.max(0, normalFee - DELIVERY_DISCOUNT);
        }
        
        int finalFee = isRushOrder ? rushFee : normalFee;
        
        logger.debug("Calculated delivery fee: {} (normal: {}, rush: {})", finalFee, normalFee, rushFee);
        return finalFee;
    }
    
    private void applyDeliveryFeeToTotal(Order order) {
        if (order.getDeliveryInfo() != null) {
            int deliveryFee = order.getDeliveryInfo().getDeliveryFee();
            order.setTotalAfterVAT(order.getTotalAfterVAT() + deliveryFee);
        }
    }
    
    private void applyBusinessRules(Order order, CreateOrderRequest request) throws OrderDomainException {
        // Business rule: Minimum order amount
        if (order.getTotalBeforeVAT() < MINIMUM_ORDER_AMOUNT) {
            throw new OrderDomainException("Order total must be at least " + MINIMUM_ORDER_AMOUNT + " VND");
        }
        
        // Business rule: Rush order validation
        if (request.isRushOrder() && !isRushOrderAllowed(request)) {
            throw new OrderDomainException("Rush order not available for selected province or time");
        }
        
        // Business rule: Validate delivery address
        if (!isValidDeliveryAddress(request.getDeliveryInfo())) {
            throw new OrderDomainException("Invalid delivery address or province");
        }
    }
    
    private boolean canCancelOrderByStatus(Order order) {
        // Business rule from CancelOrder/Service/OrderValidationService
        return order.getStatus() == OrderStatus.PENDING;
    }
    
    private boolean isOrderTooOld(Order order) {
        // Business rule: Orders older than 1 hour cannot be modified
        return order.getCreatedAt() != null && 
               order.getCreatedAt().isBefore(LocalDateTime.now().minusHours(1));
    }
    
    private boolean isValidStatusTransition(OrderStatus from, OrderStatus to) {
        // Define valid status transitions based on business rules
        switch (from) {
            case PENDING:
                return to == OrderStatus.CONFIRMED || to == OrderStatus.CANCELLED;
            case CONFIRMED:
                return to == OrderStatus.SHIPPED || to == OrderStatus.CANCELLED;
            case SHIPPED:
                return to == OrderStatus.DELIVERED;
            case DELIVERED:
            case CANCELLED:
                return false; // Terminal states
            default:
                return false;
        }
    }
    
    private void validateOrderItems(List<CreateOrderRequest.OrderItemRequest> items, List<String> errors) {
        for (int i = 0; i < items.size(); i++) {
            CreateOrderRequest.OrderItemRequest item = items.get(i);
            String prefix = "Item " + (i + 1) + ": ";
            
            if (item.getProductId() == null) {
                errors.add(prefix + "Product ID is required");
            }
            
            if (item.getQuantity() <= 0) {
                errors.add(prefix + "Quantity must be positive");
            }
            
            if (item.getUnitPrice() < 0) {
                errors.add(prefix + "Unit price cannot be negative");
            }
            
            if (item.getProductTitle() == null || item.getProductTitle().trim().isEmpty()) {
                errors.add(prefix + "Product title is required");
            }
        }
    }
    
    private void validateDeliveryInfo(CreateOrderRequest.DeliveryInfoRequest deliveryInfo, List<String> errors) {
        if (deliveryInfo.getName() == null || deliveryInfo.getName().trim().isEmpty()) {
            errors.add("Delivery name is required");
        }
        
        if (deliveryInfo.getPhone() == null || deliveryInfo.getPhone().trim().isEmpty()) {
            errors.add("Delivery phone is required");
        }
        
        if (deliveryInfo.getAddress() == null || deliveryInfo.getAddress().trim().isEmpty()) {
            errors.add("Delivery address is required");
        }
        
        if (deliveryInfo.getProvince() == null || deliveryInfo.getProvince().trim().isEmpty()) {
            errors.add("Delivery province is required");
        }
        
        // Business validation: Field length limits (from InfoValidationService)
        if (deliveryInfo.getName() != null && deliveryInfo.getName().length() > 100) {
            errors.add("Delivery name cannot exceed 100 characters");
        }
        
        if (deliveryInfo.getPhone() != null && deliveryInfo.getPhone().length() > 100) {
            errors.add("Delivery phone cannot exceed 100 characters");
        }
        
        if (deliveryInfo.getAddress() != null && deliveryInfo.getAddress().length() > 100) {
            errors.add("Delivery address cannot exceed 100 characters");
        }
        
        if (deliveryInfo.getProvince() != null && deliveryInfo.getProvince().length() > 100) {
            errors.add("Delivery province cannot exceed 100 characters");
        }
        
        if (deliveryInfo.getDeliveryMessage() != null && deliveryInfo.getDeliveryMessage().length() > 100) {
            errors.add("Delivery message cannot exceed 100 characters");
        }
    }
    
    private boolean isRushOrderAllowed(CreateOrderRequest request) {
        // Business rule: Rush orders only available for major cities
        String province = request.getDeliveryInfo().getProvince();
        return HANOI.equalsIgnoreCase(province) || HO_CHI_MINH_CITY.equalsIgnoreCase(province);
    }
    
    private boolean isValidDeliveryAddress(CreateOrderRequest.DeliveryInfoRequest deliveryInfo) {
        // Business rule: Validate supported provinces
        String[] supportedProvinces = {
            HANOI, HO_CHI_MINH_CITY, "Hai Phong", "Thanh Hoa", "Nghe An", 
            "Hung Yen", "Da Nang", "Hue", "Nha Trang"
        };
        
        String province = deliveryInfo.getProvince();
        for (String supported : supportedProvinces) {
            if (supported.equalsIgnoreCase(province)) {
                return true;
            }
        }
        
        return false;
    }
    
    private void validateOrderId(String orderId) throws OrderDomainException {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new OrderDomainException("Order ID cannot be null or empty");
        }
        
        try {
            Long.parseLong(orderId);
        } catch (NumberFormatException e) {
            throw new OrderDomainException("Invalid order ID format: " + orderId);
        }
    }
}
