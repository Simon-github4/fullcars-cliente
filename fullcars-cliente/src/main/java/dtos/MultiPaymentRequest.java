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
    private List<Long> saleIds;
    private BigDecimal paymentAmount;
    private LocalDate date;
    private String notes;
    private Boolean useCredit;
    private List<PaymentSplitRequest> splits;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentSplitRequest {
        private String paymentMethod;
        private BigDecimal amount;
        private String reference;
        private List<Long> saleIds;
    }
}