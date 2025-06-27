package Project_ITSS.PlaceOrder.Service;

import Project_ITSS.PlaceOrder.Entity.DeliveryInformation;
import Project_ITSS.PlaceOrder.Exception.PlaceOrderException;
import org.springframework.stereotype.Service;

@Service
public class InfoValidationService {

    /**
     * Validates that all string fields in DeliveryInformation do not exceed 100 characters
     * @param deliveryInfo the delivery information to validate
     * @throws PlaceOrderException if any string field exceeds 100 characters
     */
    public void validateDeliveryInfoStringLength(DeliveryInformation deliveryInfo) {
        if (deliveryInfo == null) {
            throw new PlaceOrderException("Delivery information cannot be null");
        }
        
        // Check each string field
        if (deliveryInfo.getName() != null && deliveryInfo.getName().length() > 100) {
            throw new PlaceOrderException("Name field exceeds 100 characters limit");
        }
        
        if (deliveryInfo.getPhone() != null && deliveryInfo.getPhone().length() > 100) {
            throw new PlaceOrderException("Phone field exceeds 100 characters limit");
        }
        
        if (deliveryInfo.getEmail() != null && deliveryInfo.getEmail().length() > 100) {
            throw new PlaceOrderException("Email field exceeds 100 characters limit");
        }
        
        if (deliveryInfo.getAddress() != null && deliveryInfo.getAddress().length() > 100) {
            throw new PlaceOrderException("Address field exceeds 100 characters limit");
        }
        
        if (deliveryInfo.getProvince() != null && deliveryInfo.getProvince().length() > 100) {
            throw new PlaceOrderException("Province field exceeds 100 characters limit");
        }
        
        if (deliveryInfo.getDelivery_message() != null && deliveryInfo.getDelivery_message().length() > 100) {
            throw new PlaceOrderException("Delivery message field exceeds 100 characters limit");
        }
    }
}
