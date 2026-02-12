package model.client.entities;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SaleDetail extends Detail{

	//@JsonIgnore
    private Sale sale;

    private String printedDescription;

	public SaleDetail(Integer quantity, BigDecimal unitPrice, CarPart product) {
		super(quantity, unitPrice, product);
		this.printedDescription = null;
	}

	public String getFacturaDescription() {
    	if (this.printedDescription != null && !this.printedDescription.isEmpty()) {
            return this.printedDescription;
        }

        if (this.getCarPart() != null) {
        	return getCarPart().getName()+ "  " +((getCarPart().getDescription() != null)? getCarPart().getDescription() : "");
        }
        
        return "ARTÍCULO SIN DESCRIPCIÓN";
    }
	
}