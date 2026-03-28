package com.kovanlabs.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "products")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "tenant_id" ,referencedColumnName = "id")
    private Business business;

    @Column(name = "product_name" , nullable = false)
    private String name;

    @Column(name = "product_description" )
    private String description;

    private String category ;

    private boolean is_active;

    private LocalDate created_date;

    @OneToMany(mappedBy = "product")
    private List<OrderItem> orderItem;

}
