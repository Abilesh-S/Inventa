package com.kovanlabs.project.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@NoArgsConstructor
@Data
public class Recipe {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private long id ;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "productId" , referencedColumnName = "id")
    private Product productId;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "ingredientId" ,referencedColumnName = "id")
    private Ingredient ingredientId ;

    private int quantity ;
}
