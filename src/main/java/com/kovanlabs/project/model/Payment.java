package com.kovanlabs.project.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "payment")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id ;

    @ManyToOne
    @JoinColumn(name = "order_id" , referencedColumnName = "id" , nullable = false)
    private Order order;

    private int amount;

    @Column(name = "mode")
    @Enumerated(EnumType.STRING)
    private ModeOfPayment modeOfPayment;

    @Column(name = "status" , nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "created_date" ,nullable = false)
    private LocalDate createdDate;

}
