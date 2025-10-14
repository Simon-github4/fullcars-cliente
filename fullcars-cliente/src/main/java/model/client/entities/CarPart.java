package model.client.entities;

import java.math.BigDecimal;

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
    private Long stock;
    
    private Brand brand;
    private Provider provider;
    private Category category;
    
    private String providerSku;
    private String quality;
    
    @Builder.Default
    private BigDecimal basePrice = BigDecimal.ZERO;

    @Override
    public String toString() {
    	return sku;
    }
}