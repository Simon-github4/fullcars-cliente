package model.client.entities;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    
    public BigDecimal getTotal() {
    	if(factura != null)
    		return factura.getImpTotal();
    	
    	BigDecimal total = BigDecimal.ZERO;
    	for (SaleDetail d : details) 
    	    total = total.add(d.getSubTotal());
    	
    	return total.setScale(2, RoundingMode.HALF_UP);
    }
}

