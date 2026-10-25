package dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import enums.DebtStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerDebtStatusResponse {

    /** Cantidad de clientes en cada estado. */
    private Map<DebtStatus, Long> counts;
    private List<CustomerDebtInfo> customers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CustomerDebtInfo {
        private Long customerId;
        private String customerName;
        private DebtStatus status;
        private long pendingSales;
        private BigDecimal totalPending;
        /** Fecha de la venta pendiente mas vieja; null si no debe ninguna. */
        private LocalDate oldestPendingDate;
        private Long oldestPendingDays;
        private BigDecimal creditBalance;
    }
}
