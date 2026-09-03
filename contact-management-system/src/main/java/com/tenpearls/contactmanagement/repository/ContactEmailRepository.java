package com.tenpearls.contactmanagement.repository;

import com.tenpearls.contactmanagement.entity.ContactEmail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactEmailRepository extends JpaRepository<ContactEmail, Long> {

    void deleteByContactId(Long contactId);
}
