package com.kovanlabs.project.model;

import jakarta.persistence.*;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long branchId;

    private Long productId;

    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    public Long getId() {
        return id;
    }
    public Long getBranchId() {
        return branchId;
    }
    public Long getProductId() {
        return productId;
    }
    public Integer getQuantity() {
        return quantity;
    }
    public Customer getCustomer() {
        return customer;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}