package com.tenpearls.contactmanagement.controller;

import com.tenpearls.contactmanagement.dto.user.CurrentUserResponse;
import com.tenpearls.contactmanagement.exception.GlobalExceptionHandler;
import com.tenpearls.contactmanagement.exception.UserNotFoundException;
import com.tenpearls.contactmanagement.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController(userService);
    }

    @Test
    void getCurrentUser_shouldReturnCurrentUserResponse() {
        CurrentUserResponse expectedResponse = new CurrentUserResponse(
                1L,
                "test@example.com",
                "1234567890",
                LocalDateTime.of(2026, 1, 1, 10, 0)
        );

        when(userService.getCurrentUser()).thenReturn(expectedResponse);

        CurrentUserResponse response = userController.getCurrentUser();

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("1234567890", response.getPhoneNumber());
    }

    @Test
    void getCurrentUser_shouldThrowWhenUserNotFound() {
        doThrow(new UserNotFoundException("User not found"))
                .when(userService)
                .getCurrentUser();

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userController.getCurrentUser()
        );

        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ResponseEntity<String> response = handler.handleUserNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody());
    }
}
