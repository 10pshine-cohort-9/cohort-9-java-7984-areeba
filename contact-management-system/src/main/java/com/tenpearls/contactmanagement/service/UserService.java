package com.tenpearls.contactmanagement.service;

import com.tenpearls.contactmanagement.dto.user.ChangePasswordRequest;
import com.tenpearls.contactmanagement.dto.user.CurrentUserResponse;
import com.tenpearls.contactmanagement.dto.user.UpdateProfileRequest;
import com.tenpearls.contactmanagement.entity.User;
import com.tenpearls.contactmanagement.exception.EmailAlreadyRegisteredException;
import com.tenpearls.contactmanagement.exception.PhoneAlreadyRegisteredException;
import com.tenpearls.contactmanagement.exception.InvalidCredentialsException;
import com.tenpearls.contactmanagement.exception.UserNotFoundException;
import com.tenpearls.contactmanagement.repository.UserRepository;
import com.tenpearls.contactmanagement.util.EmailNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public CurrentUserResponse getCurrentUser() {
        String email = EmailNormalizer.normalize(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return new CurrentUserResponse(
                user.getId(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getCreatedAt()
        );
    }

    public CurrentUserResponse updateProfile(UpdateProfileRequest request) {
        String currentEmail = EmailNormalizer.normalize(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String newEmail = EmailNormalizer.normalize(request.getEmail());
        if (!newEmail.equals(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
            throw new EmailAlreadyRegisteredException("Email is already registered");
        }

        String phoneNumber = request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null;
        if (phoneNumber != null && phoneNumber.isEmpty()) {
            phoneNumber = null;
        }

        if (phoneNumber != null
                && !phoneNumber.equals(user.getPhoneNumber())
                && userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new PhoneAlreadyRegisteredException("Phone number is already registered");
        }

        if (!newEmail.equals(user.getEmail())) {
            user.setEmail(newEmail);
            user.setTokenVersion(user.getTokenVersion() + 1);
        }

        user.setPhoneNumber(phoneNumber);
        userRepository.save(user);

        logger.info("Profile updated for user id: {}", user.getId());

        return new CurrentUserResponse(
                user.getId(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getCreatedAt()
        );
    }

    public void changePassword(ChangePasswordRequest request) {
        String email = EmailNormalizer.normalize(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            logger.warn("Password change failed. Invalid current password for email: {}", email);
            throw new InvalidCredentialsException("Invalid current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);

        logger.info("Password changed successfully for user id: {}", user.getId());
    }
}
