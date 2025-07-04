package model.client.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    private Long id;

    private String name;
    
    @Override
    public String toString() {
    	return name;
    }
}