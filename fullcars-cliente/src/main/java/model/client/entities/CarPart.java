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
    private Long stock;
    
    private Brand brand;
    private Provider provider;
    private Category category;
    
    @Builder.Default
    private Long basePrice = 0L;

    @Override
    public String toString() {
    	return sku;
    }
}