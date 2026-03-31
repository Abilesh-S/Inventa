package com.kovanlabs.project.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "ProductList")
@NoArgsConstructor
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id ;

    @Column(name = "businessId" )
    private long businessId;

    @Column(name = "productName", nullable = false , unique = true)
    private String productName;

    private String description;

    private float price ;

    private String category;

    private boolean isActive ;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdDate;

    @JsonIgnore
    @ToString.Exclude     // <--- You missed this one!
    @OneToMany(mappedBy = "productId")
    private List<Recipe> recipeList;

    public Product(long businessId, String productName, String description, float price, String category, boolean isActive, LocalDate createdDate) {
        this.businessId = businessId;
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.category = category;
        this.isActive = isActive;
        this.createdDate = createdDate;
    }
}
