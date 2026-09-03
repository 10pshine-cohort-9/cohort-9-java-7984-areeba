package com.tenpearls.contactmanagement.controller;

import com.tenpearls.contactmanagement.dto.user.ChangePasswordRequest;
import com.tenpearls.contactmanagement.dto.user.CurrentUserResponse;
import com.tenpearls.contactmanagement.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
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
    void changePassword_shouldDelegateToUserService() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("OldPassword123");
        request.setNewPassword("NewPassword456");

        userController.changePassword(request);

        verify(userService).changePassword(request);
    }
}
