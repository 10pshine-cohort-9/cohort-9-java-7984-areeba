package com.tenpearls.contactmanagement.service;

import com.tenpearls.contactmanagement.dto.contact.ContactResponse;
import com.tenpearls.contactmanagement.dto.contact.CreateContactRequest;
import com.tenpearls.contactmanagement.entity.Contact;
import com.tenpearls.contactmanagement.entity.ContactEmail;
import com.tenpearls.contactmanagement.entity.ContactPhone;
import com.tenpearls.contactmanagement.entity.User;
import com.tenpearls.contactmanagement.entity.enums.EmailType;
import com.tenpearls.contactmanagement.entity.enums.PhoneType;
import com.tenpearls.contactmanagement.exception.InvalidCsvFileException;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactCsvServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private ContactEmailRepository contactEmailRepository;

    @Mock
    private ContactPhoneRepository contactPhoneRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ContactService contactService;

    private ContactCsvService contactCsvService;

    private User user;

    @BeforeEach
    void setUp() {
        contactCsvService = new ContactCsvService(
                contactRepository,
                contactEmailRepository,
                contactPhoneRepository,
                userRepository,
                contactService
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
    void exportContactsCsv_shouldReturnHeaderAndRows() {
        Contact contact = new Contact();
        contact.setId(1L);
        contact.setFirstName("John");
        contact.setLastName("Doe");
        contact.setTitle("Engineer");
        contact.setUser(user);

        ContactEmail email = new ContactEmail();
        email.setEmail("john@example.com");
        email.setType(EmailType.WORK);
        email.setContact(contact);

        ContactPhone phone = new ContactPhone();
        phone.setPhoneNumber("1234567890");
        phone.setType(PhoneType.HOME);
        phone.setContact(contact);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(contactRepository.findAllByUserIdOrderByLastNameAscFirstNameAsc(1L)).thenReturn(List.of(contact));
        when(contactEmailRepository.findByContact_IdIn(List.of(1L))).thenReturn(List.of(email));
        when(contactPhoneRepository.findByContact_IdIn(List.of(1L))).thenReturn(List.of(phone));

        String csv = contactCsvService.exportContactsCsv();

        assertTrue(csv.startsWith("firstName,lastName,title,email,emailType,phone,phoneType"));
        assertTrue(csv.contains("John,Doe,Engineer,john@example.com,WORK,1234567890,HOME"));
    }

    @Test
    void importContactsCsv_withValidFile_shouldImportRows() throws Exception {
        String csv = """
                firstName,lastName,title,email,emailType,phone,phoneType
                Jane,Smith,Manager,jane@example.com,WORK,9876543210,WORK
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "contacts.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        when(contactService.createContact(any(CreateContactRequest.class)))
                .thenReturn(new ContactResponse(1L, "Jane", "Smith", "Manager", List.of(), List.of()));

        var response = contactCsvService.importContactsCsv(file);

        assertEquals(1, response.getImportedCount());
        assertEquals(0, response.getFailedCount());
        verify(contactService).createContact(any(CreateContactRequest.class));
    }

    @Test
    void importContactsCsv_withInvalidHeader_shouldThrow() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "contacts.csv",
                "text/csv",
                "bad,header\n".getBytes(StandardCharsets.UTF_8)
        );

        assertThrows(InvalidCsvFileException.class, () -> contactCsvService.importContactsCsv(file));
    }

    @Test
    void exportContactsCsv_withNoContacts_shouldReturnHeaderOnly() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(contactRepository.findAllByUserIdOrderByLastNameAscFirstNameAsc(1L))
                .thenReturn(Collections.emptyList());

        String csv = contactCsvService.exportContactsCsv();

        assertEquals("firstName,lastName,title,email,emailType,phone,phoneType\n", csv);
    }
}
