package Project_ITSS.PlaceOrder.Controller;

import Project_ITSS.PlaceOrder.DTO.*;
import Project_ITSS.PlaceOrder.Entity.*;
import Project_ITSS.PlaceOrder.Exception.PlaceOrderException;
import Project_ITSS.PlaceOrder.Repository.OrderRepository_PlaceOrder;
import Project_ITSS.PlaceOrder.Service.*;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/")
@RestController
@CrossOrigin(origins = "http://localhost:3000")
@NoArgsConstructor
public class PlaceOrderController {

    @Value("${calculating_service_version}")
    private String calculating_service_version;

    private final Map<String,IDeliveryFeeCalculating> DeliveryFeeCalculatingsMap = new HashMap<>();

    @Autowired
    PlaceOrderController(List<IDeliveryFeeCalculating> iDeliveryFeeCalculatings,
                                     List<IProcessTrackingInfo> iProcessTrackingInfos){
        for(IDeliveryFeeCalculating service : iDeliveryFeeCalculatings){
            DeliveryFeeCalculatingsMap.put(service.getVersion(),service);
        }
    }

    @Autowired
    private ProductService_PlaceOrder productService;
    @Autowired
    private OrderService_PlaceOrder orderService;
    @Autowired
    private EmailNotification_PlaceOrder nonDBService;
    @Autowired
    private DeliveryInformationService deliveryInformationService;
    @Autowired
    private OrderlineService_PlaceOrder orderlineService;
    @Autowired
    private InfoValidationService infoValidationService;

    @GetMapping("/test")
    public ResponseEntity<String> JustForFun(){
        System.out.println(100);
        return ResponseEntity.ok("Test successful");
    }

    @PostMapping("/placeorder")
    public ResponseEntity<PlaceOrderResponse> RequestToPlaceOrder(@RequestBody Cart cart){
        if(cart == null){
            throw new PlaceOrderException("The cart is null");
        }
        for(CartItem cartItem : cart.getProducts()){
            Product product = cartItem.getProduct();
            int quantity = cartItem.getQuantity();
            System.out.println(product.getProduct_id());
            if(product == null){
                throw new PlaceOrderException("Product is null");
            }
            boolean result = productService.checkProductValidity(quantity,product.getProduct_id());
            if(!result){
                System.out.println("Failed");
                return ResponseEntity.badRequest().body(new PlaceOrderResponse(null, "Inadequate"));
            }
        }
        Order order = new Order();
        order.createOrder(cart);   
        PlaceOrderResponse response = new PlaceOrderResponse(order, "Successfully");
        return ResponseEntity.ok(response);
    }


    @PostMapping("/deliveryinfo")
    public ResponseEntity<DeliveryInfoResponse> SubmitDeliveryInformation(@RequestBody DeliveryInformation deliveryInfo){
        // Validate string field lengths before processing
        infoValidationService.validateDeliveryInfoStringLength(deliveryInfo);
        
        DeliveryInformation deliveryInformation = new DeliveryInformation();
        deliveryInformation.createDeliveryInfo(deliveryInfo.getName(),deliveryInfo.getPhone(),deliveryInfo.getEmail(),deliveryInfo.getAddress(),deliveryInfo.getProvince(),deliveryInfo.getDelivery_message(),deliveryInfo.getDelivery_fee());
        DeliveryInfoResponse response = new DeliveryInfoResponse(deliveryInformation);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/recalculate")
    public ResponseEntity<RecalculateResponse> recalculate(@RequestBody CalculateFeeDTO feeInfoDTO) {
        IDeliveryFeeCalculating service = DeliveryFeeCalculatingsMap.get(calculating_service_version);
        int[] deliveryfees = service.CalculateDeliveryFee(feeInfoDTO);
        int deliveryfee = deliveryfees[0] + deliveryfees[1];
        RecalculateResponse response = new RecalculateResponse(deliveryfees[0], deliveryfees[1], deliveryfee);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/finish-order")
    public ResponseEntity<FinishOrderResponse> FinishPlaceOrder(@RequestBody Order_DeliveryInfo orderInfoDTO){
        DeliveryInformation deliveryInformation = orderInfoDTO.getDeliveryInformation();
        Order order = orderInfoDTO.getOrder();
        int delivery_id = deliveryInformationService.saveDeliveryInfo(deliveryInformation);
        order.setDelivery_id(delivery_id);
        System.out.println(deliveryInformation.getDelivery_fee());
        System.out.println(order.getTotal_after_VAT());
        orderService.saveOrder(order);
        orderlineService.saveOrderlines(order);
        // Cập nhật số lượng sản phẩm sau khi lưu orderline
        nonDBService.SendSuccessEmail(deliveryInformation.getEmail(),"Thông báo về việc đặt hàng","Bạn đã đặt hàng thành công. Mã đơn hàng của bạn là:" + order.getOrder_id());
        FinishOrderResponse response = new FinishOrderResponse(1);
        return ResponseEntity.ok(response);
    }
}


