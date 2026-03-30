package com.kovanlabs.project.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "userdetails")
@NoArgsConstructor
@Data
public class User {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private long id;

    @Column(name = "email")
    private String email;

    @Column(name = "address")
    private String address ;

    @OneToOne
    @JoinColumn(name = "loginId" , referencedColumnName = "id")
    private LoginCredentials loginId;

    public User(String email, String address){
        this.email = email;
        this.address = address;
    }
}
