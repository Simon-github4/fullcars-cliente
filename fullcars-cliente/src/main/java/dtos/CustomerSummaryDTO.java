package dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.client.entities.Customer;
import model.client.entities.Pay;
import model.client.entities.Sale;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSummaryDTO {

    private Customer customer;
    private List<Sale> sales;
    private List<Pay> payments;
    //private BigDecimal saldo;
    
}
