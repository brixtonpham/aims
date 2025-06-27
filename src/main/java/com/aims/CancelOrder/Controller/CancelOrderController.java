package Project_ITSS.CancelOrder.Controller;

import Project_ITSS.CancelOrder.Entity.Order;
import Project_ITSS.CancelOrder.Repository.OrderRepository_CancelOrder;
import Project_ITSS.CancelOrder.Service.OrderCancellationService;
import Project_ITSS.CancelOrder.Command.CommandResult;
import Project_ITSS.CancelOrder.Exception.OrderNotFoundException;
import Project_ITSS.CancelOrder.Exception.OrderCancellationException;
import Project_ITSS.CancelOrder.Service.OrderService_CancelOrder;
import Project_ITSS.CancelOrder.Service.ProductService_CancelOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for order cancellation operations
 * Implements improved design patterns with low coupling
 */
@RestController
@RequestMapping("/api/order")
@CrossOrigin(origins = "http://localhost:3000")
public class CancelOrderController {
    
    private static final Logger logger = LoggerFactory.getLogger(CancelOrderController.class);
    
    private final OrderCancellationService cancellationService;

    @Autowired
    private OrderService_CancelOrder orderService;
    @Autowired
    private ProductService_CancelOrder productService;
    
    @Autowired
    public CancelOrderController(OrderCancellationService cancellationService) {
        this.cancellationService = cancellationService;
    }

    /**
     * Cancel order endpoint with improved design patterns
     */
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelOrder(@RequestParam("order_id") long orderId, 
                                       HttpServletRequest request) {
        logger.info("Received cancel order request for order: {}", orderId);
        
        try {
            CommandResult result = cancellationService.cancelOrder(orderId, request);
            
            if (result.isSuccess()) {
                logger.info("Order cancellation successful for order: {}", orderId);
                return ResponseEntity.ok(result);
            } else {
                logger.warn("Order cancellation failed for order: {} - {}", orderId, result.getMessage());
                return ResponseEntity.badRequest().body(result.getMessage());
            }
            
        } catch (OrderNotFoundException e) {
            logger.error("Order not found: {}", orderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Order not found: " + orderId);
        } catch (OrderCancellationException e) {
            logger.error("Order cancellation exception: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during order cancellation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal error: " + e.getMessage());
        }
    }

    @PostMapping("/approve")
    public Map<String, Object> approveOrder(@RequestParam("order_id") int orderId) {
        Map<String, Object> json = new HashMap<>();
        try {
            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                json.put("status",0);
                json.put("message","Order not found");
                return json;
            }
            if (!"pending".equalsIgnoreCase(order.getStatus())) {
                json.put("status",0);
                json.put("message","Order status must be 'pending' to approve. Current status: " + order.getStatus());
                return json;
            }
            orderService.updateOrderStatusToApprove(orderId);
            productService.updateProductQuantity(orderId);
            json.put("status",1);
            json.put("message","Order " + orderId + " has been approved.");
            return json;
        } catch (Exception e) {
            e.printStackTrace();
            json.put("status",0);
            json.put("message","Error approving order: " + e.getMessage());
            return json;
        }
    }

    /**
     * Test endpoint
     */
    @GetMapping("/test")
    public String test() {
        return "Cancel Order Controller is working!";
    }
} 