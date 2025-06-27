package Project_ITSS.PlaceOrder.Service;

import Project_ITSS.PlaceOrder.DTO.CalculateFeeDTO;
import Project_ITSS.PlaceOrder.Entity.Order;

public interface IDeliveryFeeCalculating {
    public int[] CalculateDeliveryFee(CalculateFeeDTO calculateFeeDTO);
    public String getVersion();
} 