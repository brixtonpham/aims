package Project_ITSS.PlaceOrder.Service;

import Project_ITSS.PlaceOrder.DTO.OrderInfo;
import Project_ITSS.PlaceOrder.Entity.*;
import Project_ITSS.PlaceOrder.Repository.DeliveryInfoRepository_PlaceOrder;
import Project_ITSS.PlaceOrder.Repository.OrderRepository_PlaceOrder;
import Project_ITSS.PlaceOrder.Repository.OrderlineRepository_PlaceOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService_PlaceOrder {
    @Autowired
    private OrderRepository_PlaceOrder orderRepository;

    public void saveOrder(Order order){
        orderRepository.saveOrder(order);
    }

    public List<OrderInfo> getAllOrders(){
         return orderRepository.getALlOrders();
    }


    // ... các method khác cho order thường
} 