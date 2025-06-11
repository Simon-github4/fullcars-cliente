package model.client.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarPart {

    private Long id;
    private String name;
    private String description;
    private String sku;
    private long stock;
    private Brand brand;

    private Category category;

    //private int purchasePrice;
    //private BigDecimal salePrice;
}