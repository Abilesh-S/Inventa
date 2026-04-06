package com.kovanlabs.project.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "recipe")
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private String ingredientName;

    private Double quantity;


    private String unit;

    private Long ingredientId;


    public Long getId() { return id; }
    @JsonBackReference
    public Product getProduct() { return product; }
    public String getIngredientName() { return ingredientName; }
    public Double getQuantity() { return quantity; }
    public String getUnit() { return unit; }

    public void setId(Long id) { this.id = id; }
    public void setProduct(Product product) { this.product = product; }
    public void setIngredientName(String ingredientName) { this.ingredientName = ingredientName; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public void setUnit(String unit) { this.unit = unit; }
    public Long getIngredientId() { return ingredientId; }
    public void setIngredientId(Long ingredientId) { this.ingredientId = ingredientId; }
}