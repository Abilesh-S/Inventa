package com.kovanlabs.project.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "business")
@NoArgsConstructor
@Data
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    private String phone;

    private String email;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Business(String name, String ownerName, String phone, String email) {
        this.name = name;
        this.ownerName = ownerName;
        this.phone = phone;
        this.email = email;
        this.createdAt = LocalDateTime.now();
    }

}