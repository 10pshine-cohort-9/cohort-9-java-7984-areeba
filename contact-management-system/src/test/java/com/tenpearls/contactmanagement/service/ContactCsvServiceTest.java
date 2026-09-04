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
import static org.mockito.ArgumentCaptor.forClass;
import org.mockito.ArgumentCaptor;

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
    void importContactsCsv_withUnterminatedQuotedField_shouldThrow() {
        String csv = """
                firstName,lastName,title,email,emailType,phone,phoneType
                Jane,Doe,"Manager
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "contacts.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        InvalidCsvFileException exception = assertThrows(
                InvalidCsvFileException.class,
                () -> contactCsvService.importContactsCsv(file)
        );

        assertEquals("CSV file contains an unterminated quoted field", exception.getMessage());
    }

    @Test
    void exportContactsCsv_withNoContacts_shouldReturnHeaderOnly() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(contactRepository.findAllByUserIdOrderByLastNameAscFirstNameAsc(1L))
                .thenReturn(Collections.emptyList());

        String csv = contactCsvService.exportContactsCsv();

        assertEquals("firstName,lastName,title,email,emailType,phone,phoneType\n", csv);
    }

    @Test
    void exportContactsCsv_shouldIncludeAllEmailsAndPhonesInStableOrder() {
        Contact contact = new Contact();
        contact.setId(1L);
        contact.setFirstName("John");
        contact.setLastName("Doe");
        contact.setTitle("Engineer");
        contact.setUser(user);

        ContactEmail workEmail = new ContactEmail();
        workEmail.setId(2L);
        workEmail.setEmail("john@work.com");
        workEmail.setType(EmailType.WORK);
        workEmail.setContact(contact);

        ContactEmail personalEmail = new ContactEmail();
        personalEmail.setId(1L);
        personalEmail.setEmail("john@home.com");
        personalEmail.setType(EmailType.PERSONAL);
        personalEmail.setContact(contact);

        ContactPhone homePhone = new ContactPhone();
        homePhone.setId(1L);
        homePhone.setPhoneNumber("1111111111");
        homePhone.setType(PhoneType.HOME);
        homePhone.setContact(contact);

        ContactPhone workPhone = new ContactPhone();
        workPhone.setId(2L);
        workPhone.setPhoneNumber("2222222222");
        workPhone.setType(PhoneType.WORK);
        workPhone.setContact(contact);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(contactRepository.findAllByUserIdOrderByLastNameAscFirstNameAsc(1L)).thenReturn(List.of(contact));
        when(contactEmailRepository.findByContact_IdIn(List.of(1L)))
                .thenReturn(List.of(workEmail, personalEmail));
        when(contactPhoneRepository.findByContact_IdIn(List.of(1L)))
                .thenReturn(List.of(workPhone, homePhone));

        String csv = contactCsvService.exportContactsCsv();

        assertTrue(csv.contains(
                "John,Doe,Engineer,john@home.com|john@work.com,PERSONAL|WORK,1111111111|2222222222,HOME|WORK"
        ));
    }

    @Test
    void exportContactsCsv_shouldNeutralizeFormulaInjection() {
        Contact contact = new Contact();
        contact.setId(1L);
        contact.setFirstName("=1+1");
        contact.setLastName("+cmd|'/c calc'!A0");
        contact.setTitle("@SUM(A1:A2)");
        contact.setUser(user);

        ContactEmail email = new ContactEmail();
        email.setId(1L);
        email.setEmail("-alert@example.com");
        email.setType(EmailType.WORK);
        email.setContact(contact);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(contactRepository.findAllByUserIdOrderByLastNameAscFirstNameAsc(1L)).thenReturn(List.of(contact));
        when(contactEmailRepository.findByContact_IdIn(List.of(1L))).thenReturn(List.of(email));
        when(contactPhoneRepository.findByContact_IdIn(List.of(1L))).thenReturn(List.of());

        String csv = contactCsvService.exportContactsCsv();

        assertTrue(csv.contains("'=1+1"));
        assertTrue(csv.contains("'+cmd|'/c calc'!A0"));
        assertTrue(csv.contains("'@SUM(A1:A2)"));
        assertTrue(csv.contains("'-alert@example.com"));
    }

    @Test
    void importContactsCsv_withMultipleEmailsAndPhones_shouldPreserveAllValues() throws Exception {
        String csv = """
                firstName,lastName,title,email,emailType,phone,phoneType
                Jane,Smith,Manager,jane@work.com|jane@home.com,WORK|PERSONAL,1111111111|2222222222,WORK|HOME
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "contacts.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        when(contactService.createContact(any(CreateContactRequest.class)))
                .thenReturn(new ContactResponse(1L, "Jane", "Smith", "Manager", List.of(), List.of()));

        ArgumentCaptor<CreateContactRequest> requestCaptor = forClass(CreateContactRequest.class);

        var response = contactCsvService.importContactsCsv(file);

        assertEquals(1, response.getImportedCount());
        assertEquals(0, response.getFailedCount());
        verify(contactService).createContact(requestCaptor.capture());

        CreateContactRequest request = requestCaptor.getValue();
        assertEquals(2, request.getEmails().size());
        assertEquals("jane@work.com", request.getEmails().get(0).getEmail());
        assertEquals(EmailType.WORK, request.getEmails().get(0).getType());
        assertEquals("jane@home.com", request.getEmails().get(1).getEmail());
        assertEquals(EmailType.PERSONAL, request.getEmails().get(1).getType());
        assertEquals(2, request.getPhones().size());
        assertEquals("1111111111", request.getPhones().get(0).getPhoneNumber());
        assertEquals(PhoneType.WORK, request.getPhones().get(0).getType());
        assertEquals("2222222222", request.getPhones().get(1).getPhoneNumber());
        assertEquals(PhoneType.HOME, request.getPhones().get(1).getType());
    }

    @Test
    void exportAndImport_shouldRoundTripMultipleEmailsAndPhones() {
        Contact contact = new Contact();
        contact.setId(1L);
        contact.setFirstName("Alex");
        contact.setLastName("Lee");
        contact.setTitle("Lead");
        contact.setUser(user);

        ContactEmail firstEmail = new ContactEmail();
        firstEmail.setId(10L);
        firstEmail.setEmail("alex@work.com");
        firstEmail.setType(EmailType.WORK);
        firstEmail.setContact(contact);

        ContactEmail secondEmail = new ContactEmail();
        secondEmail.setId(11L);
        secondEmail.setEmail("alex@home.com");
        secondEmail.setType(EmailType.PERSONAL);
        secondEmail.setContact(contact);

        ContactPhone firstPhone = new ContactPhone();
        firstPhone.setId(20L);
        firstPhone.setPhoneNumber("3333333333");
        firstPhone.setType(PhoneType.WORK);
        firstPhone.setContact(contact);

        ContactPhone secondPhone = new ContactPhone();
        secondPhone.setId(21L);
        secondPhone.setPhoneNumber("4444444444");
        secondPhone.setType(PhoneType.HOME);
        secondPhone.setContact(contact);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(contactRepository.findAllByUserIdOrderByLastNameAscFirstNameAsc(1L)).thenReturn(List.of(contact));
        when(contactEmailRepository.findByContact_IdIn(List.of(1L)))
                .thenReturn(List.of(firstEmail, secondEmail));
        when(contactPhoneRepository.findByContact_IdIn(List.of(1L)))
                .thenReturn(List.of(firstPhone, secondPhone));
        when(contactService.createContact(any(CreateContactRequest.class)))
                .thenReturn(new ContactResponse(2L, "Alex", "Lee", "Lead", List.of(), List.of()));

        String exportedCsv = contactCsvService.exportContactsCsv();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "contacts.csv",
                "text/csv",
                exportedCsv.getBytes(StandardCharsets.UTF_8)
        );

        ArgumentCaptor<CreateContactRequest> requestCaptor = forClass(CreateContactRequest.class);

        contactCsvService.importContactsCsv(file);

        verify(contactService).createContact(requestCaptor.capture());
        CreateContactRequest importedRequest = requestCaptor.getValue();

        assertEquals(2, importedRequest.getEmails().size());
        assertEquals("alex@work.com", importedRequest.getEmails().get(0).getEmail());
        assertEquals(EmailType.WORK, importedRequest.getEmails().get(0).getType());
        assertEquals("alex@home.com", importedRequest.getEmails().get(1).getEmail());
        assertEquals(EmailType.PERSONAL, importedRequest.getEmails().get(1).getType());
        assertEquals(2, importedRequest.getPhones().size());
        assertEquals("3333333333", importedRequest.getPhones().get(0).getPhoneNumber());
        assertEquals(PhoneType.WORK, importedRequest.getPhones().get(0).getType());
        assertEquals("4444444444", importedRequest.getPhones().get(1).getPhoneNumber());
        assertEquals(PhoneType.HOME, importedRequest.getPhones().get(1).getType());
    }

    @Test
    void importContactsCsv_withQuotedMultilineField_shouldParseCompleteRecord() throws Exception {
        String csv = """
                firstName,lastName,title,email,emailType,phone,phoneType
                Mary,O'Connor,"VP
                Sales",mary@example.com,WORK,1234567890,HOME
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "contacts.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        when(contactService.createContact(any(CreateContactRequest.class)))
                .thenReturn(new ContactResponse(1L, "Mary", "O'Connor", "VP\nSales", List.of(), List.of()));

        ArgumentCaptor<CreateContactRequest> requestCaptor = forClass(CreateContactRequest.class);

        var response = contactCsvService.importContactsCsv(file);

        assertEquals(1, response.getImportedCount());
        assertEquals(0, response.getFailedCount());
        verify(contactService).createContact(requestCaptor.capture());
        assertEquals("VP\nSales", requestCaptor.getValue().getTitle());
    }

    @Test
    void exportAndImport_shouldRoundTripCommasQuotesAndLineBreaks() {
        Contact contact = new Contact();
        contact.setId(1L);
        contact.setFirstName("Mary");
        contact.setLastName("O'Connor");
        contact.setTitle("VP\nSales");
        contact.setUser(user);

        ContactEmail email = new ContactEmail();
        email.setId(1L);
        email.setEmail("mary,o'connor@example.com");
        email.setType(EmailType.WORK);
        email.setContact(contact);

        ContactPhone phone = new ContactPhone();
        phone.setId(1L);
        phone.setPhoneNumber("\"555\"");
        phone.setType(PhoneType.HOME);
        phone.setContact(contact);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(contactRepository.findAllByUserIdOrderByLastNameAscFirstNameAsc(1L)).thenReturn(List.of(contact));
        when(contactEmailRepository.findByContact_IdIn(List.of(1L))).thenReturn(List.of(email));
        when(contactPhoneRepository.findByContact_IdIn(List.of(1L))).thenReturn(List.of(phone));
        when(contactService.createContact(any(CreateContactRequest.class)))
                .thenReturn(new ContactResponse(2L, "Mary", "O'Connor", "VP\nSales", List.of(), List.of()));

        String exportedCsv = contactCsvService.exportContactsCsv();

        assertTrue(exportedCsv.contains("\"VP\nSales\""));
        assertTrue(exportedCsv.contains("\"mary,o'connor@example.com\""));
        assertTrue(exportedCsv.contains("\"\"\"555\"\"\""));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "contacts.csv",
                "text/csv",
                exportedCsv.getBytes(StandardCharsets.UTF_8)
        );

        ArgumentCaptor<CreateContactRequest> requestCaptor = forClass(CreateContactRequest.class);

        var response = contactCsvService.importContactsCsv(file);

        assertEquals(1, response.getImportedCount());
        assertEquals(0, response.getFailedCount());
        verify(contactService).createContact(requestCaptor.capture());

        CreateContactRequest importedRequest = requestCaptor.getValue();
        assertEquals("Mary", importedRequest.getFirstName());
        assertEquals("O'Connor", importedRequest.getLastName());
        assertEquals("VP\nSales", importedRequest.getTitle());
        assertEquals("mary,o'connor@example.com", importedRequest.getEmails().get(0).getEmail());
        assertEquals(EmailType.WORK, importedRequest.getEmails().get(0).getType());
        assertEquals("\"555\"", importedRequest.getPhones().get(0).getPhoneNumber());
        assertEquals(PhoneType.HOME, importedRequest.getPhones().get(0).getType());
    }
}
