package dtos;

import java.time.LocalDate;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SalesData {
    private long amount;
    private LocalDate date;

    public SalesData(long amount, LocalDate date) {
        this.amount = amount;
        this.date = date;
    }
    
    public long getAmount() { return amount; }
    public LocalDate getDate() { return date; }
}
