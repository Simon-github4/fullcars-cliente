package dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultiPaymentRequest {

    private Long customerId;
    private BigDecimal paymentAmount;
    private String paymentMethod;
    private LocalDate date;
    private String notes;
    private List<Long> saleIds;
    private Boolean useCredit;
}
