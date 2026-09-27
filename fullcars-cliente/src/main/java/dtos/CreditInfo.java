package dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Credito a favor del cliente con saldo disponible (devolucion o saldo por excedente de pago). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditInfo {

    public static final String TYPE_DEVOLUCION_NC = "DEVOLUCION_NC";
    public static final String TYPE_DEVOLUCION_INTERNA = "DEVOLUCION_INTERNA";
    public static final String TYPE_SALDO_A_FAVOR = "SALDO_A_FAVOR";
    public static final String TYPE_OTRO = "OTRO";

    private Long creditId;
    private LocalDate date;
    private BigDecimal amount;
    private BigDecimal amountUsed;
    private BigDecimal remaining;

    private String type;
    private String label;

    private Long devolucionId;
    private Long saleId;
    private String saleNumber;
    private String cae;
    private String nroNotaCredito;

    private Long payId;
}
