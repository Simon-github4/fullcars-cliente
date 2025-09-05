package model.client.entities;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Detail {
    
    private Long id;
    private Integer quantity;
    private BigDecimal unitPrice;

    private CarPart carPart;
    
    public BigDecimal getSubTotal() {
    	return unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP) ;
    }

	public Detail(Integer quantity, BigDecimal unitPrice, CarPart product) {
		super();
		this.id = null;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.carPart = product;
	}
    
    
}
