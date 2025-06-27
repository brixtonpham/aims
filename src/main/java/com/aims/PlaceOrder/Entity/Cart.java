package Project_ITSS.PlaceOrder.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Cart {
     private List<CartItem> listofProducts = new ArrayList<>();


     public void addProducts(List<CartItem> products){
         for (CartItem Cartproduct : products){
             listofProducts.add(Cartproduct);
         }
     }

    public List<CartItem> getProducts(){
         return listofProducts;
     }

     public void EmptyCart(){
         listofProducts = new ArrayList<CartItem>();
     }


}
