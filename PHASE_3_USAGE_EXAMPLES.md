# AIMS Backend - Phase 3 Usage Examples

## Overview
This document provides practical examples of how to use the new Phase 3 Command Pattern, Factory Pattern, and Event-Driven Architecture features in the AIMS backend system.

## Command Pattern Usage

### 1. Placing an Order Using Commands

#### Basic Order Placement
```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    private final CommandBus commandBus;
    
    @PostMapping("/v2")
    public ResponseEntity<ApiResponse<OrderCreationResult>> placeOrder(
            @RequestBody OrderRequest request) {
        
        // Create command
        PlaceOrderCommand command = PlaceOrderCommand.builder()
            .customerId(String.valueOf(request.getCustomerId()))
            .items(convertToOrderItems(request))
            .deliveryInfo(convertToDeliveryInfo(request))
            .paymentMethod(request.getPaymentMethod())
            .isRushOrder(request.isRushOrder())
            .build();
        
        // Execute command through bus
        OrderCreationResult result = commandBus.execute(command);
        
        return ResponseEntity.ok(ApiResponse.success("Order placed successfully", result));
    }
}
```

#### Advanced Order Placement with Validation
```java
@Service
public class OrderService {
    
    private final CommandBus commandBus;
    
    public OrderCreationResult placeOrderWithValidation(OrderRequest request) {
        try {
            // Build command with all parameters
            PlaceOrderCommand command = PlaceOrderCommand.builder()
                .customerId(String.valueOf(request.getCustomerId()))
                .items(buildOrderItems(request))
                .deliveryInfo(buildDeliveryInfo(request))
                .paymentMethod(request.getPaymentMethod())
                .language(request.getLanguage())
                .bankCode(request.getBankCode())
                .isRushOrder(request.isRushOrder())
                .httpRequest(getCurrentHttpRequest())
                .build();
            
            // Command validation happens automatically
            return commandBus.execute(command);
            
        } catch (CommandValidationException e) {
            throw new IllegalArgumentException("Invalid order data: " + e.getMessage());
        } catch (CommandExecutionException e) {
            throw new RuntimeException("Failed to place order: " + e.getMessage());
        }
    }
}
```

### 2. Cancelling an Order

#### Simple Order Cancellation
```java
@DeleteMapping("/{orderId}/v2")
public ResponseEntity<ApiResponse<CancellationResult>> cancelOrder(
        @PathVariable String orderId,
        @RequestParam String requestedBy,
        @RequestParam(required = false) String reason) {
    
    // Create cancellation command
    CancelOrderCommand command = CancelOrderCommand.builder()
        .orderId(orderId)
        .requestedBy(requestedBy)
        .reason(reason != null ? reason : "Customer request")
        .build();
    
    // Execute cancellation
    CancellationResult result = commandBus.execute(command);
    
    return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", result));
}
```

### 3. Processing Payments

#### Payment Processing with Commands
```java
@Service
public class PaymentService {
    
    private final CommandBus commandBus;
    
    public PaymentResult processPayment(PaymentRequest request) {
        ProcessPaymentCommand command = ProcessPaymentCommand.builder()
            .orderId(request.getOrderId())
            .amount(request.getAmount())
            .currency(request.getCurrency())
            .paymentMethod(request.getPaymentMethod())
            .customerId(request.getCustomerId())
            .language(request.getLanguage())
            .bankCode(request.getBankCode())
            .httpRequest(request.getHttpRequest())
            .build();
        
        return commandBus.execute(command);
    }
}
```

## Factory Pattern Usage

### 1. Payment Service Factory

#### Using the Factory Coordinator
```java
@Service
public class PaymentProcessingService {
    
    private final PaymentServiceFactoryCoordinator factoryCoordinator;
    
    public PaymentResult processPayment(String region, PaymentMethod method, 
                                       DomainPaymentRequest request) {
        
        // Get appropriate factory for region
        PaymentServiceFactory factory = factoryCoordinator.getFactory(region);
        
        // Create payment service
        PaymentDomainService paymentService = factory.getPaymentService(method);
        
        // Process payment
        return paymentService.processPayment(request);
    }
}
```

#### Direct Factory Usage
```java
@Service
public class VietnamPaymentService {
    
    private final VietnamPaymentServiceFactory factory;
    
    public PaymentResult processVNPayPayment(DomainPaymentRequest request) {
        // Get VNPay service
        PaymentDomainService vnpayService = factory.getPaymentService(PaymentMethod.VNPAY);
        
        return vnpayService.processPayment(request);
    }
    
    public PaymentResult processBankTransfer(DomainPaymentRequest request) {
        // Get bank transfer service
        PaymentDomainService bankService = factory.getPaymentService(PaymentMethod.BANK_TRANSFER);
        
        return bankService.processPayment(request);
    }
}
```

### 2. Extending the Factory Pattern

#### Creating a New Regional Factory
```java
@Component
public class EuropePaymentServiceFactory extends AbstractPaymentServiceFactory {
    
    @Override
    protected PaymentDomainService createVNPayService() {
        // Europe doesn't support VNPay
        throw new UnsupportedOperationException("VNPay not supported in Europe");
    }
    
    @Override
    protected PaymentDomainService createCreditCardService() {
        return new EuropeCreditCardService();
    }
    
    @Override
    protected PaymentDomainService createBankTransferService() {
        return new EuropeBankTransferService();
    }
    
    @Override
    public boolean supports(PaymentMethod method) {
        return method == PaymentMethod.CREDIT_CARD || 
               method == PaymentMethod.BANK_TRANSFER;
    }
}
```

## Event-Driven Architecture Usage

### 1. Event Handlers

#### Order Event Handler
```java
@Component
public class OrderNotificationHandler implements EventHandler<OrderCreatedEvent> {
    
    private final NotificationService notificationService;
    
    @Override
    public void handle(OrderCreatedEvent event) {
        // Send order confirmation email
        notificationService.sendOrderConfirmation(
            event.getCustomerId(),
            event.getOrderId(),
            event.getTotalAmount()
        );
        
        // Update inventory
        inventoryService.reserveItems(event.getOrderItems());
        
        // Log for analytics
        analyticsService.trackOrderCreated(event);
    }
    
    @Override
    public Class<OrderCreatedEvent> getEventType() {
        return OrderCreatedEvent.class;
    }
}
```

#### Payment Event Handler
```java
@Component
public class PaymentProcessedHandler implements EventHandler<PaymentProcessedEvent> {
    
    private final OrderService orderService;
    private final NotificationService notificationService;
    
    @Override
    public void handle(PaymentProcessedEvent event) {
        if (event.isSuccessful()) {
            // Update order status
            orderService.markAsPaid(event.getOrderId());
            
            // Send payment confirmation
            notificationService.sendPaymentConfirmation(
                event.getOrderId(),
                event.getTransactionId(),
                event.getAmount()
            );
            
            // Trigger fulfillment
            fulfillmentService.startFulfillment(event.getOrderId());
        } else {
            // Handle payment failure
            orderService.markPaymentFailed(event.getOrderId());
            
            // Send failure notification
            notificationService.sendPaymentFailureNotification(event.getOrderId());
        }
    }
    
    @Override
    public Class<PaymentProcessedEvent> getEventType() {
        return PaymentProcessedEvent.class;
    }
}
```

### 2. Publishing Custom Events

#### Creating Custom Events
```java
public class OrderShippedEvent extends DomainEvent {
    
    private final Long orderId;
    private final String trackingNumber;
    private final String carrier;
    private final LocalDateTime shippedAt;
    
    public OrderShippedEvent(Long orderId, String trackingNumber, String carrier) {
        super("OrderShipped");
        this.orderId = orderId;
        this.trackingNumber = trackingNumber;
        this.carrier = carrier;
        this.shippedAt = LocalDateTime.now();
    }
    
    // Getters...
}
```

#### Publishing Events
```java
@Service
public class FulfillmentService {
    
    private final DomainEventPublisher eventPublisher;
    
    public void shipOrder(Long orderId, String trackingNumber, String carrier) {
        // Update order status
        updateOrderStatus(orderId, OrderStatus.SHIPPED);
        
        // Publish shipping event
        OrderShippedEvent event = new OrderShippedEvent(orderId, trackingNumber, carrier);
        eventPublisher.publish(event);
    }
}
```

### 3. Event Store Usage

#### Querying Events
```java
@Service
public class OrderAuditService {
    
    private final EventStore eventStore;
    
    public List<DomainEvent> getOrderHistory(Long orderId) {
        return eventStore.getEvents().stream()
            .filter(event -> event.getMetadata().containsKey("orderId"))
            .filter(event -> Objects.equals(
                event.getMetadata().get("orderId"), 
                orderId.toString()
            ))
            .collect(Collectors.toList());
    }
    
    public List<DomainEvent> getEventsAfter(LocalDateTime timestamp) {
        return eventStore.getEventsAfter(timestamp);
    }
}
```

## Middleware Usage

### 1. Custom Middleware

#### Performance Monitoring Middleware
```java
@Component
public class PerformanceMonitoringMiddleware implements CommandMiddleware {
    
    private final MeterRegistry meterRegistry;
    
    @Override
    public <R> R execute(Command<R> command, CommandHandler<?, R> handler, 
                        CommandMiddlewareChain chain) {
        
        Timer.Sample sample = Timer.start(meterRegistry);
        String commandType = command.getCommandType();
        
        try {
            R result = chain.execute(command, handler);
            
            // Record success metric
            meterRegistry.counter("command.success", "type", commandType).increment();
            
            return result;
            
        } catch (Exception e) {
            // Record failure metric
            meterRegistry.counter("command.failure", "type", commandType).increment();
            throw e;
            
        } finally {
            // Record timing
            sample.stop(Timer.builder("command.duration")
                .tag("type", commandType)
                .register(meterRegistry));
        }
    }
    
    @Override
    public int getOrder() {
        return 100; // Execute before others
    }
}
```

#### Security Middleware
```java
@Component
public class SecurityCommandMiddleware implements CommandMiddleware {
    
    private final SecurityService securityService;
    
    @Override
    public <R> R execute(Command<R> command, CommandHandler<?, R> handler, 
                        CommandMiddlewareChain chain) {
        
        // Check authentication
        if (!securityService.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }
        
        // Check authorization for command type
        if (!securityService.canExecute(command.getCommandType())) {
            throw new SecurityException("Insufficient permissions");
        }
        
        return chain.execute(command, handler);
    }
    
    @Override
    public int getOrder() {
        return 50; // Execute early in chain
    }
}
```

## Integration Examples

### 1. Complete Order Flow

```java
@Service
@Transactional
public class OrderOrchestrationService {
    
    private final CommandBus commandBus;
    private final DomainEventPublisher eventPublisher;
    
    public OrderCreationResult processCompleteOrder(CompleteOrderRequest request) {
        
        // Step 1: Place order
        PlaceOrderCommand placeOrderCommand = PlaceOrderCommand.builder()
            .customerId(request.getCustomerId())
            .items(request.getItems())
            .deliveryInfo(request.getDeliveryInfo())
            .paymentMethod(request.getPaymentMethod())
            .build();
        
        OrderCreationResult orderResult = commandBus.execute(placeOrderCommand);
        
        // Step 2: Process payment if order was created successfully
        if (orderResult.isSuccess()) {
            ProcessPaymentCommand paymentCommand = ProcessPaymentCommand.builder()
                .orderId(orderResult.getOrderId())
                .amount(orderResult.getAmount())
                .currency(orderResult.getCurrency())
                .paymentMethod(request.getPaymentMethod())
                .customerId(request.getCustomerId())
                .httpRequest(request.getHttpRequest())
                .build();
            
            PaymentResult paymentResult = commandBus.execute(paymentCommand);
            
            // Update order result with payment information
            orderResult.setPaymentUrl(paymentResult.getPaymentUrl());
            orderResult.setTransactionId(paymentResult.getTransactionId());
        }
        
        return orderResult;
    }
}
```

### 2. Testing Examples

#### Unit Testing Commands
```java
@ExtendWith(MockitoExtension.class)
class PlaceOrderCommandHandlerTest {
    
    @Mock
    private OrderDomainService orderDomainService;
    
    @Mock
    private DomainEventPublisher eventPublisher;
    
    @InjectMocks
    private PlaceOrderCommandHandler handler;
    
    @Test
    public void shouldPlaceOrderSuccessfully() {
        // Given
        PlaceOrderCommand command = PlaceOrderCommand.builder()
            .customerId("123")
            .items(createTestItems())
            .deliveryInfo(createTestDeliveryInfo())
            .build();
        
        Order mockOrder = createMockOrder();
        when(orderDomainService.createOrder(any())).thenReturn(mockOrder);
        
        // When
        OrderCreationResult result = handler.handle(command);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getOrderId()).isEqualTo("123");
        
        verify(eventPublisher).publish(any(OrderCreatedEvent.class));
    }
}
```

#### Integration Testing
```java
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class OrderIntegrationTest {
    
    @Autowired
    private CommandBus commandBus;
    
    @Autowired
    private EventStore eventStore;
    
    @Test
    public void shouldHandleCompleteOrderFlow() {
        // Given
        PlaceOrderCommand command = createValidOrderCommand();
        
        // When
        OrderCreationResult result = commandBus.execute(command);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        
        // Verify events were published
        List<DomainEvent> events = eventStore.getEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(OrderCreatedEvent.class);
    }
}
```

## Best Practices

### 1. Command Design
- Keep commands immutable
- Include all necessary data
- Implement validation in commands
- Use builder pattern for complex commands

### 2. Event Design
- Make events immutable
- Include sufficient context
- Use past tense for event names
- Include correlation IDs for tracing

### 3. Handler Design
- Keep handlers focused on single responsibility
- Handle exceptions appropriately
- Use dependency injection
- Implement proper logging

### 4. Factory Design
- Use abstract factories for extensibility
- Implement proper error handling
- Cache expensive service instances
- Support configuration-based service selection

This comprehensive guide provides practical examples for implementing and using the Phase 3 features in real-world scenarios.
