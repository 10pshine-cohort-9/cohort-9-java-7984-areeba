package com.tenpearls.contactmanagement.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {

    private Long id;
    private String email;
    private String token;

    public LoginResponse(Long id, String email, String token) {
        this.id = id;
        this.email = email;
        this.token = token;
    }
}