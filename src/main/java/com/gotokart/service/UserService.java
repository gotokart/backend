package com.gotokart.service;

import com.gotokart.model.User;
import com.gotokart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    private static final Set<String> VALID_ROLES = Set.of("ADMIN", "USER");

    public User register(User user) { return userRepository.save(user); }
    public User getUserById(Long id) { return userRepository.findById(id).orElseThrow(); }
    public List<User> getAllUsers() { return userRepository.findAll(); }

    public User updateRole(Long id, String role) {
        if (role == null) throw new RuntimeException("Role is required");
        String normalised = role.trim().toUpperCase();
        if (!VALID_ROLES.contains(normalised)) {
            throw new RuntimeException("Invalid role. Allowed: " + VALID_ROLES);
        }
        User user = getUserById(id);
        user.setRole(normalised);
        return userRepository.save(user);
    }

    public User setActive(Long id, boolean active) {
        User user = getUserById(id);
        user.setActive(active);
        return userRepository.save(user);
    }
}
