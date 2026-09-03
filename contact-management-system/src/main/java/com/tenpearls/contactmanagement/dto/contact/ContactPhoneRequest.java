package com.tenpearls.contactmanagement.dto.contact;

import com.tenpearls.contactmanagement.entity.enums.PhoneType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactPhoneRequest {

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotNull(message = "Phone type is required")
    private PhoneType type;
}
