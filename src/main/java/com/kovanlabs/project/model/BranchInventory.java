package com.kovanlabs.project.model;

import jakarta.persistence.*;

@Entity
@Table(name = "branch_inventory")
public class BranchInventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    @JoinColumn(name = "branch_id" , referencedColumnName = "id")
    private Branch branch;
}
