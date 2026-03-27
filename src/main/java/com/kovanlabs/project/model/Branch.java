package com.kovanlabs.project.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "branch")
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String location;

    private String phone;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    public Branch(String name, String location, String phone, Business business) {
        this.name = name;
        this.location = location;
        this.phone = phone;
        this.business = business;
        this.createdAt = LocalDateTime.now();
    }

}