package Project_ITSS.PlaceOrder.Service;

import Project_ITSS.PlaceOrder.DTO.OrderTrackingInfo;

public interface IProcessTrackingInfo {
    public OrderTrackingInfo processTracking(int order_id);
    public String getVersion();
} 