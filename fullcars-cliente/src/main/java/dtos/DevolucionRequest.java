package dtos;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DevolucionRequest {

    private Long saleId;
    private String motivo;
    private Boolean emitirCAE;
    private BigDecimal monto;
    private List<DevolucionDetailRequest> detalles;
    
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DevolucionDetailRequest {
        private Long saleDetailId;
        private int cantidadDevuelta;
    }
}
