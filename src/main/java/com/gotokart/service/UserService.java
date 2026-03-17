package com.gotokart.service;

import com.gotokart.model.User;
import com.gotokart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User register(User user) { return userRepository.save(user); }
    public User getUserById(Long id) { return userRepository.findById(id).orElseThrow(); }
}