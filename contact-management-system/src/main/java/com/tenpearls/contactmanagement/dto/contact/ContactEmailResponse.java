package com.tenpearls.contactmanagement.dto.contact;

import com.tenpearls.contactmanagement.entity.enums.EmailType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactEmailResponse {

    private Long id;
    private String email;
    private EmailType type;
}
