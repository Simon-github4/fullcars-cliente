package model.client.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pay {

    private Long id;
    private BigDecimal amount;
    private LocalDate date;
    private String paymentMethod;
    
    private Customer customer;
    
    private String description;
    
    private Sale sale;
    
    private BigDecimal creditUsed;
    private BigDecimal creditGenerated;

}
