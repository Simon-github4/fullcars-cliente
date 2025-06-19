package model.client.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SaleDetail extends Detail{

    private Sale sale;

}