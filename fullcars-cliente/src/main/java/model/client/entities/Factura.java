package model.client.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import enums.Conceptos;
import enums.CondicionIva;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Factura {

private Long id;
    
	private Sale sale;
    
    private String fileUrl;
	
    private Long cuitEmisor;
    
    private Integer puntoVenta;

    private Integer tipoComprobante;//obt. codigo del ENUM TiposComprobante

    private Long numeroComprobante;

    private LocalDate fechaEmision;

    private Conceptos concepto;

    private Long cuitCliente;

    private Integer tipoDocCliente; // 80, 96, 99 (obt. del ENUM TipoDocumento)

    private String razonSocialCliente;

    private String domicilioCliente;

    private CondicionIva condicionIvaCliente;

    private BigDecimal impNeto; // Gravado

    //private BigDecimal impExento;

    private BigDecimal impIva;

    private BigDecimal impTributos = BigDecimal.ZERO; // IIBB, etc.

    private BigDecimal impTotal;

    private String cae;

    private LocalDate vtoCae;

    private String resultadoAfip;

    private String observaciones;

    private LocalDate fechaVencimientoPago;
	
}
