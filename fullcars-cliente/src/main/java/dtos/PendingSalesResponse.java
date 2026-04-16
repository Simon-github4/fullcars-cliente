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
public class PendingSalesResponse {

    private Long customerId;
    private String customerName;
    private BigDecimal totalPending;
    private BigDecimal creditBalance;
    private List<SalePendingInfo> sales;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SalePendingInfo {
        private Long saleId;
        private LocalDate date;
        private BigDecimal total;
        private BigDecimal totalPaid;
        private BigDecimal remainingDue;
        private String cae;
        private String saleNumber;
    }
    
}
