package com.tenpearls.contactmanagement.service;

import com.tenpearls.contactmanagement.dto.contact.ContactEmailRequest;
import com.tenpearls.contactmanagement.dto.contact.ContactEmailResponse;
import com.tenpearls.contactmanagement.dto.contact.ContactPhoneRequest;
import com.tenpearls.contactmanagement.dto.contact.ContactPhoneResponse;
import com.tenpearls.contactmanagement.dto.contact.ContactResponse;
import com.tenpearls.contactmanagement.dto.contact.CreateContactRequest;
import com.tenpearls.contactmanagement.dto.contact.PagedContactResponse;
import com.tenpearls.contactmanagement.dto.contact.UpdateContactRequest;
import com.tenpearls.contactmanagement.entity.Contact;
import com.tenpearls.contactmanagement.entity.ContactEmail;
import com.tenpearls.contactmanagement.entity.ContactPhone;
import com.tenpearls.contactmanagement.entity.User;
import com.tenpearls.contactmanagement.exception.ContactNotFoundException;
import com.tenpearls.contactmanagement.exception.UserNotFoundException;
import com.tenpearls.contactmanagement.repository.ContactEmailRepository;
import com.tenpearls.contactmanagement.repository.ContactPhoneRepository;
import com.tenpearls.contactmanagement.repository.ContactRepository;
import com.tenpearls.contactmanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class ContactService {

    private static final Logger logger = LoggerFactory.getLogger(ContactService.class);

    private final ContactRepository contactRepository;
    private final ContactEmailRepository contactEmailRepository;
    private final ContactPhoneRepository contactPhoneRepository;
    private final UserRepository userRepository;

    public ContactService(
            ContactRepository contactRepository,
            ContactEmailRepository contactEmailRepository,
            ContactPhoneRepository contactPhoneRepository,
            UserRepository userRepository
    ) {
        this.contactRepository = contactRepository;
        this.contactEmailRepository = contactEmailRepository;
        this.contactPhoneRepository = contactPhoneRepository;
        this.userRepository = userRepository;
    }

    public ContactResponse createContact(CreateContactRequest request) {
        User user = getAuthenticatedUser();

        Contact contact = new Contact();
        contact.setFirstName(request.getFirstName());
        contact.setLastName(request.getLastName());
        contact.setTitle(request.getTitle());
        contact.setUser(user);

        Contact savedContact = contactRepository.save(contact);
        saveEmails(savedContact, request.getEmails());
        savePhones(savedContact, request.getPhones());

        logger.info("Contact created with id: {} for user id: {}", savedContact.getId(), user.getId());

        return toResponse(loadContactForUser(savedContact.getId(), user.getId()));
    }

    @Transactional(readOnly = true)
    public ContactResponse getContact(Long contactId) {
        User user = getAuthenticatedUser();
        return toResponse(loadContactForUser(contactId, user.getId()));
    }

    public ContactResponse updateContact(Long contactId, UpdateContactRequest request) {
        User user = getAuthenticatedUser();
        Contact contact = loadContactForUser(contactId, user.getId());

        contact.setFirstName(request.getFirstName());
        contact.setLastName(request.getLastName());
        contact.setTitle(request.getTitle());

        contactEmailRepository.deleteByContactId(contact.getId());
        contactPhoneRepository.deleteByContactId(contact.getId());
        saveEmails(contact, request.getEmails());
        savePhones(contact, request.getPhones());

        Contact updatedContact = contactRepository.save(contact);

        logger.info("Contact updated with id: {} for user id: {}", updatedContact.getId(), user.getId());

        return toResponse(loadContactForUser(updatedContact.getId(), user.getId()));
    }

    public void deleteContact(Long contactId) {
        User user = getAuthenticatedUser();
        Contact contact = loadContactForUser(contactId, user.getId());

        contactEmailRepository.deleteByContactId(contact.getId());
        contactPhoneRepository.deleteByContactId(contact.getId());
        contactRepository.delete(contact);

        logger.info("Contact deleted with id: {} for user id: {}", contactId, user.getId());
    }

    @Transactional(readOnly = true)
    public PagedContactResponse listContacts(Pageable pageable) {
        User user = getAuthenticatedUser();
        Page<Contact> page = contactRepository.findByUserId(user.getId(), pageable);
        return toPagedResponse(page);
    }

    @Transactional(readOnly = true)
    public PagedContactResponse searchContacts(String firstName, String lastName, Pageable pageable) {
        User user = getAuthenticatedUser();
        Page<Contact> page = contactRepository.searchByUserId(
                user.getId(),
                normalizeSearchTerm(firstName),
                normalizeSearchTerm(lastName),
                pageable
        );
        return toPagedResponse(page);
    }

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private Contact loadContactForUser(Long contactId, Long userId) {
        Contact contact = contactRepository.findByIdAndUserId(contactId, userId)
                .orElseThrow(() -> new ContactNotFoundException("Contact not found"));

        contact.setEmails(contactEmailRepository.findByContactId(contactId));
        contact.setPhones(contactPhoneRepository.findByContactId(contactId));

        return contact;
    }

    private void saveEmails(Contact contact, List<ContactEmailRequest> emailRequests) {
        if (emailRequests == null || emailRequests.isEmpty()) {
            return;
        }

        for (ContactEmailRequest emailRequest : emailRequests) {
            ContactEmail contactEmail = new ContactEmail();
            contactEmail.setEmail(emailRequest.getEmail());
            contactEmail.setType(emailRequest.getType());
            contactEmail.setContact(contact);
            contactEmailRepository.save(contactEmail);
        }
    }

    private void savePhones(Contact contact, List<ContactPhoneRequest> phoneRequests) {
        if (phoneRequests == null || phoneRequests.isEmpty()) {
            return;
        }

        for (ContactPhoneRequest phoneRequest : phoneRequests) {
            ContactPhone contactPhone = new ContactPhone();
            contactPhone.setPhoneNumber(phoneRequest.getPhoneNumber());
            contactPhone.setType(phoneRequest.getType());
            contactPhone.setContact(contact);
            contactPhoneRepository.save(contactPhone);
        }
    }

    private String normalizeSearchTerm(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private PagedContactResponse toPagedResponse(Page<Contact> page) {
        List<ContactResponse> content = page.getContent().stream()
                .map(this::toResponse)
                .toList();

        return new PagedContactResponse(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    private ContactResponse toResponse(Contact contact) {
        List<ContactEmailResponse> emails = contact.getEmails() == null
                ? Collections.emptyList()
                : contact.getEmails().stream()
                .map(email -> new ContactEmailResponse(email.getId(), email.getEmail(), email.getType()))
                .toList();

        List<ContactPhoneResponse> phones = contact.getPhones() == null
                ? Collections.emptyList()
                : contact.getPhones().stream()
                .map(phone -> new ContactPhoneResponse(phone.getId(), phone.getPhoneNumber(), phone.getType()))
                .toList();

        return new ContactResponse(
                contact.getId(),
                contact.getFirstName(),
                contact.getLastName(),
                contact.getTitle(),
                new ArrayList<>(emails),
                new ArrayList<>(phones)
        );
    }
}
