package model.client.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Brand {

    private Long id;
    private String name;
    
    @Override
    public String toString() {
    	return name;
    }
}