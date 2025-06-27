package Project_ITSS.CancelOrder.Service;

import Project_ITSS.CancelOrder.Entity.DeliveryInfo;
import Project_ITSS.CancelOrder.Entity.DeliveryInformation;
import Project_ITSS.CancelOrder.Entity.Order;
import Project_ITSS.CancelOrder.Entity.Orderline;
import Project_ITSS.CancelOrder.Repository.OrderRepository_CancelOrder;
import Project_ITSS.CancelOrder.Repository.ProductRepository_CancelOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService_CancelOrder {

    @Autowired
    private OrderRepository_CancelOrder orderRepository;

    public Order getOrderById(long orderId) {
        return orderRepository.getOrderById(orderId);
    }

    public void updateOrderStatusToApprove(long orderId) {
        orderRepository.updateOrderStatusToApprove(orderId);
    }

} 