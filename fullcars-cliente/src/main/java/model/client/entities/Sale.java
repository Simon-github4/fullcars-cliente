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
    
    @Builder.Default
    private boolean anulada = false;

    private Customer customer;
    private String remitoPath;
    private Factura factura;
    private List<SaleDetail> details;
    
    private BigDecimal total;
    private BigDecimal totalReturned;
    
    public BigDecimal getTotal() {
    	if (total != null) return total;
    	if (factura != null) return factura.getImpTotal();
    	BigDecimal totalCalc = BigDecimal.ZERO;
    	for (SaleDetail d : details) 
    	    totalCalc = totalCalc.add(d.getSubTotal());
    	return totalCalc.setScale(2, RoundingMode.HALF_UP);
    }
}

