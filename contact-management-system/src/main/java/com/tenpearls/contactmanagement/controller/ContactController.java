package com.tenpearls.contactmanagement.controller;

import com.tenpearls.contactmanagement.dto.contact.ContactImportResponse;
import com.tenpearls.contactmanagement.dto.contact.ContactResponse;
import com.tenpearls.contactmanagement.dto.contact.CreateContactRequest;
import com.tenpearls.contactmanagement.dto.contact.PagedContactResponse;
import com.tenpearls.contactmanagement.dto.contact.UpdateContactRequest;
import com.tenpearls.contactmanagement.service.ContactCsvService;
import com.tenpearls.contactmanagement.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final ContactService contactService;
    private final ContactCsvService contactCsvService;

    public ContactController(ContactService contactService, ContactCsvService contactCsvService) {
        this.contactService = contactService;
        this.contactCsvService = contactCsvService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContactResponse createContact(@Valid @RequestBody CreateContactRequest request) {
        return contactService.createContact(request);
    }

    @GetMapping("/{id}")
    public ContactResponse getContact(@PathVariable Long id) {
        return contactService.getContact(id);
    }

    @PutMapping("/{id}")
    public ContactResponse updateContact(
            @PathVariable Long id,
            @Valid @RequestBody UpdateContactRequest request
    ) {
        return contactService.updateContact(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteContact(@PathVariable Long id) {
        contactService.deleteContact(id);
    }

    @GetMapping
    public PagedContactResponse listContacts(
            @PageableDefault(size = 10, sort = "lastName", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return contactService.listContacts(pageable);
    }

    @GetMapping("/search")
    public PagedContactResponse searchContacts(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @PageableDefault(size = 10, sort = "lastName", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return contactService.searchContacts(firstName, lastName, pageable);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportContacts() {
        String csv = contactCsvService.exportContactsCsv();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"contacts.csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ContactImportResponse importContacts(@RequestParam("file") MultipartFile file) {
        return contactCsvService.importContactsCsv(file);
    }
}
