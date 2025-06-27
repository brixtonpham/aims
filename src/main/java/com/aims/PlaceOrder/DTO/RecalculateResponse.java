package Project_ITSS.PlaceOrder.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecalculateResponse {
    private int regularShipping;
    private int rushShipping;
    private int totalShipping;
} 