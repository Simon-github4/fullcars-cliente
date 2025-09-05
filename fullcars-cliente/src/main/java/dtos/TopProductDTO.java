package dtos;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TopProductDTO {

    private String sku;
    private String nombre;
    private Long cantidadVendidos;
    private BigDecimal ingresosTotales;

    public TopProductDTO(String sku, String nombre, Long cantidadVendidos, BigDecimal ingresosTotales) {
        this.sku = sku;
        this.nombre = nombre;
        this.cantidadVendidos = cantidadVendidos;
        this.ingresosTotales = ingresosTotales;
    }

}
