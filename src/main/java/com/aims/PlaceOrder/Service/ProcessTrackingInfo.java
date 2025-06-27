package Project_ITSS.PlaceOrder.Service;


import Project_ITSS.PlaceOrder.DTO.OrderTrackingInfo;
import Project_ITSS.PlaceOrder.DTO.ProductItem;
import Project_ITSS.PlaceOrder.Entity.Order;
import Project_ITSS.PlaceOrder.Repository.DeliveryInfoRepository_PlaceOrder;
import Project_ITSS.PlaceOrder.Repository.OrderRepository_PlaceOrder;
import Project_ITSS.PlaceOrder.Repository.OrderlineRepository_PlaceOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProcessTrackingInfo {
    @Autowired
    private OrderRepository_PlaceOrder orderRepository;
    @Autowired
    private OrderlineRepository_PlaceOrder orderlineRepository;
    @Autowired
    private DeliveryInfoRepository_PlaceOrder deliveryInfoRepository;

    public OrderTrackingInfo getTrackingOrder(int order_id){
        Order order = orderRepository.getOrderById(order_id);
        OrderTrackingInfo orderTrackingInfo = new OrderTrackingInfo();
        orderTrackingInfo.setOrder_id(order.getOrder_id());
        orderTrackingInfo.setCurrent_status(order.getStatus());
        orderTrackingInfo.setOrder_date(order.getOrder_time());
        Map<String,Object> json = new HashMap<>();
        json.put("total_amount",order.getTotal_after_VAT());
        String address = deliveryInfoRepository.getCustomerAddress(order_id);
        json.put("payment_method",order.getPayment_method());
        json.put("delivery_address",address);
        List<ProductItem> orderlines = orderlineRepository.getProductItemByOrderId(order_id);
        json.put("items",orderlines);
        orderTrackingInfo.setOrder_details(json);
        return orderTrackingInfo;
    }
}
