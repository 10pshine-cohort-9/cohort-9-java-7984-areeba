package com.tenpearls.contactmanagement.controller;

import com.tenpearls.contactmanagement.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import com.tenpearls.contactmanagement.dto.auth.RegisterRequest;
import com.tenpearls.contactmanagement.dto.auth.RegisterResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import com.tenpearls.contactmanagement.exception.EmailAlreadyRegisteredException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.tenpearls.contactmanagement.dto.auth.LoginRequest;
import com.tenpearls.contactmanagement.dto.auth.LoginResponse;
import static org.mockito.Mockito.doThrow;
import com.tenpearls.contactmanagement.exception.GlobalExceptionHandler;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(authService);
    }

    @Test
    void register_shouldReturnRegistrationResponse() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123");

        RegisterResponse expectedResponse = new RegisterResponse();
        expectedResponse.setId(1L);
        expectedResponse.setEmail("test@example.com");

        when(authService.register(request)).thenReturn(expectedResponse);

        RegisterResponse actualResponse = authController.register(request);

        assertNotNull(actualResponse);
        assertEquals(1L, actualResponse.getId());
        assertEquals("test@example.com", actualResponse.getEmail());
    }
    @Test
    void register_shouldRejectDuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123");

        doThrow(new EmailAlreadyRegisteredException("Email is already registered"))
                .when(authService)
                .register(request);

        EmailAlreadyRegisteredException exception = assertThrows(
                EmailAlreadyRegisteredException.class,
                () -> authController.register(request)
        );

        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<String> response =
                handler.handleEmailAlreadyRegistered(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Email is already registered", response.getBody());
    }
    @Test
    void login_shouldReturnResponseForValidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123");

        LoginResponse expectedResponse = new LoginResponse(1L, "test@example.com");

        when(authService.login(request)).thenReturn(expectedResponse);

        LoginResponse response = authController.login(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("test@example.com", response.getEmail());
    }
}