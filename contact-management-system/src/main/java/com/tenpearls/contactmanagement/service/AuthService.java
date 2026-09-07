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
import com.tenpearls.contactmanagement.dto.auth.LoginRequest;
import com.tenpearls.contactmanagement.dto.auth.LoginResponse;
import java.util.Optional;
import com.tenpearls.contactmanagement.exception.InvalidCredentialsException;
import com.tenpearls.contactmanagement.security.JwtService;
import com.tenpearls.contactmanagement.util.EmailNormalizer;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public RegisterResponse register(RegisterRequest request) {

        logger.info("Registration attempt");
        String email = EmailNormalizer.normalize(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            logger.warn("Registration failed. Email already registered");
            throw new EmailAlreadyRegisteredException("Email is already registered");        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User();
        user.setEmail(email);
        user.setPassword(encodedPassword);

        User savedUser = userRepository.save(user);

        logger.info("User registered successfully with id: {}", savedUser.getId());

        RegisterResponse response = new RegisterResponse();
        response.setId(savedUser.getId());
        response.setEmail(savedUser.getEmail());

        return response;
    }
    public LoginResponse login(LoginRequest request) {

        logger.info("Login attempt for email: {}", request.getEmail());

        String email = EmailNormalizer.normalize(request.getEmail());
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            logger.warn("Login failed. User not found for email: {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.warn("Login failed. Invalid password for email: {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");        }

        logger.info("User logged in successfully with id: {}", user.getId());

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getId(),
                user.getTokenVersion()
        );

        return new LoginResponse(user.getId(), user.getEmail(), token);
    }
}