package Project_ITSS.PlaceOrder.DTO;

import Project_ITSS.PlaceOrder.Entity.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderResponse {
    private Order order;
    private String message;
} 