package Project_ITSS.PlaceOrder.Controller;


import Project_ITSS.PlaceOrder.DTO.OrderDetailResponse;
import Project_ITSS.PlaceOrder.DTO.OrderInfo;
import Project_ITSS.PlaceOrder.DTO.OrderTrackingInfo;
import Project_ITSS.PlaceOrder.Service.OrderService_PlaceOrder;
import Project_ITSS.PlaceOrder.Service.ProcessTrackingInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequestMapping("/")
@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class OrderManagementController {
    @Autowired
    private ProcessTrackingInfo processTrackingInfo;
    @Autowired
    private OrderService_PlaceOrder orderService;

    @GetMapping("/orders")
    public ResponseEntity<List<OrderInfo>> getALlOrders(){
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/order-detail")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(@RequestParam("order_id") int order_id){
        try{
            OrderTrackingInfo order = processTrackingInfo.getTrackingOrder(order_id);
            OrderDetailResponse response = new OrderDetailResponse(1, "Order details is successfully taken !", order);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            OrderDetailResponse response = new OrderDetailResponse(0, "Order details is failed to be taken !", null);
            return ResponseEntity.badRequest().body(response);
        }
    }

}
