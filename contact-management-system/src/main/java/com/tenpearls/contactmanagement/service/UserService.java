package com.tenpearls.contactmanagement.service;

import com.tenpearls.contactmanagement.dto.user.CurrentUserResponse;
import com.tenpearls.contactmanagement.entity.User;
import com.tenpearls.contactmanagement.exception.UserNotFoundException;
import com.tenpearls.contactmanagement.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public CurrentUserResponse getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return new CurrentUserResponse(
                user.getId(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getCreatedAt()
        );
    }
}
