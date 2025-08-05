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
public class Purchase {

    private Long id;
    //private nroFactura;
    private LocalDate date;
    private BigDecimal taxes;

    private String observations;
    //private boolean isReceiptSigned;
    //private boolean isPayed;
    
    private Provider provider;
    private List<PurchaseDetail> details = new ArrayList<>();

    public Long getTotal() {
    	Long total = 0L;
    	for(PurchaseDetail d : details) 
    		total += d.getSubTotal();
    	return total;
    }
}

