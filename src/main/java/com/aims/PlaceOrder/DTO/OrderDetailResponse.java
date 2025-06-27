package Project_ITSS.PlaceOrder.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResponse {
    private int status;
    private String message;
    private OrderTrackingInfo order;
} 