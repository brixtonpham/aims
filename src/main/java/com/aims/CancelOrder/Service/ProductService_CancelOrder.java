package Project_ITSS.CancelOrder.Service;


import Project_ITSS.PlaceOrder.Exception.PlaceOrderException;
import Project_ITSS.CancelOrder.Repository.ProductRepository_CancelOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductService_CancelOrder {

    @Autowired
    private ProductRepository_CancelOrder productRepository;


    public void updateProductQuantity(int order_id) {
        productRepository.updateProductQuantity(order_id);
    }


}
