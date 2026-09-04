package com.tenpearls.contactmanagement.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleEmailAlreadyRegistered_shouldReturnConflictWithMessage() {
        ResponseEntity<String> response = handler.handleEmailAlreadyRegistered(
                new EmailAlreadyRegisteredException("Email is already registered")
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Email is already registered", response.getBody());
    }

    @Test
    void handlePhoneAlreadyRegistered_shouldReturnConflictWithMessage() {
        ResponseEntity<String> response = handler.handlePhoneAlreadyRegistered(
                new PhoneAlreadyRegisteredException("Phone number is already registered")
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Phone number is already registered", response.getBody());
    }

    @Test
    void handleDataIntegrityViolation_shouldReturnEmailMessageForEmailConstraint() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "could not execute statement [ERROR: duplicate key value violates unique constraint \"uk_user_email\"]"
        );

        ResponseEntity<String> response = handler.handleDataIntegrityViolation(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Email is already registered", response.getBody());
    }

    @Test
    void handleDataIntegrityViolation_shouldReturnPhoneMessageForPhoneConstraint() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "could not execute statement [ERROR: duplicate key value violates unique constraint \"uk_user_phone_number\"]"
        );

        ResponseEntity<String> response = handler.handleDataIntegrityViolation(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Phone number is already registered", response.getBody());
    }
}
