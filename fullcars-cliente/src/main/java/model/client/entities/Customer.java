package model.client.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    private Long id;
    
    private long balance;
    private String fullName;
    private String dni;
    private String cuit;
    private String email;
    private String phone;
    private String adress;

    @Override
    public String toString() {
    	return fullName;
    }
}