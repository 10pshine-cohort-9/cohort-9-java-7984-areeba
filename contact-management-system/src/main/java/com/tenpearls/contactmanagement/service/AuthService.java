package com.tenpearls.contactmanagement.service;

import com.tenpearls.contactmanagement.dto.auth.RegisterRequest;
import com.tenpearls.contactmanagement.dto.auth.RegisterResponse;
import com.tenpearls.contactmanagement.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.tenpearls.contactmanagement.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.tenpearls.contactmanagement.exception.EmailAlreadyRegisteredException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public RegisterResponse register(RegisterRequest request) {

        logger.info("Registration attempt for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Registration failed. Email already registered: {}", request.getEmail());

            throw new EmailAlreadyRegisteredException("Email is already registered");        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(encodedPassword);

        User savedUser = userRepository.save(user);

        logger.info("User registered successfully with id: {}", savedUser.getId());

        RegisterResponse response = new RegisterResponse();
        response.setId(savedUser.getId());
        response.setEmail(savedUser.getEmail());

        return response;
    }
}