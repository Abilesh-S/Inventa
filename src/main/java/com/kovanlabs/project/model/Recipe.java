package com.kovanlabs.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@NoArgsConstructor
@Data
@AllArgsConstructor
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne()
    @JoinColumn(name = "ingredient_id" , referencedColumnName = "id")
    private Ingredient ingredient;

    @ManyToOne()
    @JoinColumn(name = "product_id" ,referencedColumnName = "id")
    private Product product;

    @Column()
    private int quantity;
}
