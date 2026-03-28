package com.kovanlabs.project.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "business")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "shop_name" , nullable = false)
    private String name;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    private String phone;

    private String email;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "business")
    private List<Branch> branch;

    @OneToMany(mappedBy = "business")
    private List<Ingredient> ingredient;

    @OneToMany(mappedBy = "business")
    private List<Order> orders;

}