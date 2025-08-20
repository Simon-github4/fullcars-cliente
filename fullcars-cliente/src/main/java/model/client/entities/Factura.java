package model.client.entities;

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
    
	private Sale purchase;
	
	private String fileUrl;
	private String fullNameSnapshot;
	private String adressSnapshot;
	private String cuitSnapshot;	
	
}
