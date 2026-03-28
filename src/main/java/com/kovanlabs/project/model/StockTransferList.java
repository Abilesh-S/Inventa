package com.kovanlabs.project.model;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "Stock_Transfer")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class StockTransferList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "warehouse_id" , referencedColumnName = "id")
    private Warehouse warehouse;

    @ManyToOne
    @JoinColumn(name = "branch_id" , referencedColumnName = "id")
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "ingredient_id" , referencedColumnName = "id")
    private Ingredient ingredient;

    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status")
    private Status status;

    private LocalDate created_at;
}
