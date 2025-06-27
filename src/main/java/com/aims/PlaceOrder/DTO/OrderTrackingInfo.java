package Project_ITSS.PlaceOrder.DTO;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class OrderTrackingInfo {
    private int order_id;
    private String current_status;
    private String order_date;
    private Map<String,Object> order_details;


}
