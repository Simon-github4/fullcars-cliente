package model.client.entities;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pay {

    private Long id;
    private int amount;
    private LocalDate date;
    private String paymentMethod;
    
    private Customer customer;
    
    private Sale sale;//not confirmed yet

}
