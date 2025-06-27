package Project_ITSS.PlaceOrder.DTO;

import Project_ITSS.PlaceOrder.Entity.DeliveryInformation;
import Project_ITSS.PlaceOrder.Entity.Order;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Order_DeliveryInfo {
    private Order order;
    private DeliveryInformation deliveryInformation;
}
