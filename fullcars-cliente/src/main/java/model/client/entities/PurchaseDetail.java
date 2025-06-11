package model.client.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@NoArgsConstructor
public class PurchaseDetail extends Detail{

    @ManyToOne
    private Purchase purchase;

}