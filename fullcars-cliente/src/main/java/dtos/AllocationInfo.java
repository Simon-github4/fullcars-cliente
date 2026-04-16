package dtos;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllocationInfo {
    private Long saleId;
    private BigDecimal saleTotal;
    private BigDecimal amountApplied;
    private Boolean isCredit;
}
