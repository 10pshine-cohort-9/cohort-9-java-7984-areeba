package com.tenpearls.contactmanagement.dto.contact;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UpdateContactRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String title;

    @Valid
    private List<@Valid ContactEmailRequest> emails = new ArrayList<>();

    @Valid
    private List<@Valid ContactPhoneRequest> phones = new ArrayList<>();
}
