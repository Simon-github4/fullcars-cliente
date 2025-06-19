package model.client.entities;

import java.time.LocalDateTime;

import Utils.MovementType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StockMovement {

    private Long id;
    private Integer quantity;
    private LocalDateTime date;
    private String reference;//EJ: Sale #155
    private String observations;
    
    private CarPart product;

    private MovementType type;

}