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
public class Purchase {

    private Long id;
    //private nroFactura;
    private LocalDate date;
    private BigDecimal taxes;

    private String observations;
    private String filePath;
    private boolean isPayed;
    
    private Provider provider;
    private List<PurchaseDetail> details = new ArrayList<>();

    public BigDecimal getTotal() {
    	BigDecimal total = BigDecimal.ZERO;
    	for(PurchaseDetail d : details) 
    		total = total.add(d.getSubTotal());
    	return total.setScale(2, RoundingMode.HALF_UP);
    }
}

