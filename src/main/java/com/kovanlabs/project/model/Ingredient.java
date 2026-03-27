package com.kovanlabs.project.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "ingredient" )
@NoArgsConstructor
@Data
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne
    @JoinColumn(name = "tenant_id" , referencedColumnName = "id")
    private Business business;
    private String name ;
    private int units;
    private boolean isActive;
    @Temporal(TemporalType.DATE)
    private Date created_date;

    public Ingredient(long id, Business business, String productName, int quantity, boolean isActive, Date created_date) {
        this.id = id;
        this.business = business;
        this.name = productName;
        this.units = quantity;
        this.isActive = isActive;
        this.created_date = created_date;
    }
}
