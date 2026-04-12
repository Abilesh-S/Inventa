package com.kovanlabs.project.model;

import jakarta.persistence.*;
import lombok.Data;

import com.kovanlabs.project.model.Warehouse;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@Table(
        name = "warehouse_inventory",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"warehouse_id", "ingredient_name"}
        )
)
public class WarehouseInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ingredientName;
    private Double quantity;
    private Double threshold;
    private String unit;
    private Double pricePerUnit;
    private String batchNumber;
    private LocalDate expiryDate;
    private String status;

    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    public WarehouseInventory(String ingredientName,Double quantity,Double threshold,String unit,Warehouse warehouse){
        this.ingredientName=ingredientName;
        this.quantity=quantity;
        this.threshold=threshold;
        this.unit=unit;
        this.warehouse=warehouse;
    }
    public LocalDate getExpireDate(){
        return expiryDate;
    }
    public void setExpireDate(LocalDate expiryDate){
        this.expiryDate = expiryDate;
    }
}

