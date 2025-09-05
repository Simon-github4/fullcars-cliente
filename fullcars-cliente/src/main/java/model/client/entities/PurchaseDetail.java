package model.client.entities;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseDetail extends Detail{

    private Purchase purchase;

	public PurchaseDetail(Integer quantity, BigDecimal unitPrice, CarPart product) {
		super(quantity, unitPrice, product);
	}
}