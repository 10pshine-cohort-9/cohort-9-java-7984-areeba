package com.tenpearls.contactmanagement.repository;

import com.tenpearls.contactmanagement.entity.ContactPhone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactPhoneRepository extends JpaRepository<ContactPhone, Long> {

    void deleteByContactId(Long contactId);
}
