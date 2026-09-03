package com.tenpearls.contactmanagement.service;

import com.tenpearls.contactmanagement.dto.user.CurrentUserResponse;
import com.tenpearls.contactmanagement.entity.User;
import com.tenpearls.contactmanagement.exception.UserNotFoundException;
import com.tenpearls.contactmanagement.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "test@example.com",
                        null,
                        List.of()
                )
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_shouldReturnCurrentUserDetails() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPhoneNumber("1234567890");
        user.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        CurrentUserResponse response = userService.getCurrentUser();

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("1234567890", response.getPhoneNumber());
        assertEquals(LocalDateTime.of(2026, 1, 1, 10, 0), response.getCreatedAt());
    }

    @Test
    void getCurrentUser_shouldThrowWhenUserNotFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getCurrentUser());
    }
}
