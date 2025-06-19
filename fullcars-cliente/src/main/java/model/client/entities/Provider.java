package model.client.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Provider {

	private Long id;

    private String companyName;
    private String cuit;
    private String phone;
    private String email;
    private String adress;
    //private String condicionIva;

}