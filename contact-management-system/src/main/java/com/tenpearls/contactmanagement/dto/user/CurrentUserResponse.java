package com.tenpearls.contactmanagement.dto.user;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CurrentUserResponse {

    private Long id;
    private String email;
    private String phoneNumber;
    private LocalDateTime createdAt;

    public CurrentUserResponse(Long id, String email, String phoneNumber, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.createdAt = createdAt;
    }
}
