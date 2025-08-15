package model.client.entities;

import java.time.LocalDate;

import Utils.MovementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovement {

    private Long id;
    private Integer quantity;
    private LocalDate date;
    private String reference;//EJ: Sale #155
    
    private CarPart carPart;

    private MovementType type;

    //Uno de los dos va a ser null
    private SaleDetail saleDetail;
    private PurchaseDetail purchaseDetail;
    
}