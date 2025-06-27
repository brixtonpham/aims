package Project_ITSS.PlaceOrder.DTO;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderInfo {
    private int order_id;
    private String customer_name;
    private int total_amount;
    private String order_date;
    private String status;
    private String payment_method;
}
