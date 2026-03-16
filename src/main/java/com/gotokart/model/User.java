package com.gotokart.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")   // IMPORTANT FIX
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String password;

    // getters
    // setters
}