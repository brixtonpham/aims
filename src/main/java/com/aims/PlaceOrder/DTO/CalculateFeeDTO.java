package Project_ITSS.PlaceOrder.DTO;

import Project_ITSS.PlaceOrder.Entity.Order;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CalculateFeeDTO {
    @JsonProperty("province")
    private String province;

    @JsonProperty("order")
    @JsonIgnoreProperties(ignoreUnknown = true)
    private Order order;
}
