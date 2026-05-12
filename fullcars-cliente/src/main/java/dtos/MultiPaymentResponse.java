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
public class MultiPaymentResponse {

    private Long paymentId;
    private Long customerId;
    private BigDecimal totalAmount;
    private BigDecimal creditUsed;
    private BigDecimal creditGenerated;
    private BigDecimal customerCreditBalance;
    private LocalDate date;
    private String description;
    private List<PaymentSplitResponse> splits;
    private List<SaleUpdate> salesUpdated;
    private String summary;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentSplitResponse {
        private Long splitId;
        private String paymentMethod;
        private BigDecimal amount;
        private String reference;
        private List<String> salesCovered;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SaleUpdate {
        private Long saleId;
        private BigDecimal total;
        private BigDecimal totalPaid;
        private BigDecimal remainingDue;
        private boolean paid;
    }
}