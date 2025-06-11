package model.client.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.fullcars.restapi.enums.MovementType;

@Entity
@Data
@NoArgsConstructor
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Integer quantity;
    private LocalDateTime date;
    private String reference;//EJ: Sale #155
    private String observations;
    
    @ManyToOne
    private CarPart product;

    @Enumerated(EnumType.STRING)
    private MovementType type;

}