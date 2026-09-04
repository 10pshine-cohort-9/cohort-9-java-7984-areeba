package com.tenpearls.contactmanagement.dto.contact;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagedContactResponse {

    private List<ContactResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
