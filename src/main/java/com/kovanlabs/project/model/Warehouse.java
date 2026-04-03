package com.kovanlabs.project.model;

import jakarta.persistence.*;

@Entity
@Table(name = "warehouse")
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location;

    @OneToOne
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    public Warehouse() {}

    public Warehouse(String name, String location, Business business) {
        this.name = name;
        this.location = location;
        this.business = business;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public Business getBusiness() {
        return business;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }
}