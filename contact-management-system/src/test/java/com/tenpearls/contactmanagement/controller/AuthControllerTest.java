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
import static org.mockito.Mockito.when;

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
}