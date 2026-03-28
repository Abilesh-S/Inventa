package com.kovanlabs.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "online_customer")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "customer_name" , nullable = false)
    private String name;

    private String address;

    @Column(name = "phone_no" , nullable = false)
    private String phoneno;

    @OneToMany(mappedBy = "customer")
    private List<Order> order;

}
