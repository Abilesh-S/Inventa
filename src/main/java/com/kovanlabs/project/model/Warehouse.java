package com.kovanlabs.project.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "warehouse_list")
@NoArgsConstructor
@Data
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "business_id" , referencedColumnName = "id")
    private Business business;

    private String name;

    private String location;

    private LocalDate createdDate;

    @OneToMany(mappedBy = "warehouse")
    private List<WarehouseInventory> warehouseInventory;

    @OneToMany(mappedBy = "warehouse")
    private List<StockTransferList> stockTransferLists;


    public Warehouse(Business business, String name, String location, LocalDate createdDate) {
        this.business = business;
        this.name = name;
        this.location = location;
        this.createdDate = createdDate;
    }
}
