package com.tenpearls.contactmanagement.service;

import com.tenpearls.contactmanagement.dto.contact.ContactEmailRequest;
import com.tenpearls.contactmanagement.dto.contact.ContactPhoneRequest;
import com.tenpearls.contactmanagement.dto.contact.CreateContactRequest;
import com.tenpearls.contactmanagement.dto.contact.PagedContactResponse;
import com.tenpearls.contactmanagement.dto.contact.UpdateContactRequest;
import com.tenpearls.contactmanagement.entity.Contact;
import com.tenpearls.contactmanagement.entity.ContactEmail;
import com.tenpearls.contactmanagement.entity.ContactPhone;
import com.tenpearls.contactmanagement.entity.User;
import com.tenpearls.contactmanagement.entity.enums.EmailType;
import com.tenpearls.contactmanagement.entity.enums.PhoneType;
import com.tenpearls.contactmanagement.exception.ContactNotFoundException;
import com.tenpearls.contactmanagement.repository.ContactEmailRepository;
import com.tenpearls.contactmanagement.repository.ContactPhoneRepository;
import com.tenpearls.contactmanagement.repository.ContactRepository;
import com.tenpearls.contactmanagement.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private ContactEmailRepository contactEmailRepository;

    @Mock
    private ContactPhoneRepository contactPhoneRepository;

    @Mock
    private UserRepository userRepository;

    private ContactService contactService;

    private User user;

    @BeforeEach
    void setUp() {
        contactService = new ContactService(
                contactRepository,
                contactEmailRepository,
                contactPhoneRepository,
                userRepository
        );

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test@example.com", null, List.of())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createContact_shouldPersistContactWithEmailsAndPhones() {
        CreateContactRequest request = buildCreateRequest();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(contactRepository.save(any(Contact.class))).thenAnswer(invocation -> {
            Contact contact = invocation.getArgument(0);
            contact.setId(10L);
            return contact;
        });
        when(contactRepository.findByIdAndUserId(10L, 1L)).thenAnswer(invocation -> {
            Contact contact = buildContactWithDetails(10L);
            return Optional.of(contact);
        });

        var response = contactService.createContact(request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals(1, response.getEmails().size());
        assertEquals(1, response.getPhones().size());

        verify(contactEmailRepository).save(any(ContactEmail.class));
        verify(contactPhoneRepository).save(any(ContactPhone.class));
    }

    @Test
    void getContact_shouldReturnContactForOwner() {
        Contact contact = buildContactWithDetails(5L);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(contactRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(contact));

        var response = contactService.getContact(5L);

        assertEquals(5L, response.getId());
        assertEquals("John", response.getFirstName());
    }

    @Test
    void getContact_shouldThrowWhenContactNotFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(contactRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(ContactNotFoundException.class, () -> contactService.getContact(99L));
    }

    @Test
    void updateContact_shouldReplaceEmailsAndPhones() {
        UpdateContactRequest request = new UpdateContactRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setTitle("Manager");

        ContactEmailRequest emailRequest = new ContactEmailRequest();
        emailRequest.setEmail("jane@example.com");
        emailRequest.setType(EmailType.WORK);
        request.setEmails(List.of(emailRequest));
        request.setPhones(List.of());

        Contact existingContact = buildContactWithDetails(7L);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(contactRepository.findByIdAndUserId(7L, 1L)).thenReturn(Optional.of(existingContact));
        when(contactRepository.save(any(Contact.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = contactService.updateContact(7L, request);

        assertEquals("Jane", response.getFirstName());
        assertEquals("Smith", response.getLastName());
        verify(contactEmailRepository).deleteByContactId(7L);
        verify(contactPhoneRepository).deleteByContactId(7L);
    }

    @Test
    void deleteContact_shouldDeleteOwnedContact() {
        Contact contact = buildContactWithDetails(3L);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(contactRepository.findByIdAndUserId(3L, 1L)).thenReturn(Optional.of(contact));

        contactService.deleteContact(3L);

        verify(contactEmailRepository).deleteByContactId(3L);
        verify(contactPhoneRepository).deleteByContactId(3L);
        verify(contactRepository).delete(contact);
    }

    @Test
    void listContacts_shouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Contact contact = buildContactWithDetails(1L);
        Page<Contact> page = new PageImpl<>(List.of(contact), pageable, 1);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(contactRepository.findByUserId(1L, pageable)).thenReturn(page);

        PagedContactResponse response = contactService.listContacts(pageable);

        assertEquals(1, response.getContent().size());
        assertEquals(1, response.getTotalElements());
    }

    @Test
    void searchContacts_shouldSearchByFirstAndLastName() {
        Pageable pageable = PageRequest.of(0, 10);
        Contact contact = buildContactWithDetails(1L);
        Page<Contact> page = new PageImpl<>(List.of(contact), pageable, 1);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(contactRepository.searchByUserId(eq(1L), eq("John"), eq("Doe"), eq(pageable))).thenReturn(page);

        PagedContactResponse response = contactService.searchContacts("John", "Doe", pageable);

        assertEquals(1, response.getContent().size());
        assertEquals("John", response.getContent().get(0).getFirstName());
    }

    private CreateContactRequest buildCreateRequest() {
        CreateContactRequest request = new CreateContactRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setTitle("Engineer");

        ContactEmailRequest emailRequest = new ContactEmailRequest();
        emailRequest.setEmail("john@example.com");
        emailRequest.setType(EmailType.WORK);

        ContactPhoneRequest phoneRequest = new ContactPhoneRequest();
        phoneRequest.setPhoneNumber("1234567890");
        phoneRequest.setType(PhoneType.HOME);

        request.setEmails(List.of(emailRequest));
        request.setPhones(List.of(phoneRequest));

        return request;
    }

    private Contact buildContactWithDetails(Long id) {
        Contact contact = new Contact();
        contact.setId(id);
        contact.setFirstName("John");
        contact.setLastName("Doe");
        contact.setTitle("Engineer");
        contact.setUser(user);

        ContactEmail email = new ContactEmail();
        email.setId(100L);
        email.setEmail("john@example.com");
        email.setType(EmailType.WORK);
        email.setContact(contact);

        ContactPhone phone = new ContactPhone();
        phone.setId(200L);
        phone.setPhoneNumber("1234567890");
        phone.setType(PhoneType.HOME);
        phone.setContact(contact);

        contact.setEmails(new ArrayList<>(List.of(email)));
        contact.setPhones(new ArrayList<>(List.of(phone)));

        return contact;
    }
}
