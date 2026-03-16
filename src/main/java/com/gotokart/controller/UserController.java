package com.gotokart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gotokart.model.User;
import com.gotokart.services.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    UserService service;

    @PostMapping("/register")
    public User register(@RequestBody User user){
        return service.register(user);
    }
}