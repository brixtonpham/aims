package Project_ITSS.PlaceOrder.Service;

import Project_ITSS.PlaceOrder.Entity.DeliveryInformation;
import Project_ITSS.PlaceOrder.Repository.DeliveryInfoRepository_PlaceOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeliveryInformationService {

    @Autowired
    private DeliveryInfoRepository_PlaceOrder deliveryInformationRepo;
    public int saveDeliveryInfo(DeliveryInformation deliveryInformation) {
        return deliveryInformationRepo.saveDeliveryInfo(deliveryInformation);
    }
} 