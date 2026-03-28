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
    private long id;

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

    @Column(name = "product_quantity")
    private int quantity;

    @Column(name = "product_price")
    private int price;

    @OneToMany(mappedBy = "product")
    private List<Recipe> recipes;

    @OneToMany(mappedBy = "product")
    private List<OrderItem> orderItem;

}
