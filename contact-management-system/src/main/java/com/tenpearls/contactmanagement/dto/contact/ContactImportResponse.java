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
public class ContactImportResponse {

    private int importedCount;
    private int failedCount;
    private List<ContactImportError> errors = new ArrayList<>();
}
