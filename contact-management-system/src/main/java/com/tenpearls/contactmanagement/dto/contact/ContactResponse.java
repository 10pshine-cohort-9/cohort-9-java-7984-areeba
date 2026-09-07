package com.tenpearls.contactmanagement.dto.contact;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String title;
    private List<ContactEmailResponse> emails = new ArrayList<>();
    private List<ContactPhoneResponse> phones = new ArrayList<>();
}
