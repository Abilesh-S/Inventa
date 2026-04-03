package com.kovanlabs.project.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "warehouse_inventory")
public class WarehouseInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ingredientName;
    private Double quantity;

    private String unit;
    @ManyToOne
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    public WarehouseInventory() {}


}