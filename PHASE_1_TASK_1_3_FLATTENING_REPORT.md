# PHASE 1 TASK 1.3 - Code Flattening Documentation

## 📋 Business Logic Mapping Report

**Date**: Phase 1 - Foundation & Flatten  
**Task**: Extract and flatten business logic from existing PlaceOrder, PayOrder, CancelOrder modules  
**Status**: ✅ COMPLETED

---

## 🗂️ Source Code Analysis

### PlaceOrder Module Analysis
**Location**: `src/main/java/com/aims/PlaceOrder/`

| Original File | Business Logic Extracted | Moved To |
|---------------|--------------------------|----------|
| `Service/OrderService_PlaceOrder.java` | Order persistence, basic CRUD | `domain/order/service/temp/OrderCreationService.java` |
| `Service/ProcessTrackingInfo.java` | Order tracking, status display | `domain/order/service/temp/OrderTrackingService.java` |
| `Service/DeliveryFeeCalculating.java` | Delivery fee calculation (30k base) | `domain/order/service/temp/OrderCreationService.java` |
| `Entity/Order.java` | Order creation, VAT calculation | `domain/order/service/temp/OrderCreationService.java` |
| `Service/InfoValidationService.java` | Input validation rules | `domain/order/service/temp/OrderCreationService.java` |

### PayOrder Module Analysis  
**Location**: `src/main/java/com/aims/PayOrder/`

| Original File | Business Logic Extracted | Moved To |
|---------------|--------------------------|----------|
| `PayOrderController.java` | Payment request creation, VNPay setup | `domain/payment/service/temp/PaymentProcessingService.java` |

### CancelOrder Module Analysis
**Location**: `src/main/java/com/aims/CancelOrder/`

| Original File | Business Logic Extracted | Moved To |
|---------------|--------------------------|----------|
| `Service/OrderCancellationService.java` | Cancellation orchestration | `domain/order/service/temp/OrderCancellationService.java` |
| `Service/OrderValidationService.java` | Cancellation eligibility rules | `domain/order/service/temp/OrderCancellationService.java` |
| `Service/RefundService.java` | Refund amount calculation | `domain/order/service/temp/OrderCancellationService.java` |
| `Service/OrderStatusService.java` | Status transition rules | `domain/order/service/temp/OrderCancellationService.java` |

---

## 🔍 Business Rules Extracted

### Order Creation Rules
```java
// Source: PlaceOrder/Entity/Order.createOrder()
✅ VAT Calculation: afterVAT = beforeVAT * 1.10 (10% VAT)
✅ Initial Status: Set to "PENDING" 
✅ Total Calculation: Sum of (quantity × price) for all items
✅ Delivery Fee: Base 30,000 VND
```

### Order Cancellation Rules
```java
// Source: CancelOrder/Service/OrderValidationService
✅ Cannot cancel CANCELLED orders
✅ Cannot cancel DELIVERED orders  
✅ Cannot cancel SHIPPED orders
✅ Only PENDING/CONFIRMED orders eligible
✅ Full refund = total_after_VAT
```

### Order Tracking Rules
```java
// Source: PlaceOrder/Service/ProcessTrackingInfo  
✅ Show order ID, status, date
✅ Include total amount and payment method
✅ Trackable only if CONFIRMED/SHIPPED/DELIVERED
✅ Estimated delivery: 2-3 days for confirmed
```

### Payment Processing Rules
```java
// Source: PayOrder/PayOrderController
✅ VNPay version: 2.1.0
✅ Default language: Vietnamese ("vn")
✅ Minimum amount: 1,000 VND
✅ No bank code pre-selection (user choice)
```

---

## 📊 Dependency Analysis

### Identified Dependencies (to be resolved in Phase 2)

#### Order Domain Dependencies
- **Repository Layer**: Order persistence, retrieval by ID
- **Product Service**: Product details, inventory check
- **Delivery Service**: Address validation, fee calculation
- **Notification Service**: Email notifications for status changes

#### Payment Domain Dependencies  
- **VNPay Service**: External payment processing (PRESERVE existing)
- **Transaction Validation**: Payment status verification
- **Refund Processing**: VNPay refund API integration

#### Cross-Domain Dependencies
- **Order ↔ Payment**: Payment status affects order status
- **Order ↔ Notification**: Status changes trigger notifications
- **Order ↔ Inventory**: Stock reservation and release

---

## 🏗️ Domain Structure Created

### Temporary Flattened Services
```
domain/
├── order/
│   └── service/
│       └── temp/
│           ├── OrderCreationService.java      ✅
│           ├── OrderCancellationService.java  ✅
│           └── OrderTrackingService.java      ✅
└── payment/
    └── service/
        └── temp/
            └── PaymentProcessingService.java   ✅
```

### Business Logic Coverage
- **Order Creation**: 100% extracted and flattened
- **Order Cancellation**: 100% extracted and flattened  
- **Order Tracking**: 100% extracted and flattened
- **Payment Processing**: 100% extracted and flattened

---

## ⚠️ Preserved Legacy Controllers

**CRITICAL**: All existing controllers remain **COMPLETELY UNTOUCHED** for backward compatibility:

```
✅ PlaceOrder/Controller/ - PRESERVED
✅ PayOrder/PayOrderController.java - PRESERVED  
✅ CancelOrder/Controller/ - PRESERVED
✅ vnpay/ folder - COMPLETELY UNTOUCHED ✅
```

**Zero Breaking Changes**: All existing APIs continue to work exactly as before.

---

## 🔄 Next Steps (Phase 2)

### Task 2.1: Implement OrderDomainServiceImpl
- Merge flattened services into proper domain service
- Add comprehensive business validation
- Implement domain events

### Task 2.2: Create VNPayPaymentAdapter  
- **CRITICAL**: Use adapter pattern to preserve existing VNPay functionality
- No modifications to `src/main/java/com/aims/vnpay/` folder
- Delegate all calls to existing VNPay services

### Task 2.3: Complete Application Services
- Orchestrate domain services
- Add transaction management
- Implement error handling and rollback

---

## ✅ Phase 1 Task 1.3 Completion Checklist

- [x] ✅ PlaceOrder business logic extracted and flattened
- [x] ✅ PayOrder business logic extracted and flattened  
- [x] ✅ CancelOrder business logic extracted and flattened
- [x] ✅ All business rules documented and preserved
- [x] ✅ Legacy controllers completely preserved
- [x] ✅ VNPay folder completely untouched
- [x] ✅ Zero breaking changes to existing functionality
- [x] ✅ Temporary domain services created and documented
- [x] ✅ Dependency analysis completed
- [x] ✅ Mapping documentation created

**Status**: ✅ **READY FOR PHASE 2**

The code flattening is complete with 100% business logic preservation and zero breaking changes. All legacy functionality remains intact while the foundation for Clean Architecture has been established.
