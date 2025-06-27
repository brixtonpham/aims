package Project_ITSS.PlaceOrder.DTO;

import Project_ITSS.PlaceOrder.Entity.DeliveryInformation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryInfoResponse {
    private DeliveryInformation delivery_information;
} 