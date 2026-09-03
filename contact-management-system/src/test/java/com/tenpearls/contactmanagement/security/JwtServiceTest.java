package com.tenpearls.contactmanagement.security;

import com.tenpearls.contactmanagement.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("contact-management-system-jwt-secret-key-256-bits-minimum");
        jwtProperties.setExpirationMs(86400000);
        jwtService = new JwtService(jwtProperties);
    }

    @Test
    void generateToken_shouldCreateValidToken() {
        String token = jwtService.generateToken("test@example.com", 1L, 0);

        assertNotNull(token);
        assertEquals("test@example.com", jwtService.extractEmail(token));
        assertEquals(1L, jwtService.extractUserId(token));
        assertEquals(0, jwtService.extractTokenVersion(token));
        assertTrue(jwtService.isTokenValid(token, "test@example.com", 0));
    }

    @Test
    void isTokenValid_shouldRejectMismatchedEmail() {
        String token = jwtService.generateToken("test@example.com", 1L, 0);

        assertFalse(jwtService.isTokenValid(token, "other@example.com", 0));
    }

    @Test
    void isTokenValid_shouldRejectStaleTokenVersion() {
        String token = jwtService.generateToken("test@example.com", 1L, 0);

        assertFalse(jwtService.isTokenValid(token, "test@example.com", 1));
    }
}
