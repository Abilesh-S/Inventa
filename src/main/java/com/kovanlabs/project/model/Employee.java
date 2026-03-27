package com.kovanlabs.project.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;


    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;


    public Employee() {}

    public Employee(String username, String password, Business business, Role role) {
        this.username = username;
        this.password = password;
        this.business = business;
        this.role = role;
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
    }

    public Long getId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public Business getBusiness() { return business; }
    public void setBusiness(Business business) { this.business = business; }

    public Branch getBranch() { return branch; }
    public void setBranch(Branch branch) { this.branch = branch; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}