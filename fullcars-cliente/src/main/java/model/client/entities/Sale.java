package model.client.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Sale {

    private Long id;
    private LocalDate date;
    private String saleNumber;
    private BigDecimal taxes;
    
    //en vez de poner customer, plasmar los atributos de el customer en ESE MOMENTO
    private Customer customer;
    private List<SaleDetail> details;
}