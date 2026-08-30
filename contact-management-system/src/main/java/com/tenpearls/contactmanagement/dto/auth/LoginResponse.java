package com.tenpearls.contactmanagement.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {

    private Long id;
    private String email;

    public LoginResponse(Long id, String email) {
        this.id = id;
        this.email = email;
    }
}