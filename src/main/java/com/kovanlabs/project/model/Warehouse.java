package com.kovanlabs.project.model;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@NoArgsConstructor
@Table(name = "warehouse_list")
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne
    @JoinColumn(name = "business_id" , referencedColumnName = "id")
    private Business business;
    private String name;
    private String location;
    @Temporal(TemporalType.DATE)
    private Date createdDate;
    @OneToMany(mappedBy = "warehouse")
    private WarehouseInventory warehouseInventory;



    public Warehouse(int id, Business business, String name, String location, Date createdDate) {
        this.id = id;
        this.business = business;
        this.name = name;
        this.location = location;
        this.createdDate = createdDate;
    }
}
