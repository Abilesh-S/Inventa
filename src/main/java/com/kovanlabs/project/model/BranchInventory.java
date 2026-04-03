package com.kovanlabs.project.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor


@Table(
        name = "branch_inventory",
        uniqueConstraints = @UniqueConstraint(columnNames = {"branch_id", "ingredient_name"})
)
public class BranchInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ingredientName;
    private Double quantity;
    private Double threshold;
    private String unit;
    private String batchNumber;
    private LocalDate expiryDate;
    private String status;
    @ManyToOne
    @JoinColumn(name = "branch_id")
    @ToString.Exclude
    private Branch branch;

    public BranchInventory(String ingredientName,Double quantity,Double threshold,String unit,Branch branch){
        this.ingredientName=ingredientName;
        this.quantity=quantity;
        this.threshold=threshold;
        this.unit=unit;
        this.branch=branch;
    }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

}