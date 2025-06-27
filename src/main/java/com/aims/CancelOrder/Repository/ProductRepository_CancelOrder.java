package Project_ITSS.CancelOrder.Repository;

//import Project_ITSS.demo.Entity.Product;
import Project_ITSS.CancelOrder.Entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository_CancelOrder {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    public void updateProductQuantity(int order_id){
        String sql = "UPDATE Product p\n" +
                "SET quantity = p.quantity - ol.quantity\n" +
                "FROM Orderline ol\n" +
                "WHERE p.product_id = ol.product_id\n" +
                "  AND ol.order_id = ?";
        jdbcTemplate.update(sql, order_id);
    }
}
