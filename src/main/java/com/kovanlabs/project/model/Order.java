package com.kovanlabs.project.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;


@Entity
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "tenant_id" , referencedColumnName = "id")
    private Business business;

    @ManyToOne
    @JoinColumn(name = "branch_id" , referencedColumnName = "id")
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "employee_id" , referencedColumnName = "id")
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "customer_id" , referencedColumnName = "id" , nullable = false)
    private Customer customer;

    private int total_amount ;

    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private LocalDate created_at ;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItem;

    @OneToMany(mappedBy = "order")
    private List<Payment> payment;

}
