package com.tenpearls.contactmanagement.controller;

import com.tenpearls.contactmanagement.config.JwtProperties;
import com.tenpearls.contactmanagement.config.PasswordConfig;
import com.tenpearls.contactmanagement.config.SecurityConfig;
import com.tenpearls.contactmanagement.entity.Contact;
import com.tenpearls.contactmanagement.entity.ContactEmail;
import com.tenpearls.contactmanagement.entity.ContactPhone;
import com.tenpearls.contactmanagement.entity.User;
import com.tenpearls.contactmanagement.entity.enums.EmailType;
import com.tenpearls.contactmanagement.entity.enums.PhoneType;
import com.tenpearls.contactmanagement.repository.ContactEmailRepository;
import com.tenpearls.contactmanagement.repository.ContactPhoneRepository;
import com.tenpearls.contactmanagement.repository.ContactRepository;
import com.tenpearls.contactmanagement.repository.UserRepository;
import com.tenpearls.contactmanagement.security.JwtAuthenticationEntryPoint;
import com.tenpearls.contactmanagement.security.JwtService;
import com.tenpearls.contactmanagement.service.ContactCsvService;
import com.tenpearls.contactmanagement.service.ContactService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

@WebMvcTest(controllers = ContactController.class)
@Import({
        SecurityConfig.class,
        PasswordConfig.class,
        ContactService.class,
        ContactCsvService.class,
        JwtService.class,
        JwtAuthenticationEntryPoint.class
})
@EnableConfigurationProperties(JwtProperties.class)
class ContactControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private ContactRepository contactRepository;

    @MockitoBean
    private ContactEmailRepository contactEmailRepository;

    @MockitoBean
    private ContactPhoneRepository contactPhoneRepository;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void createContact_withValidBearerToken_returns201() throws Exception {
        User user = buildUser();
        Contact contact = buildContact(1L, user);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(contactRepository.save(any(Contact.class))).thenAnswer(invocation -> {
            Contact savedContact = invocation.getArgument(0);
            savedContact.setId(1L);
            return savedContact;
        });
        when(contactRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(contact));
        when(contactEmailRepository.findByContactId(1L)).thenReturn(contact.getEmails());
        when(contactPhoneRepository.findByContactId(1L)).thenReturn(contact.getPhones());

        String token = jwtService.generateToken("test@example.com", 1L, 0);

        mockMvc.perform(post("/api/contacts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "John",
                                  "lastName": "Doe",
                                  "title": "Engineer",
                                  "emails": [
                                    { "email": "john@example.com", "type": "WORK" }
                                  ],
                                  "phones": [
                                    { "phoneNumber": "1234567890", "type": "HOME" }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.emails[0].email").value("john@example.com"));
    }

    @Test
    void getContact_withValidBearerToken_returns200() throws Exception {
        User user = buildUser();
        Contact contact = buildContact(1L, user);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(contactRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(contact));
        when(contactEmailRepository.findByContactId(1L)).thenReturn(contact.getEmails());
        when(contactPhoneRepository.findByContactId(1L)).thenReturn(contact.getPhones());

        String token = jwtService.generateToken("test@example.com", 1L, 0);

        mockMvc.perform(get("/api/contacts/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void getContact_whenContactNotFound_returns404() throws Exception {
        User user = buildUser();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(contactRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        String token = jwtService.generateToken("test@example.com", 1L, 0);

        mockMvc.perform(get("/api/contacts/99")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateContact_withValidBearerToken_returns200() throws Exception {
        User user = buildUser();
        Contact contact = buildContact(1L, user);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(contactRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(contact));
        when(contactRepository.save(any(Contact.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(contactEmailRepository.findByContactId(1L)).thenReturn(contact.getEmails());
        when(contactPhoneRepository.findByContactId(1L)).thenReturn(contact.getPhones());

        String token = jwtService.generateToken("test@example.com", 1L, 0);

        mockMvc.perform(put("/api/contacts/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Jane",
                                  "lastName": "Smith",
                                  "title": "Manager",
                                  "emails": [],
                                  "phones": []
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"));
    }

    @Test
    void deleteContact_withValidBearerToken_returns204() throws Exception {
        User user = buildUser();
        Contact contact = buildContact(1L, user);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(contactRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(contact));
        when(contactEmailRepository.findByContactId(1L)).thenReturn(contact.getEmails());
        when(contactPhoneRepository.findByContactId(1L)).thenReturn(contact.getPhones());

        String token = jwtService.generateToken("test@example.com", 1L, 0);

        mockMvc.perform(delete("/api/contacts/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void listContacts_withValidBearerToken_returns200() throws Exception {
        User user = buildUser();
        Contact contact = buildContact(1L, user);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(contactRepository.findByUserId(eq(1L), any())).thenReturn(
                new PageImpl<>(List.of(contact), PageRequest.of(0, 10), 1)
        );
        when(contactEmailRepository.findByContact_IdIn(List.of(1L))).thenReturn(contact.getEmails());
        when(contactPhoneRepository.findByContact_IdIn(List.of(1L))).thenReturn(contact.getPhones());

        String token = jwtService.generateToken("test@example.com", 1L, 0);

        mockMvc.perform(get("/api/contacts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName").value("John"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void searchContacts_withValidBearerToken_returns200() throws Exception {
        User user = buildUser();
        Contact contact = buildContact(1L, user);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(contactRepository.searchByUserId(eq(1L), eq("John"), eq(null), any())).thenReturn(
                new PageImpl<>(List.of(contact), PageRequest.of(0, 10), 1)
        );
        when(contactEmailRepository.findByContact_IdIn(List.of(1L))).thenReturn(contact.getEmails());
        when(contactPhoneRepository.findByContact_IdIn(List.of(1L))).thenReturn(contact.getPhones());

        String token = jwtService.generateToken("test@example.com", 1L, 0);

        mockMvc.perform(get("/api/contacts/search")
                        .param("firstName", "John")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName").value("John"));
    }

    @Test
    void exportContacts_withValidBearerToken_returnsCsvFile() throws Exception {
        User user = buildUser();
        Contact contact = buildContact(1L, user);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(contactRepository.findAllByUserIdOrderByLastNameAscFirstNameAsc(1L)).thenReturn(List.of(contact));
        when(contactEmailRepository.findByContact_IdIn(List.of(1L))).thenReturn(contact.getEmails());
        when(contactPhoneRepository.findByContact_IdIn(List.of(1L))).thenReturn(contact.getPhones());

        String token = jwtService.generateToken("test@example.com", 1L, 0);

        mockMvc.perform(get("/api/contacts/export")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("contacts.csv")));
    }

    @Test
    void importContacts_withValidCsv_returns200() throws Exception {
        User user = buildUser();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(contactRepository.save(any(Contact.class))).thenAnswer(invocation -> {
            Contact savedContact = invocation.getArgument(0);
            savedContact.setId(2L);
            return savedContact;
        });
        when(contactRepository.findByIdAndUserId(2L, 1L)).thenAnswer(invocation -> {
            Contact savedContact = buildContact(2L, user);
            savedContact.setFirstName("Jane");
            savedContact.setLastName("Smith");
            return Optional.of(savedContact);
        });
        when(contactEmailRepository.findByContactId(2L)).thenReturn(List.of());
        when(contactPhoneRepository.findByContactId(2L)).thenReturn(List.of());

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

        String token = jwtService.generateToken("test@example.com", 1L, 0);

        mockMvc.perform(multipart("/api/contacts/import")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importedCount").value(1))
                .andExpect(jsonPath("$.failedCount").value(0));
    }

    @Test
    void createContact_withoutBearerToken_returns401() throws Exception {
        mockMvc.perform(post("/api/contacts")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "John",
                                  "lastName": "Doe",
                                  "emails": [],
                                  "phones": []
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    private User buildUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encoded-password");
        user.setTokenVersion(0);
        return user;
    }

    private Contact buildContact(Long id, User user) {
        Contact contact = new Contact();
        contact.setId(id);
        contact.setFirstName("John");
        contact.setLastName("Doe");
        contact.setTitle("Engineer");
        contact.setUser(user);

        ContactEmail email = new ContactEmail();
        email.setId(10L);
        email.setEmail("john@example.com");
        email.setType(EmailType.WORK);
        email.setContact(contact);

        ContactPhone phone = new ContactPhone();
        phone.setId(20L);
        phone.setPhoneNumber("1234567890");
        phone.setType(PhoneType.HOME);
        phone.setContact(contact);

        contact.setEmails(new ArrayList<>(List.of(email)));
        contact.setPhones(new ArrayList<>(List.of(phone)));

        return contact;
    }
}
