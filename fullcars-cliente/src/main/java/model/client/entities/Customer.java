package model.client.entities;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer implements Comparable<Customer>{

    private Long id;
    
    private String fullName;
    private String dni;
    private String cuit;
    private String email;
    private String phone;
    private String adress;
    
    private BigDecimal creditBalance;

    @Override
    public String toString() {
    	return fullName;
    }

	@Override
	public int compareTo(Customer o) {
		return this.fullName.compareTo(o.getFullName());
	}
    
 

}