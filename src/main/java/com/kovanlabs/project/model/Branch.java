package com.kovanlabs.project.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "branch")
@NoArgsConstructor
@Data
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(nullable = false)
    private String name;

    private String location;

    private String phone;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "branch")
    private List<StockTransferList> stockTransferList;

    @OneToMany(mappedBy = "branch")
    private List<Order> orders;



    public Branch(String name, String location, String phone, Business business) {
        this.name = name;
        this.location = location;
        this.phone = phone;
        this.business = business;
        this.createdAt = LocalDateTime.now();
    }

}