package com.tenpearls.contactmanagement.controller;

import com.tenpearls.contactmanagement.dto.user.CurrentUserResponse;
import com.tenpearls.contactmanagement.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public CurrentUserResponse getCurrentUser() {
        return userService.getCurrentUser();
    }
}
