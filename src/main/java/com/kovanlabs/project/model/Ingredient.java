package com.kovanlabs.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "ingredient" )
@NoArgsConstructor
@AllArgsConstructor
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
    private LocalDate created_date;

    @OneToMany(mappedBy = "ingredient")
    private List<WarehouseInventory> warehouseInventories;

    @OneToMany(mappedBy = "ingredient")
    private List<BranchInventory> branchInventories;

    @OneToMany(mappedBy = "ingredient")
    private List<StockTransferList> stockTransferLists;

}
