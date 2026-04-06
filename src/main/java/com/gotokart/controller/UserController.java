package com.gotokart.controller;

import com.gotokart.config.JwtUtil;
import com.gotokart.model.User;
import com.gotokart.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil     jwtUtil;

    @GetMapping
    public List<User> getAllUser() { return userService.getAllUsers(); }

    @PostMapping("/register")
    public User register(@RequestBody User user) { return userService.register(user); }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) { return userService.getUserById(id); }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("No token provided");
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.isValid(token)) {
            return ResponseEntity.status(401).body("Invalid or expired token");
        }
        Long userId = jwtUtil.getUserId(token);
        return ResponseEntity.ok(userService.getUserById(userId));
    }
}