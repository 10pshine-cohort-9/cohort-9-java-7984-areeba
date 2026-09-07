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

    @Test
    void handleDataIntegrityViolation_shouldReturnGenericMessageForForeignKeyViolation() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "could not execute statement [ERROR: insert or update on table \"contacts\" "
                        + "violates foreign key constraint \"fk_contact_user_id\"]"
        );

        ResponseEntity<String> response = handler.handleDataIntegrityViolation(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Could not complete request due to a data integrity violation", response.getBody());
    }

    @Test
    void handleDataIntegrityViolation_shouldReturnGenericMessageForNotNullViolation() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "could not execute statement [ERROR: null value in column \"first_name\" "
                        + "of relation \"contacts\" violates not-null constraint]"
        );

        ResponseEntity<String> response = handler.handleDataIntegrityViolation(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Could not complete request due to a data integrity violation", response.getBody());
    }

    @Test
    void handleDataIntegrityViolation_shouldNotMisclassifyEmailInForeignKeyMessage() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "could not execute statement [ERROR: update on table \"contact_emails\" "
                        + "violates foreign key constraint \"fk_contact_email_contact\"]"
        );

        ResponseEntity<String> response = handler.handleDataIntegrityViolation(exception);

        assertEquals("Could not complete request due to a data integrity violation", response.getBody());
    }
}
