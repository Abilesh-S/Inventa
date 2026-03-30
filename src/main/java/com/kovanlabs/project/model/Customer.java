package com.kovanlabs.project.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customer")
@NoArgsConstructor
@Data
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "email" , unique = true , nullable = false)
    private String emailId ;

    @Column(name = "address" )
    private String address ;

    @Column(name = "phoneNumber")
    private String phoneNumber;

    @OneToOne
    @JoinColumn(name = "loginId" )
    private LoginCredentials loginCredentials;

    public Customer(String emailId, String address, String phoneNumber) {
        this.emailId = emailId;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }
}
