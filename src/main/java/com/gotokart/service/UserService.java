package com.gotokart.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gotokart.model.User;
import com.gotokart.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    UserRepository repo;

    public User register(User user){
        return repo.save(user);
    }

}