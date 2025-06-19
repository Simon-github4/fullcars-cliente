package model.client.entities;

import java.math.BigDecimal;

import lombok.Data;


@Data
public abstract class Detail {
    
    private Long id;
    private Integer quantity;
    private BigDecimal unitPrice;

    private CarPart product;
    
}
