package com.gotokart.controller;

import com.gotokart.model.User;
import com.gotokart.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<User> getAllUser() { return userService.getAllUsers(); }

    @PostMapping("/register")
    public User register(@RequestBody User user) { return userService.register(user); }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) { return userService.getUserById(id); }
}