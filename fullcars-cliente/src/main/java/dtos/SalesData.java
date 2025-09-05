package dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SalesData {
    private BigDecimal amount;
    private LocalDate date;

    public SalesData(BigDecimal amount, LocalDate date) {
        this.amount = amount;
        this.date = date;
    }
    
    public BigDecimal getAmount() { return amount; }
    public LocalDate getDate() { return date; }
}
