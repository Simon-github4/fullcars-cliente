package model.client.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sale {

    private Long id;
    private LocalDate date;
    private String saleNumber;
    private BigDecimal taxes;
    
    private Customer customer;
    private String remitoPath;
    private Factura factura;
    private List<SaleDetail> details;
    
    //private char type;
    
    public Long getTotal() {
    	Long total = 0L;
    	for (SaleDetail d : details) 
    	    total += d.getSubTotal();
    	 return total;
    }
}

