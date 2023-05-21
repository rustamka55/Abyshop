package com.web.jwtauth.models;

import lombok.Data;

import javax.persistence.*;
import java.time.Instant;

@Entity(name = "refreshtoken")
@Data
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id",referencedColumnName = "id")
    private User user;

    @Column(nullable = false,unique = true)
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;

}
