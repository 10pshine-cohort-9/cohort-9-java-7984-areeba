package com.tenpearls.contactmanagement.service;

import com.tenpearls.contactmanagement.dto.contact.ContactEmailRequest;
import com.tenpearls.contactmanagement.dto.contact.ContactImportError;
import com.tenpearls.contactmanagement.dto.contact.ContactImportResponse;
import com.tenpearls.contactmanagement.dto.contact.ContactPhoneRequest;
import com.tenpearls.contactmanagement.dto.contact.ContactResponse;
import com.tenpearls.contactmanagement.dto.contact.CreateContactRequest;
import com.tenpearls.contactmanagement.entity.Contact;
import com.tenpearls.contactmanagement.entity.ContactEmail;
import com.tenpearls.contactmanagement.entity.ContactPhone;
import com.tenpearls.contactmanagement.entity.User;
import com.tenpearls.contactmanagement.entity.enums.EmailType;
import com.tenpearls.contactmanagement.entity.enums.PhoneType;
import com.tenpearls.contactmanagement.exception.InvalidCsvFileException;
import com.tenpearls.contactmanagement.exception.UserNotFoundException;
import com.tenpearls.contactmanagement.repository.ContactEmailRepository;
import com.tenpearls.contactmanagement.repository.ContactPhoneRepository;
import com.tenpearls.contactmanagement.repository.ContactRepository;
import com.tenpearls.contactmanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ContactCsvService {

    private static final Logger logger = LoggerFactory.getLogger(ContactCsvService.class);

    private static final String CSV_HEADER =
            "firstName,lastName,title,email,emailType,phone,phoneType";

    private final ContactRepository contactRepository;
    private final ContactEmailRepository contactEmailRepository;
    private final ContactPhoneRepository contactPhoneRepository;
    private final UserRepository userRepository;
    private final ContactService contactService;

    public ContactCsvService(
            ContactRepository contactRepository,
            ContactEmailRepository contactEmailRepository,
            ContactPhoneRepository contactPhoneRepository,
            UserRepository userRepository,
            ContactService contactService
    ) {
        this.contactRepository = contactRepository;
        this.contactEmailRepository = contactEmailRepository;
        this.contactPhoneRepository = contactPhoneRepository;
        this.userRepository = userRepository;
        this.contactService = contactService;
    }

    @Transactional(readOnly = true)
    public String exportContactsCsv() {
        User user = getAuthenticatedUser();
        List<Contact> contacts = contactRepository.findAllByUserIdOrderByLastNameAscFirstNameAsc(user.getId());

        if (contacts.isEmpty()) {
            return CSV_HEADER + "\n";
        }

        List<Long> contactIds = contacts.stream().map(Contact::getId).toList();
        Map<Long, List<ContactEmail>> emailsByContactId = contactEmailRepository
                .findByContact_IdIn(contactIds)
                .stream()
                .collect(Collectors.groupingBy(email -> email.getContact().getId()));
        Map<Long, List<ContactPhone>> phonesByContactId = contactPhoneRepository
                .findByContact_IdIn(contactIds)
                .stream()
                .collect(Collectors.groupingBy(phone -> phone.getContact().getId()));

        StringBuilder csv = new StringBuilder(CSV_HEADER).append('\n');

        for (Contact contact : contacts) {
            List<ContactEmail> emails = emailsByContactId.getOrDefault(contact.getId(), Collections.emptyList());
            List<ContactPhone> phones = phonesByContactId.getOrDefault(contact.getId(), Collections.emptyList());

            String email = emails.isEmpty() ? "" : emails.get(0).getEmail();
            String emailType = emails.isEmpty() ? "" : emails.get(0).getType().name();
            String phone = phones.isEmpty() ? "" : phones.get(0).getPhoneNumber();
            String phoneType = phones.isEmpty() ? "" : phones.get(0).getType().name();

            csv.append(escapeCsv(contact.getFirstName())).append(',');
            csv.append(escapeCsv(contact.getLastName())).append(',');
            csv.append(escapeCsv(contact.getTitle() == null ? "" : contact.getTitle())).append(',');
            csv.append(escapeCsv(email)).append(',');
            csv.append(escapeCsv(emailType)).append(',');
            csv.append(escapeCsv(phone)).append(',');
            csv.append(escapeCsv(phoneType)).append('\n');
        }

        return csv.toString();
    }

    public ContactImportResponse importContactsCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidCsvFileException("CSV file is required");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new InvalidCsvFileException("Only .csv files are supported");
        }

        ContactImportResponse response = new ContactImportResponse();
        response.setErrors(new ArrayList<>());

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new InvalidCsvFileException("CSV file is empty");
            }

            validateHeader(headerLine);

            String line;
            int rowNumber = 1;

            while ((line = reader.readLine()) != null) {
                rowNumber++;
                if (line.isBlank()) {
                    continue;
                }

                try {
                    CreateContactRequest request = parseRow(line);
                    ContactResponse created = contactService.createContact(request);
                    response.setImportedCount(response.getImportedCount() + 1);
                    logger.info("Imported contact from CSV row {} with id {}", rowNumber, created.getId());
                } catch (Exception exception) {
                    response.setFailedCount(response.getFailedCount() + 1);
                    response.getErrors().add(new ContactImportError(rowNumber, exception.getMessage()));
                }
            }
        } catch (IOException exception) {
            throw new InvalidCsvFileException("Failed to read CSV file");
        }

        return response;
    }

    private void validateHeader(String headerLine) {
        String normalized = headerLine.trim().toLowerCase(Locale.ROOT);
        String expected = CSV_HEADER.toLowerCase(Locale.ROOT);
        if (!normalized.equals(expected)) {
            throw new InvalidCsvFileException(
                    "Invalid CSV header. Expected: " + CSV_HEADER
            );
        }
    }

    private CreateContactRequest parseRow(String line) {
        List<String> values = parseCsvLine(line);

        if (values.size() < 2) {
            throw new IllegalArgumentException("Row must include firstName and lastName");
        }

        String firstName = values.get(0).trim();
        String lastName = values.get(1).trim();

        if (firstName.isEmpty() || lastName.isEmpty()) {
            throw new IllegalArgumentException("firstName and lastName are required");
        }

        String title = values.size() > 2 ? values.get(2).trim() : "";
        String email = values.size() > 3 ? values.get(3).trim() : "";
        String emailTypeValue = values.size() > 4 ? values.get(4).trim() : "";
        String phone = values.size() > 5 ? values.get(5).trim() : "";
        String phoneTypeValue = values.size() > 6 ? values.get(6).trim() : "";

        CreateContactRequest request = new CreateContactRequest();
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setTitle(title.isEmpty() ? null : title);

        if (!email.isEmpty()) {
            ContactEmailRequest emailRequest = new ContactEmailRequest();
            emailRequest.setEmail(email);
            emailRequest.setType(parseEmailType(emailTypeValue));
            request.getEmails().add(emailRequest);
        }

        if (!phone.isEmpty()) {
            ContactPhoneRequest phoneRequest = new ContactPhoneRequest();
            phoneRequest.setPhoneNumber(phone);
            phoneRequest.setType(parsePhoneType(phoneTypeValue));
            request.getPhones().add(phoneRequest);
        }

        return request;
    }

    private EmailType parseEmailType(String value) {
        if (value == null || value.isBlank()) {
            return EmailType.WORK;
        }
        return EmailType.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    private PhoneType parsePhoneType(String value) {
        if (value == null || value.isBlank()) {
            return PhoneType.HOME;
        }
        return PhoneType.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);

            if (character == '"') {
                if (inQuotes && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    current.append('"');
                    index++;
                } else {
                    inQuotes = !inQuotes;
                }
                continue;
            }

            if (character == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
                continue;
            }

            current.append(character);
        }

        values.add(current.toString());
        return values;
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }

        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
