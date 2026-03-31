package com.kovanlabs.project.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "Ingredients")
@NoArgsConstructor
@Data
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "businessId")
    private long businessId;

    @Column(unique = true , nullable = false)
    private String ingredientName ;

    private int quantity ;

    @Column(name = "created_At")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdAt;

    private boolean isActive;

    @JsonIgnore
    @OneToMany(mappedBy = "ingredientId")
    private List<Recipe> recipe;

    public Ingredient(long businessId, String ingredientName, int quantity, LocalDate createdAt, boolean isActive) {
        this.businessId = businessId;
        this.ingredientName = ingredientName;
        this.quantity = quantity;
        this.createdAt = createdAt;
        this.isActive = isActive;
    }
}
