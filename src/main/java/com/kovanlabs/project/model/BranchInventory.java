package com.kovanlabs.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "branch_inventory")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class BranchInventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "branch_id" , referencedColumnName = "id")
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "ingredient_id" , referencedColumnName = "id")
    private Ingredient ingredient;

    @Column(name = "quantity" , nullable = false)
    private int quantity;

}
