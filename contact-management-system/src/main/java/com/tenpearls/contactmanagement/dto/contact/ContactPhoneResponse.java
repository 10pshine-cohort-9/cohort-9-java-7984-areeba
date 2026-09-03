package com.tenpearls.contactmanagement.dto.contact;

import com.tenpearls.contactmanagement.entity.enums.PhoneType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactPhoneResponse {

    private Long id;
    private String phoneNumber;
    private PhoneType type;
}
