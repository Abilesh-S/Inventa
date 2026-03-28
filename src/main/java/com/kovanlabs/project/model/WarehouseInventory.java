package com.kovanlabs.project.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse_inventory")
@NoArgsConstructor
@Data
public class WarehouseInventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "warehouse_id" , referencedColumnName = "id")
    private Warehouse warehouse;

    @ManyToOne
    @JoinColumn(name = "ingredient_id" , referencedColumnName = "id")
    private Ingredient ingredient;

    private int quantity;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    public WarehouseInventory(Warehouse warehouse, Ingredient ingredient, int quantity) {
        this.warehouse = warehouse;
        this.ingredient = ingredient;
        this.quantity = quantity;
    }
}
