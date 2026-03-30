package com.kovanlabs.project.model;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@NoArgsConstructor
@Data
public class LoginCredentials {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "username" , nullable = false)
    private String userName ;

    @Column(nullable = false)
    private String password;

    @OneToOne(mappedBy = "loginCredentials" )
    private Customer customerId ;

    @OneToOne(mappedBy = "loginId")
    private User userId ;

    public LoginCredentials(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }
}
