package com.kovanlabs.project.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "business")
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Column(name = "location")
    private String location;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private boolean active = true;

    public Business(String name, String ownerName, String location) {
        this.name = name;
        this.ownerName = ownerName;
        this.location = location;
    }


//    @PrePersist
//    public void prePersist() {
//        this.createdAt = LocalDateTime.now();
//    }

}