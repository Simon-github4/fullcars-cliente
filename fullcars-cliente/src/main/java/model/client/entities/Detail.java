package model.client.entities;

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
    private Long unitPrice;

    private CarPart product;
    
    public Long getSubTotal() {
    	return quantity * unitPrice ;
    }

	public Detail(Integer quantity, Long unitPrice, CarPart product) {
		super();
		this.id = null;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.product = product;
	}
    
    
}
