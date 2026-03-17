package com.gotokart.controller;

import com.gotokart.model.User;
import com.gotokart.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public User register(@RequestBody User user) { return userService.register(user); }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) { return userService.getUserById(id); }
}