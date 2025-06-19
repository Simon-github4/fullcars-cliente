package model.client.entities;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class PurchaseDetail extends Detail{

    private Purchase purchase;

}