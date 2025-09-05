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

	public SaleDetail(Integer quantity, BigDecimal unitPrice, CarPart product) {
		super(quantity, unitPrice, product);
	}

    
}