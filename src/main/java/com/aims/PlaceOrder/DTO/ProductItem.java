package Project_ITSS.PlaceOrder.DTO;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductItem {
    private int product_id;
    private String title;
    private int price;
    private int quantity;
    private boolean rush_order_using;
}
