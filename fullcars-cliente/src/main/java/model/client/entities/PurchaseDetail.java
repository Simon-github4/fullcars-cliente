package model.client.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseDetail extends Detail{

    private Purchase purchase;

	public PurchaseDetail(Integer quantity, Long unitPrice, CarPart product) {
		super(quantity, unitPrice, product);
	}
}