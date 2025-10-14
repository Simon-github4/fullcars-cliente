package model.client.entities;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderPart {

    //private Long id;

    private Long providerId;

    private String nombre;
    private String marca;
    private BigDecimal precio;

	private String provCod;
	private String quality;
	private String category;
}
