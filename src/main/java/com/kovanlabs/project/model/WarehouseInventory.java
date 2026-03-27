package com.kovanlabs.project.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    public WarehouseInventory(long id, Warehouse warehouse, Ingredient ingredient, int quantity) {
        this.id = id;
        this.warehouse = warehouse;
        this.ingredient = ingredient;
        this.quantity = quantity;
    }
}
