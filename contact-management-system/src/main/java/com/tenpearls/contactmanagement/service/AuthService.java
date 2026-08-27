package com.tenpearls.contactmanagement.service;

import com.tenpearls.contactmanagement.dto.auth.RegisterRequest;
import com.tenpearls.contactmanagement.dto.auth.RegisterResponse;
import com.tenpearls.contactmanagement.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.tenpearls.contactmanagement.entity.User;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(encodedPassword);

        User savedUser = userRepository.save(user);

        RegisterResponse response = new RegisterResponse();
        response.setId(savedUser.getId());
        response.setEmail(savedUser.getEmail());

        return response;
    }
}