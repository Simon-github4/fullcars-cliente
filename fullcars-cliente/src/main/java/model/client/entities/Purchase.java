package model.client.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Purchase {

    private Long id;
    //private nroFactura;
    private LocalDate date;
    private BigDecimal taxes;
    private String observations;
    private boolean isReceiptSigned;
    private boolean isPayed;
    
    private Provider provider;

    private List<PurchaseDetail> details;

}

