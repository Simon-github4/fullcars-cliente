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
public class SaleUpdate {
    private Long saleId;
    private BigDecimal total;
    private BigDecimal totalPaid;
    private BigDecimal remainingDue;
    private boolean paid;
}
