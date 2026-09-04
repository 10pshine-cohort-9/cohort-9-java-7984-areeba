package com.tenpearls.contactmanagement.dto.contact;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactImportError {

    private int row;
    private String message;
}
