package model.client.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

	public SaleDetail(Integer quantity, Long unitPrice, CarPart product) {
		super(quantity, unitPrice, product);
	}

    
}